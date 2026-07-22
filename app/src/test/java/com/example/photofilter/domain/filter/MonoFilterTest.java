package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MonoFilterTest {

    @Test
    public void removesColorFromAPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(200, 80, 40));

        Bitmap result = new MonoFilter().apply(source);
        int pixel = result.getPixel(0, 0);

        assertEquals("Sau khu mau, kenh do va xanh la phai gan bang nhau (mono)",
                Color.red(pixel), Color.green(pixel), 4);
    }
}
