package com.example.photofilter.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.photofilter.data.FilterItem;

import java.util.List;

/**
 * MVP contract between {@code MainActivity} (View) and {@link EditorPresenter}.
 * The View never touches {@code Filter}/{@code ImageRepository} directly;
 * the Presenter never touches Android UI widgets directly.
 */
public interface EditorContract {

    interface View {
        void showFilterList(List<FilterItem> filters);

        void showOriginalImage(Bitmap bitmap);

        void showFilteredImage(Bitmap bitmap);

        void showFilterThumbnails(List<FilterThumbnail> thumbnails);

        void showLoading(boolean loading);

        void showError(String message);

        void showSaveResult(boolean success, Uri savedUri);

        void launchShareIntent(Uri uri);
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void onImagePicked(Uri imageUri, int viewportWidth, int viewportHeight);

        void onFilterSelected(FilterItem filterItem);

        void onSaveClicked();

        void onShareClicked();
    }
}
