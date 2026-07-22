package com.example.photofilter.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Decorative-only: a handful of soft dots drifting slowly upward behind the
 * hero image, looping forever. Positions are randomized on each layout pass
 * (view size isn't known until then); the animation itself just advances a
 * 0..1 clock and each particle maps that clock to its own screen position.
 */
public class ParticleView extends View {

    private static final int PARTICLE_COUNT = 18;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private ValueAnimator animator;

    public ParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) {
            return;
        }
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(random, w, h));
        }
    }

    /** Starts the looping drift animation; call from onResume-equivalent lifecycle points. */
    public void start() {
        if (animator != null) {
            return;
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(9000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> invalidate());
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animator == null) {
            return;
        }
        float clock = (float) animator.getAnimatedValue();
        int width = getWidth();
        int height = getHeight();
        for (Particle particle : particles) {
            float progress = (clock + particle.phaseOffset) % 1f;
            float y = height * (1f - progress);
            float x = particle.baseX * width + particle.driftAmplitude * width
                    * (float) Math.sin(progress * Math.PI * 2 * particle.driftCycles);
            float fadeIn = Math.min(1f, progress * 6f);
            float fadeOut = Math.min(1f, (1f - progress) * 6f);
            paint.setAlpha((int) (particle.maxAlpha * Math.min(fadeIn, fadeOut)));
            canvas.drawCircle(x, y, particle.radius, paint);
        }
    }

    private static final class Particle {
        final float baseX;
        final float phaseOffset;
        final float radius;
        final int maxAlpha;
        final float driftAmplitude;
        final float driftCycles;

        Particle(Random random, int width, int height) {
            baseX = random.nextFloat();
            phaseOffset = random.nextFloat();
            radius = (2 + random.nextFloat() * 3) * (width / 360f);
            maxAlpha = 60 + random.nextInt(90);
            driftAmplitude = 0.02f + random.nextFloat() * 0.03f;
            driftCycles = 1 + random.nextInt(2);
        }
    }
}
