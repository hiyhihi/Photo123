package com.example.photofilter.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.photofilter.data.FilterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Records every call the presenter makes so tests can assert on interaction order. */
final class FakeView implements EditorContract.View {

    final List<List<FilterItem>> filterLists = new ArrayList<>();
    final List<Bitmap> images = new ArrayList<>();
    final List<List<FilterThumbnail>> thumbnailBatches = new ArrayList<>();
    final List<Boolean> loadingStates = new ArrayList<>();
    final List<String> errors = new ArrayList<>();
    final List<Boolean> undoAvailableStates = new ArrayList<>();
    final List<Boolean> redoAvailableStates = new ArrayList<>();
    Boolean lastSaveSuccess;
    Uri lastSavedUri;
    Uri lastSharedUri;
    Set<String> lastFavoriteIds;

    @Override
    public void showFilterList(List<FilterItem> filters) {
        filterLists.add(filters);
    }

    @Override
    public void showImage(Bitmap bitmap) {
        images.add(bitmap);
    }

    @Override
    public void showFilterThumbnails(List<FilterThumbnail> thumbnails) {
        thumbnailBatches.add(thumbnails);
    }

    @Override
    public void showLoading(boolean loading) {
        loadingStates.add(loading);
    }

    @Override
    public void showError(String message) {
        errors.add(message);
    }

    @Override
    public void showSaveResult(boolean success, Uri savedUri) {
        lastSaveSuccess = success;
        lastSavedUri = savedUri;
    }

    @Override
    public void showUndoRedoAvailability(boolean canUndo, boolean canRedo) {
        undoAvailableStates.add(canUndo);
        redoAvailableStates.add(canRedo);
    }

    @Override
    public void launchShareIntent(Uri uri) {
        lastSharedUri = uri;
    }

    @Override
    public void showFavoriteIds(Set<String> favoriteFilterIds) {
        lastFavoriteIds = favoriteFilterIds;
    }
}
