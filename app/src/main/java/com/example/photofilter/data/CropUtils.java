package com.example.photofilter.data;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/** Pure bitmap-geometry helpers used by the Crop tool sheet: center-crop, rotate, flip, resize. */
public final class CropUtils {

    private CropUtils() {
    }

    /** Scales the bitmap to {@code scalePercent}% of its current size (e.g. 150 = 150%). */
    public static Bitmap resize(Bitmap source, int scalePercent) {
        float scale = scalePercent / 100f;
        int newWidth = Math.max(1, Math.round(source.getWidth() * scale));
        int newHeight = Math.max(1, Math.round(source.getHeight() * scale));
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    public static Bitmap rotate90(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap flipHorizontal(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1f, 1f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap centerCrop(Bitmap source, CropRatio ratio) {
        if (ratio == CropRatio.ORIGINAL) {
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight());
        }
        float targetRatio = targetRatio(ratio);
        int width = source.getWidth();
        int height = source.getHeight();
        float currentRatio = (float) width / height;
        int cropWidth = width;
        int cropHeight = height;
        if (currentRatio > targetRatio) {
            cropWidth = Math.round(height * targetRatio);
        } else {
            cropHeight = Math.round(width / targetRatio);
        }
        int x = (width - cropWidth) / 2;
        int y = (height - cropHeight) / 2;
        return Bitmap.createBitmap(source, x, y, cropWidth, cropHeight);
    }

    private static float targetRatio(CropRatio ratio) {
        switch (ratio) {
            case SQUARE:
                return 1f;
            case FOUR_THREE:
                return 4f / 3f;
            case SIXTEEN_NINE:
                return 16f / 9f;
            default:
                return 1f;
        }
    }
}
