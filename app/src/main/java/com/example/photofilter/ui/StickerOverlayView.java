package com.example.photofilter.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Shows the currently chosen sticker over the photo and lets the user drag
 * (1 finger) or pinch-zoom + rotate (2 fingers) it. Pure screen-space overlay —
 * never touches the real bitmap; {@link #getNormalizedPlacement()} is read once,
 * at Apply time, so the caller can bake the sticker into the real image elsewhere.
 */
public class StickerOverlayView extends View {

    private static final float DEFAULT_WIDTH_FRACTION = 0.3f;

    /** Normalized (0..1, relative to the image area set via {@link #setImageBounds}) sticker placement. */
    static final class Placement {
        final float centerXFraction;
        final float centerYFraction;
        final float scaleFraction;
        final float rotationDegrees;

        Placement(float centerXFraction, float centerYFraction, float scaleFraction, float rotationDegrees) {
            this.centerXFraction = centerXFraction;
            this.centerYFraction = centerYFraction;
            this.scaleFraction = scaleFraction;
            this.rotationDegrees = rotationDegrees;
        }
    }

    private Bitmap stickerBitmap;
    private RectF imageBounds;
    private float centerX;
    private float centerY;
    private float scale = 1f;
    private float rotationDegrees;

    private float lastX;
    private float lastY;
    private float lastSpan;
    private float lastAngle;
    private boolean twoFingerActive;

    public StickerOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setImageBounds(RectF bounds) {
        imageBounds = bounds;
    }

    boolean hasSticker() {
        return stickerBitmap != null;
    }

    Bitmap getStickerBitmap() {
        return stickerBitmap;
    }

    /** Swaps the displayed bitmap. Keeps the current position/scale/rotation if a sticker was already active. */
    void setSticker(Bitmap bitmap) {
        boolean firstTime = stickerBitmap == null;
        stickerBitmap = bitmap;
        if (firstTime && imageBounds != null) {
            centerX = imageBounds.centerX();
            centerY = imageBounds.centerY();
            scale = (imageBounds.width() * DEFAULT_WIDTH_FRACTION) / bitmap.getWidth();
            rotationDegrees = 0f;
        }
        invalidate();
    }

    /** Clears the overlay back to empty (no sticker) — call when the Sticker tab is closed/cancelled. */
    void reset() {
        stickerBitmap = null;
        invalidate();
    }

    /** @return normalized placement relative to {@link #setImageBounds} — read once, at Apply time. */
    Placement getNormalizedPlacement() {
        float cxFraction = (centerX - imageBounds.left) / imageBounds.width();
        float cyFraction = (centerY - imageBounds.top) / imageBounds.height();
        float scaleFraction = (scale * stickerBitmap.getWidth()) / imageBounds.width();
        return new Placement(cxFraction, cyFraction, scaleFraction, rotationDegrees);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stickerBitmap == null) {
            return;
        }
        Matrix matrix = new Matrix();
        matrix.postTranslate(-stickerBitmap.getWidth() / 2f, -stickerBitmap.getHeight() / 2f);
        matrix.postScale(scale, scale);
        matrix.postRotate(rotationDegrees);
        matrix.postTranslate(centerX, centerY);
        canvas.drawBitmap(stickerBitmap, matrix, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (stickerBitmap == null) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                twoFingerActive = false;
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    twoFingerActive = true;
                    lastSpan = spanBetween(event);
                    lastAngle = angleBetween(event);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (twoFingerActive && event.getPointerCount() >= 2) {
                    float span = spanBetween(event);
                    float angle = angleBetween(event);
                    if (lastSpan > 0f) {
                        scale *= span / lastSpan;
                    }
                    rotationDegrees += angle - lastAngle;
                    lastSpan = span;
                    lastAngle = angle;
                } else if (event.getPointerCount() == 1) {
                    centerX += event.getX() - lastX;
                    centerY += event.getY() - lastY;
                    lastX = event.getX();
                    lastY = event.getY();
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    // Dropping to 1 finger: re-anchor the drag origin to whichever pointer remains.
                    int remainingIndex = event.getActionIndex() == 0 ? 1 : 0;
                    lastX = event.getX(remainingIndex);
                    lastY = event.getY(remainingIndex);
                }
                twoFingerActive = false;
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                twoFingerActive = false;
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private static float spanBetween(MotionEvent event) {
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);
        return (float) Math.hypot(dx, dy);
    }

    private static float angleBetween(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }
}
