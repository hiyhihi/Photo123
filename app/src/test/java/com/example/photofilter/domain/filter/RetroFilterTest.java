package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class RetroFilterTest {

    @Test
    public void reducesBlueChannelOnAGrayPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(150, 150, 150));

        Bitmap result = new RetroFilter().apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Kenh xanh duong phai giam so voi kenh do (tong retro am)",
                Color.blue(pixel) < Color.red(pixel));
    }
}
