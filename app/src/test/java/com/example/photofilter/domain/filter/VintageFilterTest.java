package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class VintageFilterTest {

    @Test
    public void warmsUpAGrayPixel() {
        Bitmap source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        source.setPixel(0, 0, Color.rgb(150, 150, 150));

        Bitmap result = new VintageFilter().apply(source);
        int pixel = result.getPixel(0, 0);

        assertTrue("Kênh đỏ phải cao hơn kênh xanh dương (tông ấm)",
                Color.red(pixel) > Color.blue(pixel));
    }
}
