package com.example.photofilter.data;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * Cuts the subject out of the background using ML Kit's on-device Selfie
 * Segmentation model — real ML, unlike the pixel-math tools in
 * {@link AiToolsRepository}. Runs synchronously (blocking on {@link Tasks#await});
 * callers must invoke this off the main thread.
 */
public class BackgroundRemovalRepository {

    private static final float FOREGROUND_CONFIDENCE_THRESHOLD = 0.5f;

    public Bitmap removeBackground(Bitmap source) throws IOException {
        SelfieSegmenterOptions options = new SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build();
        Segmenter segmenter = Segmentation.getClient(options);
        try {
            InputImage inputImage = InputImage.fromBitmap(source, 0);
            SegmentationMask mask = Tasks.await(segmenter.process(inputImage));
            return applyMask(source, mask);
        } catch (ExecutionException | InterruptedException e) {
            throw new IOException("Khong the tach nen anh", e);
        } finally {
            segmenter.close();
        }
    }

    private static Bitmap applyMask(Bitmap source, SegmentationMask mask) {
        int width = mask.getWidth();
        int height = mask.getHeight();
        ByteBuffer buffer = mask.getBuffer();
        buffer.rewind();

        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        int[] pixels = new int[width * height];
        result.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            float confidence = buffer.getFloat();
            if (confidence < FOREGROUND_CONFIDENCE_THRESHOLD) {
                pixels[i] = pixels[i] & 0x00FFFFFF;
            }
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
