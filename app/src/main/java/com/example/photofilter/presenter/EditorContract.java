package com.example.photofilter.presenter;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;

import com.example.photofilter.data.CropRatio;
import com.example.photofilter.data.FilterItem;

import java.util.List;
import java.util.Set;

/**
 * MVP contract between {@code MainActivity} (View) and {@link EditorPresenter}.
 * The View never touches {@code Filter}/{@code ImageRepository} directly;
 * the Presenter never touches Android UI widgets directly.
 */
public interface EditorContract {

    interface View {
        void showFilterList(List<FilterItem> filters);

        void showImage(Bitmap bitmap);

        void showFilterThumbnails(List<FilterThumbnail> thumbnails);

        void showFavoriteIds(Set<String> favoriteFilterIds);

        void showLoading(boolean loading);

        void showError(String message);

        void showSaveResult(boolean success, Uri savedUri);

        void showUndoRedoAvailability(boolean canUndo, boolean canRedo);

        void launchShareIntent(Uri uri);
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void onImagePicked(Uri imageUri, int viewportWidth, int viewportHeight);

        /** Synchronous: creates the MediaStore target the camera app should write into. Returns null on failure. */
        Uri createCameraOutputUri();

        /** Call when a tool tab (Bộ lọc/Cắt/Tuỳ chỉnh/AI) becomes visible: starts a new draft from the current committed image. */
        void onToolTabOpened();

        /** Commits the current draft as one Undo step. No-op (same as Cancel) if the draft is unchanged. */
        void onApplyRequested();

        /** Discards the current draft; the displayed image reverts to the last committed state. */
        void onCancelRequested();

        void onUndoRequested();

        void onRedoRequested();

        void onFilterSelected(FilterItem filterItem);

        void onFavoriteToggled(FilterItem filterItem);

        void onAdjustValuesChanged(int brightness, int contrast, int saturation, int hue, int exposure);

        void onRotateRequested();

        void onFlipRequested();

        void onCropRequested(CropRatio ratio);

        /** @param normalizedRect free-form crop rect as fractions (0..1) of the image, from the drag-handle overlay. */
        void onCustomCropRequested(RectF normalizedRect);

        void onSharpenRequested();

        void onRemoveNoiseRequested();

        void onUpscaleRequested();

        void onBackgroundRemovalRequested();

        /**
         * Composites {@code stickerBitmap} onto the current committed image and commits the
         * result as one Undo step. All 4 placement params are normalized (0..1) relative to the
         * base image's own width/height, not screen pixels — the caller maps its on-screen
         * overlay into this space before calling.
         * @param centerXFraction horizontal center of the sticker, as a fraction of image width
         * @param centerYFraction vertical center of the sticker, as a fraction of image height
         * @param scaleFraction sticker's rendered width, as a fraction of image width
         * @param rotationDegrees clockwise rotation in degrees
         */
        void onStickerApplyRequested(Bitmap stickerBitmap, float centerXFraction, float centerYFraction,
                                      float scaleFraction, float rotationDegrees);

        void onSaveClicked();

        void onShareClicked();
    }
}
