package com.example.photofilter.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.photofilter.BuildConfig;
import com.example.photofilter.R;
import com.example.photofilter.data.AiToolsRepository;
import com.example.photofilter.data.BackgroundRemovalRepository;
import com.example.photofilter.data.CropRatio;
import com.example.photofilter.data.CropUtils;
import com.example.photofilter.data.FavoriteRepository;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.data.FilterRepository;
import com.example.photofilter.data.GeminiEnhanceRepository;
import com.example.photofilter.data.HistoryRepository;
import com.example.photofilter.data.ImageRepository;
import com.example.photofilter.domain.filter.ColorAdjustFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorPresenter implements EditorContract.Presenter {

    private static final int THUMBNAIL_MAX_DIMENSION = 200;

    private final Context appContext;
    private final ImageRepository imageRepository;
    private final FilterRepository filterRepository;
    private final HistoryRepository historyRepository;
    private final FavoriteRepository favoriteRepository;
    private final GeminiEnhanceRepository aiEnhanceRepository = new GeminiEnhanceRepository();
    private final AiToolsRepository aiToolsRepository = new AiToolsRepository();
    private final BackgroundRemovalRepository backgroundRemovalRepository = new BackgroundRemovalRepository();
    private final ExecutorService executor;
    private final Handler mainHandler;

    private EditorContract.View view;
    private List<FilterItem> availableFilters;
    private List<FilterThumbnail> currentThumbnails;
    private Set<String> favoriteIds;
    private Bitmap originalBitmap;
    private Bitmap currentFilteredBitmap;
    private String currentFilterLabel;
    private Uri lastSavedUri;

    /** Bumped on every new user action; stale async results compare against it and are discarded. */
    private int requestId;

    public EditorPresenter(Context context) {
        this(context, Executors.newSingleThreadExecutor(), new ImageRepository(),
                new HistoryRepository(context), new FavoriteRepository(context));
    }

    /** Visible for testing: lets tests inject a same-thread executor and fake repositories for deterministic results. */
    EditorPresenter(Context context, ExecutorService executor, ImageRepository imageRepository,
                     HistoryRepository historyRepository, FavoriteRepository favoriteRepository) {
        this.appContext = context.getApplicationContext();
        this.imageRepository = imageRepository;
        this.filterRepository = new FilterRepository();
        this.historyRepository = historyRepository;
        this.favoriteRepository = favoriteRepository;
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
        if (favoriteIds != null) {
            view.showFavoriteIds(favoriteIds);
        } else {
            executor.execute(() -> {
                Set<String> loaded = favoriteRepository.getFavoriteIds();
                mainHandler.post(() -> {
                    favoriteIds = loaded;
                    if (view != null) {
                        view.showFavoriteIds(favoriteIds);
                    }
                });
            });
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
    public Uri createCameraOutputUri() {
        try {
            return imageRepository.createCameraOutputUri(appContext);
        } catch (IOException e) {
            if (view != null) {
                view.showError(appContext.getString(R.string.error_camera));
            }
            return null;
        }
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
                    replaceOriginalBitmap(loaded);
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
                currentFilterLabel = filterItem.getDisplayName();
                lastSavedUri = null;
                view.showFilteredImage(result);
                recycleIfPossible(old);
            });
        });
    }

    @Override
    public void onFavoriteToggled(FilterItem filterItem) {
        executor.execute(() -> {
            boolean nowFavorite = favoriteRepository.toggleFavorite(filterItem.getId());
            mainHandler.post(() -> {
                if (favoriteIds == null) {
                    favoriteIds = new HashSet<>();
                }
                if (nowFavorite) {
                    favoriteIds.add(filterItem.getId());
                } else {
                    favoriteIds.remove(filterItem.getId());
                }
                if (view != null) {
                    view.showFavoriteIds(favoriteIds);
                }
            });
        });
    }

    @Override
    public void onAdjustValuesChanged(int brightness, int contrast, int saturation, int hue, int exposure) {
        final Bitmap source = originalBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        executor.execute(() -> {
            Bitmap result = new ColorAdjustFilter(brightness, contrast, saturation, hue, exposure).apply(source);
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                Bitmap old = currentFilteredBitmap;
                currentFilteredBitmap = result;
                currentFilterLabel = appContext.getString(R.string.filter_adjust);
                lastSavedUri = null;
                view.showFilteredImage(result);
                recycleIfPossible(old);
            });
        });
    }

    @Override
    public void onRotateRequested() {
        applyGeometryChange(CropUtils::rotate90);
    }

    @Override
    public void onFlipRequested() {
        applyGeometryChange(CropUtils::flipHorizontal);
    }

    @Override
    public void onCropRequested(CropRatio ratio) {
        if (ratio == CropRatio.ORIGINAL) {
            return;
        }
        applyGeometryChange(source -> CropUtils.centerCrop(source, ratio));
    }

    @Override
    public void onResizeRequested(int scalePercent) {
        if (scalePercent == 100) {
            return;
        }
        applyGeometryChange(source -> CropUtils.resize(source, scalePercent));
    }

    /** Shared plumbing for rotate/flip/crop: all replace originalBitmap and regenerate thumbnails the same way. */
    private void applyGeometryChange(GeometryOp op) {
        final Bitmap source = originalBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        executor.execute(() -> {
            Bitmap result = op.apply(source);
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                replaceOriginalBitmap(result);
                lastSavedUri = null;
                view.showOriginalImage(result);
                generateThumbnails(myRequestId, result);
            });
        });
    }

    private interface GeometryOp {
        Bitmap apply(Bitmap source);
    }

    @Override
    public void onAiEnhanceRequested() {
        applyAiTool(appContext.getString(R.string.action_ai_enhance), appContext.getString(R.string.error_ai_enhance),
                source -> aiEnhanceRepository.enhance(source, BuildConfig.GEMINI_API_KEY));
    }

    @Override
    public void onSharpenRequested() {
        applyAiTool(appContext.getString(R.string.ai_tool_sharpen), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::sharpen);
    }

    @Override
    public void onRemoveNoiseRequested() {
        applyAiTool(appContext.getString(R.string.ai_tool_remove_noise), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::removeNoise);
    }

    @Override
    public void onUpscaleRequested() {
        applyAiTool(appContext.getString(R.string.ai_tool_upscale), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::upscale);
    }

    @Override
    public void onBackgroundRemovalRequested() {
        applyAiTool(appContext.getString(R.string.ai_tool_background_removal), appContext.getString(R.string.error_ai_tool),
                backgroundRemovalRepository::removeBackground);
    }

    /** Shared plumbing for every "AI tool": takes the currently displayed bitmap, runs {@code op} in the background. */
    private void applyAiTool(String resultLabel, String errorMessage, BitmapOp op) {
        final Bitmap source = currentFilteredBitmap != null ? currentFilteredBitmap : originalBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        if (view != null) {
            view.showLoading(true);
        }
        executor.execute(() -> {
            try {
                Bitmap result = op.apply(source);
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        result.recycle();
                        return;
                    }
                    Bitmap old = currentFilteredBitmap;
                    currentFilteredBitmap = result;
                    currentFilterLabel = resultLabel;
                    lastSavedUri = null;
                    view.showLoading(false);
                    view.showFilteredImage(result);
                    recycleIfPossible(old);
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        return;
                    }
                    view.showLoading(false);
                    view.showError(errorMessage);
                });
            }
        });
    }

    private interface BitmapOp {
        Bitmap apply(Bitmap source) throws IOException;
    }

    /** Swaps in a new original bitmap (new image picked, rotated, or cropped), recycling whatever came before. */
    private void replaceOriginalBitmap(Bitmap replacement) {
        recycleIfPossible(originalBitmap);
        recycleIfPossible(currentFilteredBitmap);
        recycleThumbnails(currentThumbnails);
        originalBitmap = replacement;
        currentFilteredBitmap = null;
        currentThumbnails = null;
        currentFilterLabel = null;
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
        String filterName = currentFilterLabel != null
                ? currentFilterLabel
                : appContext.getString(R.string.filter_original);
        executor.execute(() -> {
            try {
                String name = "PhotoFilter_" + System.currentTimeMillis() + ".jpg";
                Uri uri = imageRepository.saveToGallery(appContext, toSave, name);
                historyRepository.insert(filterName, uri.toString(), System.currentTimeMillis());
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
