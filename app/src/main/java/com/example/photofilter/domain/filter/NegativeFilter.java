package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Inverts every color channel (255 - channel), alpha untouched. */
public class NegativeFilter extends BaseFilter {

    private static final float[] NEGATIVE_MATRIX = {
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
    };

    @Override
    protected Bitmap process(Bitmap source) {
        return applyColorMatrix(source, new ColorMatrix(NEGATIVE_MATRIX));
    }
}
