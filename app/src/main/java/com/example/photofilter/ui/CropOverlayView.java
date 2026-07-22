package com.example.photofilter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Free-form crop rectangle with draggable corner handles, drawn over the main
 * image. Coordinates are in this view's own pixel space, which the host
 * Activity keeps aligned 1:1 with the underlying ImageView (same bounds
 * inside the same FrameLayout) so no extra transform is needed here — only
 * {@link #getNormalizedCropRect()} maps back to the image's own 0..1 space.
 */
public class CropOverlayView extends View {

    private static final int HANDLE_NONE = 0;
    private static final int HANDLE_TOP_LEFT = 1;
    private static final int HANDLE_TOP_RIGHT = 2;
    private static final int HANDLE_BOTTOM_LEFT = 3;
    private static final int HANDLE_BOTTOM_RIGHT = 4;
    private static final int HANDLE_MOVE = 5;

    private final Paint dimPaint = new Paint();
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF imageBounds = new RectF();
    private final RectF cropRect = new RectF();
    private final float handleTouchRadiusPx;
    private final float minSizePx;
    private final float handleRadiusPx;

    private int activeHandle = HANDLE_NONE;
    private float lastTouchX;
    private float lastTouchY;

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = context.getResources().getDisplayMetrics().density;
        handleTouchRadiusPx = 24f * density;
        minSizePx = 48f * density;
        handleRadiusPx = 6f * density;

        dimPaint.setColor(0xAA000000);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f * density);
        gridPaint.setColor(0x66FFFFFF);
        gridPaint.setStrokeWidth(1f * density);
        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);
    }

    /** Called by the host once it knows where the ImageView's actual (letterboxed) image sits. */
    public void setImageBounds(RectF bounds) {
        imageBounds.set(bounds);
        float insetX = bounds.width() * 0.1f;
        float insetY = bounds.height() * 0.1f;
        cropRect.set(bounds.left + insetX, bounds.top + insetY, bounds.right - insetX, bounds.bottom - insetY);
        invalidate();
    }

    /** Current crop rectangle as fractions (0..1) of the image, ready to hand to the presenter. */
    public RectF getNormalizedCropRect() {
        return new RectF(
                (cropRect.left - imageBounds.left) / imageBounds.width(),
                (cropRect.top - imageBounds.top) / imageBounds.height(),
                (cropRect.right - imageBounds.left) / imageBounds.width(),
                (cropRect.bottom - imageBounds.top) / imageBounds.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (imageBounds.isEmpty()) {
            return;
        }
        canvas.drawRect(imageBounds.left, imageBounds.top, imageBounds.right, cropRect.top, dimPaint);
        canvas.drawRect(imageBounds.left, cropRect.bottom, imageBounds.right, imageBounds.bottom, dimPaint);
        canvas.drawRect(imageBounds.left, cropRect.top, cropRect.left, cropRect.bottom, dimPaint);
        canvas.drawRect(cropRect.right, cropRect.top, imageBounds.right, cropRect.bottom, dimPaint);

        canvas.drawRect(cropRect, borderPaint);

        float thirdWidth = cropRect.width() / 3f;
        float thirdHeight = cropRect.height() / 3f;
        canvas.drawLine(cropRect.left + thirdWidth, cropRect.top, cropRect.left + thirdWidth, cropRect.bottom, gridPaint);
        canvas.drawLine(cropRect.left + 2 * thirdWidth, cropRect.top, cropRect.left + 2 * thirdWidth, cropRect.bottom, gridPaint);
        canvas.drawLine(cropRect.left, cropRect.top + thirdHeight, cropRect.right, cropRect.top + thirdHeight, gridPaint);
        canvas.drawLine(cropRect.left, cropRect.top + 2 * thirdHeight, cropRect.right, cropRect.top + 2 * thirdHeight, gridPaint);

        canvas.drawCircle(cropRect.left, cropRect.top, handleRadiusPx, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.top, handleRadiusPx, handlePaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleRadiusPx, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleRadiusPx, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageBounds.isEmpty()) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = detectHandle(x, y);
                lastTouchX = x;
                lastTouchY = y;
                return activeHandle != HANDLE_NONE;
            case MotionEvent.ACTION_MOVE:
                if (activeHandle == HANDLE_NONE) {
                    return false;
                }
                moveHandle(activeHandle, x - lastTouchX, y - lastTouchY);
                lastTouchX = x;
                lastTouchY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeHandle = HANDLE_NONE;
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private int detectHandle(float x, float y) {
        if (isNear(x, y, cropRect.left, cropRect.top)) {
            return HANDLE_TOP_LEFT;
        }
        if (isNear(x, y, cropRect.right, cropRect.top)) {
            return HANDLE_TOP_RIGHT;
        }
        if (isNear(x, y, cropRect.left, cropRect.bottom)) {
            return HANDLE_BOTTOM_LEFT;
        }
        if (isNear(x, y, cropRect.right, cropRect.bottom)) {
            return HANDLE_BOTTOM_RIGHT;
        }
        if (cropRect.contains(x, y)) {
            return HANDLE_MOVE;
        }
        return HANDLE_NONE;
    }

    private boolean isNear(float x, float y, float handleX, float handleY) {
        float dx = x - handleX;
        float dy = y - handleY;
        return dx * dx + dy * dy <= handleTouchRadiusPx * handleTouchRadiusPx;
    }

    private void moveHandle(int handle, float dx, float dy) {
        switch (handle) {
            case HANDLE_TOP_LEFT:
                cropRect.left = clamp(cropRect.left + dx, imageBounds.left, cropRect.right - minSizePx);
                cropRect.top = clamp(cropRect.top + dy, imageBounds.top, cropRect.bottom - minSizePx);
                break;
            case HANDLE_TOP_RIGHT:
                cropRect.right = clamp(cropRect.right + dx, cropRect.left + minSizePx, imageBounds.right);
                cropRect.top = clamp(cropRect.top + dy, imageBounds.top, cropRect.bottom - minSizePx);
                break;
            case HANDLE_BOTTOM_LEFT:
                cropRect.left = clamp(cropRect.left + dx, imageBounds.left, cropRect.right - minSizePx);
                cropRect.bottom = clamp(cropRect.bottom + dy, cropRect.top + minSizePx, imageBounds.bottom);
                break;
            case HANDLE_BOTTOM_RIGHT:
                cropRect.right = clamp(cropRect.right + dx, cropRect.left + minSizePx, imageBounds.right);
                cropRect.bottom = clamp(cropRect.bottom + dy, cropRect.top + minSizePx, imageBounds.bottom);
                break;
            case HANDLE_MOVE:
                float width = cropRect.width();
                float height = cropRect.height();
                float newLeft = clamp(cropRect.left + dx, imageBounds.left, imageBounds.right - width);
                float newTop = clamp(cropRect.top + dy, imageBounds.top, imageBounds.bottom - height);
                cropRect.set(newLeft, newTop, newLeft + width, newTop + height);
                break;
            default:
                break;
        }
    }

    private static float clamp(float value, float min, float max) {
        if (min > max) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
