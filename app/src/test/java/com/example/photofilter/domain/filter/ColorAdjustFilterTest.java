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

        Bitmap result = new ColorAdjustFilter(0f, 100f, 100f).apply(source);
        int pixel = result.getPixel(0, 0);

        assertEquals(120, Color.red(pixel));
        assertEquals(80, Color.green(pixel));
        assertEquals(200, Color.blue(pixel));
    }

    @Test
    public void positiveBrightness_lightensPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(100, 100, 100));

        Bitmap result = new ColorAdjustFilter(50f, 100f, 100f).apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Pixel phải sáng hơn 100, thực tế: " + Color.red(pixel), Color.red(pixel) > 100);
    }
}
