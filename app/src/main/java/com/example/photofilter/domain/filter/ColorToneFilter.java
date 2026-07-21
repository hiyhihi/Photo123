package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;

/** Shifts the red/blue balance to give a warm or cool tint. */
public class ColorToneFilter extends BaseFilter {

    public enum Tone { WARM, COOL }

    private static final float OFFSET = 30f;

    private final Tone tone;

    public ColorToneFilter(Tone tone) {
        this.tone = tone;
    }

    @Override
    protected Bitmap process(Bitmap source) {
        float redOffset = tone == Tone.WARM ? OFFSET : -OFFSET;
        float blueOffset = tone == Tone.WARM ? -OFFSET : OFFSET;
        float[] matrix = {
                1f, 0f, 0f, 0f, redOffset,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, blueOffset,
                0f, 0f, 0f, 1f, 0f
        };
        return applyColorMatrix(source, new ColorMatrix(matrix));
    }
}
