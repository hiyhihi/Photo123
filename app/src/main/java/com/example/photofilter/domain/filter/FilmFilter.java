package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Filmic look: lifted shadows, reduced contrast, slight desaturation and a soft green-yellow cast. */
public class FilmFilter extends BaseFilter {

    private static final float[] FILM_MATRIX = {
            0.92f, 0f, 0f, 0f, 12f,
            0f, 0.94f, 0f, 0f, 14f,
            0f, 0f, 0.90f, 0f, 6f,
            0f, 0f, 0f, 1f, 0f
    };

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.85f);
        matrix.postConcat(new ColorMatrix(FILM_MATRIX));
        return applyColorMatrix(source, matrix);
    }
}
