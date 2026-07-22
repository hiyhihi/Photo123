package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** High-contrast monochrome with a faint cool tint — distinct from the plain GrayscaleFilter. */
public class MonoFilter extends BaseFilter {

    private static final float[] MONO_TINT_CONTRAST = {
            1.15f, 0f, 0f, 0f, -10f,
            0f, 1.15f, 0f, 0f, -8f,
            0f, 0f, 1.18f, 0f, 2f,
            0f, 0f, 0f, 1f, 0f
    };

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        matrix.postConcat(new ColorMatrix(MONO_TINT_CONTRAST));
        return applyColorMatrix(source, matrix);
    }
}
