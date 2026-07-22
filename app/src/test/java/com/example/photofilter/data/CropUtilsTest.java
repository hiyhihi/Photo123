package com.example.photofilter.data;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class CropUtilsTest {

    @Test
    public void customCrop_withHalfRect_returnsQuarterSizeBitmap() {
        Bitmap source = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);

        Bitmap result = CropUtils.customCrop(source, new RectF(0f, 0f, 0.5f, 0.5f));

        assertEquals(50, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    public void customCrop_withFullRect_keepsOriginalSize() {
        Bitmap source = Bitmap.createBitmap(80, 60, Bitmap.Config.ARGB_8888);

        Bitmap result = CropUtils.customCrop(source, new RectF(0f, 0f, 1f, 1f));

        assertEquals(80, result.getWidth());
        assertEquals(60, result.getHeight());
    }
}
