package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;

/**
 * Contract for a single image filter. Implementations must be pure functions
 * of the input bitmap: no side effects, no UI/Android framework dependency
 * beyond {@link android.graphics}.
 */
public interface Filter {

    /**
     * Applies the filter to {@code source} and returns a new bitmap.
     * {@code source} is never mutated or recycled by an implementation.
     */
    Bitmap apply(Bitmap source);
}
