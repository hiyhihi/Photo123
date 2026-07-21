package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Black & white filter — desaturates the image completely. */
public class GrayscaleFilter extends BaseFilter {

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        return applyColorMatrix(source, matrix);
    }
}
