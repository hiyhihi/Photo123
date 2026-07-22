package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/**
 * Adjustable brightness/contrast filter.
 *
 * @param brightness additive offset per channel, roughly -255..255
 * @param contrast   scale factor around the midpoint gray (127.5); 1f = no change
 */
public class BrightnessContrastFilter extends BaseFilter {

    private final float brightness;
    private final float contrast;

    public BrightnessContrastFilter(float brightness, float contrast) {
        this.brightness = brightness;
        this.contrast = contrast;
    }

    @Override
    protected Bitmap process(Bitmap source) {
        float translate = brightness + (1f - contrast) * 127.5f;
        float[] matrix = {
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
        };
        return applyColorMatrix(source, new ColorMatrix(matrix));
    }
}
