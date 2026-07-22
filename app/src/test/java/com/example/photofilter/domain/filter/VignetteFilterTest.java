package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class VignetteFilterTest {

    @Test
    public void cornersAreDarkerThanCenter() {
        int size = 40;
        Bitmap source = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        source.eraseColor(Color.WHITE);

        Bitmap result = new VignetteFilter().apply(source);

        int centerBrightness = Color.red(result.getPixel(size / 2, size / 2));
        int cornerBrightness = Color.red(result.getPixel(0, 0));

        assertTrue("Góc ảnh phải tối hơn giữa ảnh", cornerBrightness < centerBrightness);
    }
}
