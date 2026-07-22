package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Classic sepia (vintage brown) tone. */
public class SepiaFilter extends BaseFilter {

    private static final float[] SEPIA_MATRIX = {
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
    };

    @Override
    protected Bitmap process(Bitmap source) {
        return applyColorMatrix(source, new ColorMatrix(SEPIA_MATRIX));
    }
}
