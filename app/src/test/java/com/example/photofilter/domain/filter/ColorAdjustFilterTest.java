package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ColorAdjustFilterTest {

    @Test
    public void neutralValues_leavePixelUnchanged() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(120, 80, 200));

        Bitmap result = new ColorAdjustFilter(0f, 100f, 100f, 0f, 0f).apply(source);
        int pixel = result.getPixel(0, 0);

        assertEquals(120, Color.red(pixel));
        assertEquals(80, Color.green(pixel));
        assertEquals(200, Color.blue(pixel));
    }

    @Test
    public void positiveBrightness_lightensPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(100, 100, 100));

        Bitmap result = new ColorAdjustFilter(50f, 100f, 100f, 0f, 0f).apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Pixel phải sáng hơn 100, thực tế: " + Color.red(pixel), Color.red(pixel) > 100);
    }

    @Test
    public void positiveExposure_lightensPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(100, 100, 100));

        Bitmap result = new ColorAdjustFilter(0f, 100f, 100f, 0f, 50f).apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Pixel phải sáng hơn 100 khi tăng exposure, thực tế: " + Color.red(pixel), Color.red(pixel) > 100);
    }

    @Test
    public void hueRotation_shiftsColor() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(200, 60, 60));

        Bitmap result = new ColorAdjustFilter(0f, 100f, 100f, 120f, 0f).apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Xoay hue 120 do phai lam doi mau (kenh do khong con vuot troi)",
                Color.red(pixel) != 200 || Color.green(pixel) != 60 || Color.blue(pixel) != 60);
    }
}
