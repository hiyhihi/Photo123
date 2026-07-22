package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class FilmFilterTest {

    @Test
    public void liftsBlacksOnADarkPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(10, 10, 10));

        Bitmap result = new FilmFilter().apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Vung toi phai duoc nang len (khong con den thui)", Color.red(pixel) > 10);
    }
}
