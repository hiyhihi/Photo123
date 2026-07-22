package com.example.photofilter.data;

import android.graphics.Bitmap;

import java.util.Arrays;

/**
 * Local (non-ML) "AI tools" — sharpen, denoise and upscale — implemented as
 * plain pixel-array processing, the same technique {@code BlurFilter} uses.
 * Kept out of the {@code domain.filter} package since these are one-shot
 * editor tools, not entries in the filter picker.
 */
public class AiToolsRepository {

    private static final int[] SHARPEN_KERNEL = {
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
    };

    public Bitmap sharpen(Bitmap source) {
        return convolve3x3(source, SHARPEN_KERNEL);
    }

    /** 3x3 median filter — better at removing speckle noise than an averaging blur. */
    public Bitmap removeNoise(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] out = new int[pixels.length];

        int[] a = new int[9];
        int[] r = new int[9];
        int[] g = new int[9];
        int[] b = new int[9];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int count = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    int sy = clamp(y + dy, height);
                    for (int dx = -1; dx <= 1; dx++) {
                        int sx = clamp(x + dx, width);
                        int pixel = pixels[sy * width + sx];
                        a[count] = (pixel >>> 24) & 0xFF;
                        r[count] = (pixel >>> 16) & 0xFF;
                        g[count] = (pixel >>> 8) & 0xFF;
                        b[count] = pixel & 0xFF;
                        count++;
                    }
                }
                Arrays.sort(a);
                Arrays.sort(r);
                Arrays.sort(g);
                Arrays.sort(b);
                out[y * width + x] = (a[4] << 24) | (r[4] << 16) | (g[4] << 8) | b[4];
            }
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        output.setPixels(out, 0, width, 0, 0, width, height);
        return output;
    }

    /** 2x bilinear upscale followed by a sharpen pass to offset the softness scaling introduces. */
    public Bitmap upscale(Bitmap source) {
        int newWidth = source.getWidth() * 2;
        int newHeight = source.getHeight() * 2;
        Bitmap scaled = Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
        Bitmap result = sharpen(scaled);
        if (scaled != result) {
            scaled.recycle();
        }
        return result;
    }

    private static Bitmap convolve3x3(Bitmap source, int[] kernel) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] out = new int[pixels.length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = 0;
                int g = 0;
                int b = 0;
                int k = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    int sy = clamp(y + dy, height);
                    for (int dx = -1; dx <= 1; dx++) {
                        int sx = clamp(x + dx, width);
                        int weight = kernel[k++];
                        int pixel = pixels[sy * width + sx];
                        r += weight * ((pixel >>> 16) & 0xFF);
                        g += weight * ((pixel >>> 8) & 0xFF);
                        b += weight * (pixel & 0xFF);
                    }
                }
                int alpha = (pixels[y * width + x] >>> 24) & 0xFF;
                out[y * width + x] = (alpha << 24) | (clampByte(r) << 16) | (clampByte(g) << 8) | clampByte(b);
            }
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        output.setPixels(out, 0, width, 0, 0, width, height);
        return output;
    }

    private static int clamp(int value, int limit) {
        return Math.max(0, Math.min(limit - 1, value));
    }

    private static int clampByte(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
