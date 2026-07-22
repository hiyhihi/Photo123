package com.example.photofilter.domain.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class BlurFilterTest {

    @Test
    public void softensASharpEdge() {
        int size = 30;
        Bitmap source = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                source.setPixel(x, y, x < size / 2 ? Color.BLACK : Color.WHITE);
            }
        }

        Bitmap result = new BlurFilter().apply(source);

        int atBoundary = Color.red(result.getPixel(size / 2, size / 2));
        assertTrue("Điểm giữa biên phải bị pha trộn (không còn đen tuyệt đối hay trắng tuyệt đối)",
                atBoundary > 10 && atBoundary < 245);
    }
}
