package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class NegativeFilterTest {

    @Test
    public void invertsEachColorChannel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(255, 0, 40));

        Bitmap result = new NegativeFilter().apply(source);
        int pixel = result.getPixel(0, 0);

        assertEquals(0, Color.red(pixel));
        assertEquals(255, Color.green(pixel));
        assertEquals(215, Color.blue(pixel));
    }
}
