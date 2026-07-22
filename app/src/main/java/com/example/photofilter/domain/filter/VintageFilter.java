package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Faded, warm-toned look: reduced saturation + lifted shadows + warm shift. */
public class VintageFilter extends BaseFilter {

    private static final float[] WARM_FADE_MATRIX = {
            0.9f, 0f, 0f, 0f, 20f,
            0f, 0.88f, 0f, 0f, 10f,
            0f, 0f, 0.8f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
    };

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.7f);
        matrix.postConcat(new ColorMatrix(WARM_FADE_MATRIX));
        return applyColorMatrix(source, matrix);
    }
}
