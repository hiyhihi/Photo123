package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class GrayscaleFilterTest {

    @Test
    public void desaturatesToEqualChannels() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(200, 40, 90));

        Bitmap result = new GrayscaleFilter().apply(source);
        int pixel = result.getPixel(0, 0);
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);

        assertEquals(r, g);
        assertEquals(g, b);
        assertTrue("Kết quả không nên là màu tuyệt đối đen hoặc trắng", r > 0 && r < 255);
    }
}
