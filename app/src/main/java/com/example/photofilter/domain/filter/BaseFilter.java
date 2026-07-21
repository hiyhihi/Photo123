package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * Template method base class shared by every {@link Filter} implementation.
 * {@link #apply(Bitmap)} validates the input once, then delegates the actual
 * transform to {@link #process(Bitmap)}. Subclasses that only need a color
 * matrix transform can call {@link #applyColorMatrix(Bitmap, ColorMatrix)}
 * instead of hand-rolling the Bitmap/Canvas/Paint boilerplate.
 */
public abstract class BaseFilter implements Filter {

    @Override
    public final Bitmap apply(Bitmap source) {
        if (source == null || source.isRecycled()) {
            throw new IllegalArgumentException("Source bitmap không hợp lệ hoặc đã bị recycle");
        }
        return process(source);
    }

    protected abstract Bitmap process(Bitmap source);

    protected final Bitmap applyColorMatrix(Bitmap source, ColorMatrix matrix) {
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), config);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(source, 0, 0, paint);
        return output;
    }
}
