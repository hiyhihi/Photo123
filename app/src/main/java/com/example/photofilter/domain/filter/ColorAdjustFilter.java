package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/**
 * Interactive brightness/contrast/saturation/hue/exposure adjustment, driven by
 * the Adjust tool sheet in the editor. Brightness, hue and exposure use a
 * -100..100 scale where 0 means "no change"; contrast and saturation use a
 * 0-200 "percent" scale where 100 means "no change" (matches how a slider
 * centered at 100 reads to a user).
 */
public class ColorAdjustFilter extends BaseFilter {

    private static final float LUM_R = 0.213f;
    private static final float LUM_G = 0.715f;
    private static final float LUM_B = 0.072f;

    private final float brightness;
    private final float contrast;
    private final float saturation;
    private final float hue;
    private final float exposure;

    public ColorAdjustFilter(float brightness, float contrast, float saturation, float hue, float exposure) {
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
        this.hue = hue;
        this.exposure = exposure;
    }

    @Override
    protected Bitmap process(Bitmap source) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturation / 100f);

        if (hue != 0f) {
            matrix.postConcat(hueRotationMatrix(hue));
        }

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

        if (exposure != 0f) {
            float exposureScale = (float) Math.pow(2f, exposure / 100f);
            float[] exposureMatrix = {
                    exposureScale, 0f, 0f, 0f, 0f,
                    0f, exposureScale, 0f, 0f, 0f,
                    0f, 0f, exposureScale, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
            };
            matrix.postConcat(new ColorMatrix(exposureMatrix));
        }

        return applyColorMatrix(source, matrix);
    }

    /** Rotates hue by {@code degrees} while preserving perceived luminance. */
    private static ColorMatrix hueRotationMatrix(float degrees) {
        float radians = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        float[] m = {
                LUM_R + cos * (1 - LUM_R) - sin * LUM_R, LUM_G - cos * LUM_G - sin * LUM_G, LUM_B - cos * LUM_B + sin * (1 - LUM_B), 0f, 0f,
                LUM_R - cos * LUM_R + sin * 0.143f, LUM_G + cos * (1 - LUM_G) + sin * 0.140f, LUM_B - cos * LUM_B - sin * 0.283f, 0f, 0f,
                LUM_R - cos * LUM_R - sin * (1 - LUM_R), LUM_G - cos * LUM_G + sin * LUM_G, LUM_B + cos * (1 - LUM_B) + sin * LUM_B, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        };
        return new ColorMatrix(m);
    }
}
