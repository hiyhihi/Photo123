package com.example.photofilter.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.photofilter.R;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.data.FilterRepository;
import com.example.photofilter.data.ImageRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorPresenter implements EditorContract.Presenter {

    private static final int THUMBNAIL_MAX_DIMENSION = 200;

    private final Context appContext;
    private final ImageRepository imageRepository;
    private final FilterRepository filterRepository;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private EditorContract.View view;
    private List<FilterItem> availableFilters;
    private List<FilterThumbnail> currentThumbnails;
    private Bitmap originalBitmap;
    private Bitmap currentFilteredBitmap;
    private Uri lastSavedUri;

    /** Bumped on every new user action; stale async results compare against it and are discarded. */
    private int requestId;

    public EditorPresenter(Context context) {
        this(context, Executors.newSingleThreadExecutor(), new ImageRepository());
    }

    /** Visible for testing: lets tests inject a same-thread executor and a fake repository for deterministic results. */
    EditorPresenter(Context context, ExecutorService executor, ImageRepository imageRepository) {
        this.appContext = context.getApplicationContext();
        this.imageRepository = imageRepository;
        this.filterRepository = new FilterRepository();
        this.executor = executor;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void attachView(EditorContract.View view) {
        this.view = view;
        if (availableFilters == null) {
            availableFilters = filterRepository.getAvailableFilters(appContext);
        }
        view.showFilterList(availableFilters);
        if (originalBitmap != null) {
            view.showOriginalImage(originalBitmap);
        }
        if (currentFilteredBitmap != null) {
            view.showFilteredImage(currentFilteredBitmap);
        }
        if (currentThumbnails != null) {
            view.showFilterThumbnails(currentThumbnails);
        }
    }

    @Override
    public void detachView() {
        this.view = null;
        executor.shutdown(); // let an in-flight save finish writing rather than corrupting the file
        recycleIfPossible(originalBitmap);
        recycleIfPossible(currentFilteredBitmap);
        recycleThumbnails(currentThumbnails);
        originalBitmap = null;
        currentFilteredBitmap = null;
        currentThumbnails = null;
    }

    @Override
    public void onImagePicked(Uri imageUri, int viewportWidth, int viewportHeight) {
        final int myRequestId = ++requestId;
        if (view != null) {
            view.showLoading(true);
        }
        executor.execute(() -> {
            try {
                Bitmap loaded = imageRepository.loadDownsampled(appContext, imageUri, viewportWidth, viewportHeight);
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        loaded.recycle();
                        return;
                    }
                    recycleIfPossible(originalBitmap);
                    recycleIfPossible(currentFilteredBitmap);
                    recycleThumbnails(currentThumbnails);
                    originalBitmap = loaded;
                    currentFilteredBitmap = null;
                    currentThumbnails = null;
                    lastSavedUri = null;
                    view.showLoading(false);
                    view.showOriginalImage(loaded);
                    generateThumbnails(myRequestId, loaded);
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        return;
                    }
                    view.showLoading(false);
                    view.showError(appContext.getString(R.string.error_open_image));
                });
            }
        });
    }

    private void generateThumbnails(int myRequestId, Bitmap source) {
        executor.execute(() -> {
            Bitmap thumbSource = createDownscaledCopy(source, THUMBNAIL_MAX_DIMENSION);
            boolean sourceIsCopy = thumbSource != source;
            List<FilterThumbnail> thumbnails = new ArrayList<>(availableFilters.size());
            for (FilterItem item : availableFilters) {
                Bitmap result = item.getFilter().apply(thumbSource);
                thumbnails.add(new FilterThumbnail(item, result));
            }
            if (sourceIsCopy) {
                thumbSource.recycle();
            }
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    recycleThumbnails(thumbnails);
                    return;
                }
                currentThumbnails = thumbnails;
                view.showFilterThumbnails(thumbnails);
            });
        });
    }

    @Override
    public void onFilterSelected(FilterItem filterItem) {
        final Bitmap source = originalBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        executor.execute(() -> {
            Bitmap result = filterItem.getFilter().apply(source);
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                Bitmap old = currentFilteredBitmap;
                currentFilteredBitmap = result;
                lastSavedUri = null;
                view.showFilteredImage(result);
                recycleIfPossible(old);
            });
        });
    }

    @Override
    public void onSaveClicked() {
        performSave(uri -> {
            if (view != null) {
                view.showSaveResult(true, uri);
            }
        }, message -> {
            if (view != null) {
                view.showSaveResult(false, null);
                view.showError(message);
            }
        });
    }

    @Override
    public void onShareClicked() {
        performSave(uri -> {
            if (view != null) {
                view.launchShareIntent(uri);
            }
        }, message -> {
            if (view != null) {
                view.showError(message);
            }
        });
    }

    private void performSave(OnSaved onSaved, OnSaveFailed onFailed) {
        Bitmap toSave = currentFilteredBitmap != null ? currentFilteredBitmap : originalBitmap;
        if (toSave == null) {
            return;
        }
        if (lastSavedUri != null) {
            onSaved.run(lastSavedUri);
            return;
        }
        executor.execute(() -> {
            try {
                String name = "PhotoFilter_" + System.currentTimeMillis() + ".jpg";
                Uri uri = imageRepository.saveToGallery(appContext, toSave, name);
                mainHandler.post(() -> {
                    lastSavedUri = uri;
                    onSaved.run(uri);
                });
            } catch (IOException e) {
                mainHandler.post(() -> onFailed.run(appContext.getString(R.string.error_save_image)));
            }
        });
    }

    private static Bitmap createDownscaledCopy(Bitmap source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scale = Math.min(1f, (float) maxDimension / Math.max(width, height));
        if (scale >= 1f) {
            return source;
        }
        int newWidth = Math.max(1, Math.round(width * scale));
        int newHeight = Math.max(1, Math.round(height * scale));
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    private static void recycleIfPossible(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private static void recycleThumbnails(List<FilterThumbnail> thumbnails) {
        if (thumbnails == null) {
            return;
        }
        for (FilterThumbnail thumbnail : thumbnails) {
            recycleIfPossible(thumbnail.getThumbnail());
        }
    }

    private interface OnSaved {
        void run(Uri uri);
    }

    private interface OnSaveFailed {
        void run(String message);
    }
}
