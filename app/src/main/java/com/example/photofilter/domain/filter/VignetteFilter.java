package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Darkens the corners with a radial gradient. Unlike the other filters this
 * is position-dependent, so it draws directly instead of going through
 * {@link #applyColorMatrix}.
 */
public class VignetteFilter extends BaseFilter {

    @Override
    protected Bitmap process(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap output = Bitmap.createBitmap(width, height, config);

        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(source, 0, 0, null);

        float radius = Math.max(width, height) * 0.75f;
        RadialGradient gradient = new RadialGradient(
                width / 2f, height / 2f, radius,
                new int[]{0x00000000, 0x00000000, 0x99000000},
                new float[]{0f, 0.55f, 1f},
                Shader.TileMode.CLAMP);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(gradient);
        canvas.drawRect(0f, 0f, width, height, paint);

        return output;
    }
}
