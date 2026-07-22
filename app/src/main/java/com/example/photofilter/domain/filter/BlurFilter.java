package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;

/**
 * Light Gaussian-ish blur via a two-pass box blur directly on the pixel
 * array (getPixels/setPixels) — a position-dependent operation ColorMatrix
 * cannot express.
 */
public class BlurFilter extends BaseFilter {

    private static final int RADIUS = 6;

    @Override
    protected Bitmap process(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] horizontal = boxBlurPass(pixels, width, height, RADIUS, true);
        int[] result = boxBlurPass(horizontal, width, height, RADIUS, false);

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        output.setPixels(result, 0, width, 0, 0, width, height);
        return output;
    }

    private static int[] boxBlurPass(int[] pixels, int width, int height, int radius, boolean horizontal) {
        int[] out = new int[pixels.length];
        int outerLimit = horizontal ? height : width;
        int innerLimit = horizontal ? width : height;
        for (int outer = 0; outer < outerLimit; outer++) {
            for (int inner = 0; inner < innerLimit; inner++) {
                int a = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                int count = 0;
                for (int d = -radius; d <= radius; d++) {
                    int sample = inner + d;
                    if (sample < 0 || sample >= innerLimit) {
                        continue;
                    }
                    int index = horizontal ? (outer * width + sample) : (sample * width + outer);
                    int pixel = pixels[index];
                    a += (pixel >>> 24) & 0xFF;
                    r += (pixel >>> 16) & 0xFF;
                    g += (pixel >>> 8) & 0xFF;
                    b += pixel & 0xFF;
                    count++;
                }
                int index = horizontal ? (outer * width + inner) : (inner * width + outer);
                out[index] = ((a / count) << 24) | ((r / count) << 16) | ((g / count) << 8) | (b / count);
            }
        }
        return out;
    }
}
