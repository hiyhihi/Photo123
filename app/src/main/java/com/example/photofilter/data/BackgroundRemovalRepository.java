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

    /**
     * The mask's resolution does not always match the source bitmap's (ML Kit
     * can return it at the model's own internal size), so it must be scaled
     * up to the source's dimensions before being applied pixel-for-pixel —
     * applying it directly at mask resolution only ever touched a corner of
     * the real photo.
     */
    private static Bitmap applyMask(Bitmap source, SegmentationMask mask) {
        int maskWidth = mask.getWidth();
        int maskHeight = mask.getHeight();
        ByteBuffer buffer = mask.getBuffer();
        buffer.rewind();

        int[] maskPixels = new int[maskWidth * maskHeight];
        for (int i = 0; i < maskPixels.length; i++) {
            float confidence = buffer.getFloat();
            maskPixels[i] = confidence >= FOREGROUND_CONFIDENCE_THRESHOLD ? 0xFF000000 : 0x00000000;
        }
        Bitmap maskBitmap = Bitmap.createBitmap(maskPixels, maskWidth, maskHeight, Bitmap.Config.ARGB_8888);

        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap scaledMask = (maskWidth == width && maskHeight == height)
                ? maskBitmap
                : Bitmap.createScaledBitmap(maskBitmap, width, height, true);

        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        int[] pixels = new int[width * height];
        int[] maskAlpha = new int[width * height];
        result.getPixels(pixels, 0, width, 0, 0, width, height);
        scaledMask.getPixels(maskAlpha, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            if (maskAlpha[i] >>> 24 == 0) {
                pixels[i] = pixels[i] & 0x00FFFFFF;
            }
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
