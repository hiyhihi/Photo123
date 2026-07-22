package com.example.photofilter.presenter;

import android.graphics.Bitmap;

import com.example.photofilter.data.FilterItem;

/** View-model pairing a {@link FilterItem} with its small preview bitmap. */
public final class FilterThumbnail {

    private final FilterItem filterItem;
    private final Bitmap thumbnail;

    public FilterThumbnail(FilterItem filterItem, Bitmap thumbnail) {
        this.filterItem = filterItem;
        this.thumbnail = thumbnail;
    }

    public FilterItem getFilterItem() {
        return filterItem;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}
