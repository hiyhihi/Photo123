package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Passthrough filter ("Color" / ảnh gốc) — identity color matrix. */
public class OriginalFilter extends BaseFilter {

    @Override
    protected Bitmap process(Bitmap source) {
        return applyColorMatrix(source, new ColorMatrix());
    }
}
