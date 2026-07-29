package com.example.photofilter.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photofilter.R;
import com.example.photofilter.data.AuthRepository;

/**
 * One-time welcome screen shown after the first login/registration on a
 * device. Plays a short voice clip and auto-advances to {@link HomeActivity}
 * when it finishes; Back or a MediaPlayer failure both skip straight to Home.
 */
public class IntroActivity extends AppCompatActivity {

    private static final int FALLBACK_DELAY_MS = 2500;

    private AuthRepository authRepository;
    private MediaPlayer mediaPlayer;
    private ValueAnimator kenBurnsAnimator;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        authRepository = new AuthRepository(getApplicationContext());

        startKenBurnsZoom(findViewById(R.id.introBackgroundView));
        findViewById(R.id.introWelcomeText).animate()
                .alpha(1f)
                .setStartDelay(300)
                .setDuration(600)
                .start();

        mediaPlayer = MediaPlayer.create(this, R.raw.welcome_hat);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> goToHome());
            mediaPlayer.start();
        } else {
            mainHandler.postDelayed(this::goToHome, FALLBACK_DELAY_MS);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goToHome();
            }
        });
    }

    private void startKenBurnsZoom(View backgroundView) {
        kenBurnsAnimator = ValueAnimator.ofFloat(1f, 1.15f);
        kenBurnsAnimator.setDuration(9000);
        kenBurnsAnimator.setRepeatMode(ValueAnimator.REVERSE);
        kenBurnsAnimator.setRepeatCount(ValueAnimator.INFINITE);
        kenBurnsAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        kenBurnsAnimator.addUpdateListener(a -> {
            float scale = (float) a.getAnimatedValue();
            backgroundView.setScaleX(scale);
            backgroundView.setScaleY(scale);
        });
        kenBurnsAnimator.start();
    }

    /** Idempotent: completion listener, Back press and the fallback timer can each try to call this once. */
    private void goToHome() {
        if (navigated) {
            return;
        }
        navigated = true;
        authRepository.markIntroSeen();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (kenBurnsAnimator != null) {
            kenBurnsAnimator.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
