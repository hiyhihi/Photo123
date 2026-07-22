package com.example.photofilter.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.photofilter.data.FilterItem;

import java.util.List;
import java.util.Set;

/**
 * MVP contract between {@code MainActivity} (View) and {@link EditorPresenter}.
 * The View never touches {@code Filter}/{@code ImageRepository} directly;
 * the Presenter never touches Android UI widgets directly.
 */
public interface EditorContract {

    /** Fixed aspect ratios offered by the crop dialog. */
    enum CropRatio { ORIGINAL, SQUARE, FOUR_THREE, SIXTEEN_NINE }

    interface View {
        void showFilterList(List<FilterItem> filters);

        void showOriginalImage(Bitmap bitmap);

        void showFilteredImage(Bitmap bitmap);

        void showFilterThumbnails(List<FilterThumbnail> thumbnails);

        void showFavoriteIds(Set<String> favoriteFilterIds);

        void showLoading(boolean loading);

        void showError(String message);

        void showSaveResult(boolean success, Uri savedUri);

        void launchShareIntent(Uri uri);
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void onImagePicked(Uri imageUri, int viewportWidth, int viewportHeight);

        /** Synchronous: creates the MediaStore target the camera app should write into. Returns null on failure. */
        Uri createCameraOutputUri();

        void onFilterSelected(FilterItem filterItem);

        void onFavoriteToggled(FilterItem filterItem);

        void onAdjustValuesChanged(int brightness, int contrast, int saturation);

        void onRotateRequested();

        void onCropRequested(CropRatio ratio);

        void onAiEnhanceRequested();

        void onSaveClicked();

        void onShareClicked();
    }
}
