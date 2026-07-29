package com.example.photofilter.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.photofilter.R;
import com.example.photofilter.data.AiToolsRepository;
import com.example.photofilter.data.BackgroundRemovalRepository;
import com.example.photofilter.data.CropRatio;
import com.example.photofilter.data.CropUtils;
import com.example.photofilter.data.FavoriteRepository;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.data.FilterRepository;
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
    private final AiToolsRepository aiToolsRepository = new AiToolsRepository();
    private final BackgroundRemovalRepository backgroundRemovalRepository = new BackgroundRemovalRepository();
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final EditHistory history = new EditHistory();

    private EditorContract.View view;
    private List<FilterItem> availableFilters;
    private List<FilterThumbnail> currentThumbnails;
    private Set<String> favoriteIds;
    private Uri lastSavedUri;

    /** The image + label a tool tab started editing from; recomputed by non-cumulative tools (Bộ lọc/Tuỳ chỉnh). */
    private Bitmap draftBaseBitmap;
    /** The live preview; recomputed by cumulative tools (Cắt/AI) on top of itself, and shown to the View. */
    private Bitmap draftBitmap;
    private String draftLabel;

    /** Bumped on every new user action; stale async results compare against it and are discarded. */
    private int requestId;

    /**
     * Counts background ops currently reading {@code draftBitmap}/{@code draftBaseBitmap}
     * (dispatched from {@link #onFilterSelected}, {@link #onAdjustValuesChanged},
     * {@link #applyGeometryOpFrom}, {@link #applyAiToolDraft}). While positive, {@link #clearDraft()}
     * must not recycle {@code draftBitmap}: a Cancel/tab-switch can now run synchronously on the
     * main thread while one of those ops is still mid-computation on the executor thread with
     * that exact Bitmap as its source, and recycling out from under it crashes.
     */
    private int pendingOps;

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
        if (history.current() != null) {
            view.showImage(history.current());
            view.showUndoRedoAvailability(history.canUndo(), history.canRedo());
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
        clearDraft();
        history.clearAll();
        recycleThumbnails(currentThumbnails);
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
                    clearDraft();
                    history.reset(loaded, appContext.getString(R.string.filter_original));
                    view.showLoading(false);
                    afterHistoryChange();
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
    public void onToolTabOpened() {
        // Defensive: cleans up any draft left behind by a path that bypassed an explicit
        // Cancel (e.g. the user drags the bottom sheet closed instead of tapping Cancel/a
        // nav button) so a leftover draftBitmap is never silently overwritten/orphaned.
        clearDraft();
        if (history.current() == null) {
            return;
        }
        draftBaseBitmap = history.current();
        draftBitmap = draftBaseBitmap;
        draftLabel = history.currentLabel();
    }

    @Override
    public void onApplyRequested() {
        if (draftBitmap == null || draftBitmap == draftBaseBitmap) {
            onCancelRequested();
            return;
        }
        Bitmap committed = draftBitmap;
        String label = draftLabel != null ? draftLabel : history.currentLabel();
        draftBitmap = null;
        draftBaseBitmap = null;
        draftLabel = null;
        history.commit(committed, label);
        afterHistoryChange();
    }

    @Override
    public void onCancelRequested() {
        ++requestId; // discard any in-flight draft computation
        clearDraft();
        if (view != null) {
            view.showImage(history.current());
        }
    }

    @Override
    public void onUndoRequested() {
        if (!history.canUndo()) {
            return;
        }
        history.undo();
        afterHistoryChange();
    }

    @Override
    public void onRedoRequested() {
        if (!history.canRedo()) {
            return;
        }
        history.redo();
        afterHistoryChange();
    }

    /** Common tail for anything that changes the committed image: image pick, Apply, Undo, Redo. */
    private void afterHistoryChange() {
        lastSavedUri = null;
        if (view != null) {
            view.showImage(history.current());
            view.showUndoRedoAvailability(history.canUndo(), history.canRedo());
        }
        final int myRequestId = ++requestId;
        generateThumbnails(myRequestId, history.current());
    }

    private void clearDraft() {
        // If a background op is still reading draftBitmap as its source (pendingOps > 0), leave
        // it unrecycled rather than racing the executor thread; it becomes unreachable garbage
        // for the JVM but is never actively read/mutated as a Bitmap after this point, since its
        // eventual mainHandler callback will find requestId stale and just recycle its own result.
        if (draftBitmap != null && draftBitmap != draftBaseBitmap && pendingOps == 0) {
            recycleIfPossible(draftBitmap);
        }
        draftBitmap = null;
        draftBaseBitmap = null;
        draftLabel = null;
    }

    /** Replaces the draft bitmap, recycling the previous one unless it's still `draftBaseBitmap`/history-owned. */
    private void updateDraft(Bitmap newDraft, String label) {
        if (draftBitmap != null && draftBitmap != draftBaseBitmap) {
            recycleIfPossible(draftBitmap);
        }
        draftBitmap = newDraft;
        draftLabel = label;
    }

    @Override
    public void onFilterSelected(FilterItem filterItem) {
        final Bitmap source = draftBaseBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        pendingOps++;
        executor.execute(() -> {
            Bitmap result = filterItem.getFilter().apply(source);
            mainHandler.post(() -> {
                pendingOps--;
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                updateDraft(result, filterItem.getDisplayName());
                view.showImage(result);
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
        final Bitmap source = draftBaseBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        pendingOps++;
        executor.execute(() -> {
            Bitmap result = new ColorAdjustFilter(brightness, contrast, saturation, hue, exposure).apply(source);
            mainHandler.post(() -> {
                pendingOps--;
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                updateDraft(result, appContext.getString(R.string.filter_adjust));
                view.showImage(result);
            });
        });
    }

    @Override
    public void onRotateRequested() {
        applyGeometryDraftOp(CropUtils::rotate90);
    }

    @Override
    public void onFlipRequested() {
        applyGeometryDraftOp(CropUtils::flipHorizontal);
    }

    @Override
    public void onCropRequested(CropRatio ratio) {
        if (ratio == CropRatio.ORIGINAL) {
            // Bypasses the cumulative draft chain on purpose: "Original" always means
            // the true pristine image, not "undo my last crop within this session".
            // Deliberately uses Bitmap.copy(), NOT CropUtils.centerCrop()/Bitmap.createBitmap():
            // AOSP's Bitmap.createBitmap(Bitmap, x, y, w, h) has a documented fast path that
            // returns the SAME object (no copy) when the source is immutable and the full
            // width/height is requested with no matrix — exactly the case here, since
            // ImageRepository.loadDownsampled decodes via BitmapFactory without inMutable.
            // That would alias history.pristineOriginal() straight into the draft, and a
            // later Cancel/clearDraft() or an EditHistory.commit()-triggered redo-stack clear
            // would recycle it, leaving pristineOriginal() a permanently dangling recycled
            // Bitmap for the rest of the session. Bitmap.copy() always allocates a new object.
            applyGeometryOpFrom(history.pristineOriginal(),
                    source -> source.copy(source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888, false));
            return;
        }
        applyGeometryDraftOp(source -> CropUtils.centerCrop(source, ratio));
    }

    @Override
    public void onCustomCropRequested(RectF normalizedRect) {
        applyGeometryDraftOp(source -> CropUtils.customCrop(source, normalizedRect));
    }

    @Override
    public void onResizeRequested(int scalePercent) {
        if (scalePercent == 100) {
            return;
        }
        applyGeometryDraftOp(source -> CropUtils.resize(source, scalePercent));
    }

    /** Cumulative geometry op: reads and replaces `draftBitmap` (Cắt tab keeps stacking Rotate/Flip/Crop/Resize). */
    private void applyGeometryDraftOp(GeometryOp op) {
        applyGeometryOpFrom(draftBitmap, op);
    }

    private void applyGeometryOpFrom(Bitmap source, GeometryOp op) {
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        pendingOps++;
        executor.execute(() -> {
            Bitmap result = op.apply(source);
            mainHandler.post(() -> {
                pendingOps--;
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                updateDraft(result, draftLabel);
                view.showImage(result);
            });
        });
    }

    private interface GeometryOp {
        Bitmap apply(Bitmap source);
    }

    @Override
    public void onSharpenRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_sharpen), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::sharpen);
    }

    @Override
    public void onRemoveNoiseRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_remove_noise), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::removeNoise);
    }

    @Override
    public void onUpscaleRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_upscale), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::upscale);
    }

    @Override
    public void onBackgroundRemovalRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_background_removal), appContext.getString(R.string.error_ai_tool),
                backgroundRemovalRepository::removeBackground);
    }

    /** Cumulative AI op: reads and replaces `draftBitmap`, so e.g. Sharpen then Upscale can stack before one Apply. */
    private void applyAiToolDraft(String resultLabel, String errorMessage, BitmapOp op) {
        final Bitmap source = draftBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        if (view != null) {
            view.showLoading(true);
        }
        pendingOps++;
        executor.execute(() -> {
            try {
                Bitmap result = op.apply(source);
                mainHandler.post(() -> {
                    pendingOps--;
                    if (myRequestId != requestId || view == null) {
                        result.recycle();
                        return;
                    }
                    updateDraft(result, resultLabel);
                    view.showLoading(false);
                    view.showImage(result);
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    pendingOps--;
                    if (myRequestId != requestId || view == null) {
                        return;
                    }
                    view.showLoading(false);
                    String detail = e.getMessage();
                    view.showError(detail != null && !detail.isEmpty() ? errorMessage + ": " + detail : errorMessage);
                });
            }
        });
    }

    private interface BitmapOp {
        Bitmap apply(Bitmap source) throws IOException;
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
        Bitmap toSave = history.current();
        if (toSave == null) {
            return;
        }
        if (lastSavedUri != null) {
            onSaved.run(lastSavedUri);
            return;
        }
        String filterName = history.currentLabel();
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
