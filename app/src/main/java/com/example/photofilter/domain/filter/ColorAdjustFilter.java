package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/**
 * Interactive brightness/contrast/saturation adjustment, driven by the
 * SeekBar panel in the editor. All three parameters use a 0-200 "percent"
 * scale where 100 means "no change" (matches how a slider centered at 100
 * reads to a user), except brightness which is -100..100.
 */
public class ColorAdjustFilter extends BaseFilter {

    private final float brightness;
    private final float contrast;
    private final float saturation;

    public ColorAdjustFilter(float brightness, float contrast, float saturation) {
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
    }

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturation / 100f);

        float contrastScale = contrast / 100f;
        float brightnessOffset = brightness / 100f * 255f;
        float translate = brightnessOffset + (1f - contrastScale) * 127.5f;
        float[] brightnessContrast = {
                contrastScale, 0f, 0f, 0f, translate,
                0f, contrastScale, 0f, 0f, translate,
                0f, 0f, contrastScale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
        };
        matrix.postConcat(new ColorMatrix(brightnessContrast));

        return applyColorMatrix(source, matrix);
    }
}
