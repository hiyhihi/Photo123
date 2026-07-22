package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** 70s-style retro look: boosted saturation, warm/magenta shift, reduced blue channel. */
public class RetroFilter extends BaseFilter {

    private static final float[] RETRO_MATRIX = {
            1.1f, 0.05f, 0f, 0f, 10f,
            0f, 0.95f, 0f, 0f, 0f,
            0.05f, 0f, 0.8f, 0f, 15f,
            0f, 0f, 0f, 1f, 0f
    };

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(1.1f);
        matrix.postConcat(new ColorMatrix(RETRO_MATRIX));
        return applyColorMatrix(source, matrix);
    }
}
