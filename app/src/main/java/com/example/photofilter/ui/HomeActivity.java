package com.example.photofilter.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofilter.R;
import com.example.photofilter.data.AuthRepository;
import com.example.photofilter.data.HistoryEntry;
import com.example.photofilter.data.HistoryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Premium dashboard entry screen: hero mood board (Ken Burns zoom + drifting
 * particles), four action cards and a Recent Photos strip backed by saved-edit
 * history. No editing logic here — everything routes into {@link MainActivity}
 * or {@link HistoryActivity}.
 */
public class HomeActivity extends AppCompatActivity {

    private static final int CARD_FADE_STAGGER_MS = 120;
    private static final int RECENT_PHOTOS_LIMIT = 12;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AuthRepository authRepository;
    private HistoryRepository historyRepository;

    private View heroBackgroundView;
    private ParticleView particleView;
    private ValueAnimator kenBurnsAnimator;

    private View cardGallery;
    private View cardCamera;
    private View cardAiEnhance;
    private View cardHistory;

    private RecyclerView recentPhotosRecyclerView;
    private TextView recentPhotosEmptyText;
    private RecentPhotoAdapter recentPhotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        historyRepository = new HistoryRepository(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());
        heroBackgroundView = findViewById(R.id.heroBackgroundView);
        particleView = findViewById(R.id.particleView);

        setUpCards();
        setUpRecentPhotos();
        setUpAccountButton();
        startKenBurnsZoom();
        fadeInCardsSequentially();
    }

    @Override
    protected void onResume() {
        super.onResume();
        particleView.start();
        loadRecentPhotos();
    }

    @Override
    protected void onPause() {
        particleView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (kenBurnsAnimator != null) {
            kenBurnsAnimator.cancel();
        }
        executor.shutdown();
        super.onDestroy();
    }

    private void setUpCards() {
        cardGallery = findViewById(R.id.cardGallery);
        cardCamera = findViewById(R.id.cardCamera);
        cardAiEnhance = findViewById(R.id.cardAiEnhance);
        cardHistory = findViewById(R.id.cardHistory);

        bindCard(cardGallery, android.R.drawable.ic_menu_gallery,
                R.string.home_card_gallery, R.string.home_card_gallery_subtitle,
                () -> openEditor(cardGallery, MainActivity.AUTO_ACTION_PICK));
        bindCard(cardCamera, android.R.drawable.ic_menu_camera,
                R.string.home_card_camera, R.string.home_card_camera_subtitle,
                () -> openEditor(cardCamera, MainActivity.AUTO_ACTION_CAMERA));
        bindCard(cardAiEnhance, R.drawable.ic_tab_ai,
                R.string.home_card_ai_enhance, R.string.home_card_ai_enhance_subtitle,
                () -> openEditor(cardAiEnhance, MainActivity.AUTO_ACTION_PICK_THEN_AI));
        bindCard(cardHistory, android.R.drawable.ic_menu_recent_history,
                R.string.home_card_history, R.string.home_card_history_subtitle,
                () -> startActivity(new Intent(this, HistoryActivity.class)));
    }

    private void bindCard(View card, int iconRes, int titleRes, int subtitleRes, Runnable onClick) {
        ImageView icon = card.findViewById(R.id.cardIcon);
        TextView title = card.findViewById(R.id.cardTitle);
        TextView subtitle = card.findViewById(R.id.cardSubtitle);
        icon.setImageResource(iconRes);
        title.setText(titleRes);
        subtitle.setText(subtitleRes);
        card.setContentDescription(getString(titleRes));
        card.setOnClickListener(v -> onClick.run());
    }

    private void openEditor(View sharedElement, String autoAction) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_AUTO_ACTION, autoAction);
        sharedElement.setTransitionName("editorPhotoCanvas");
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedElement, "editorPhotoCanvas");
        startActivity(intent, options.toBundle());
    }

    private void setUpAccountButton() {
        findViewById(R.id.accountButton).setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        String email = authRepository.getCurrentUserEmail();
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(getString(R.string.logout_confirm_message, email != null ? email : ""))
                .setPositiveButton(R.string.action_logout, (dialog, which) -> {
                    authRepository.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.action_cancel_dialog, null)
                .show();
    }

    private void setUpRecentPhotos() {
        recentPhotosRecyclerView = findViewById(R.id.recentPhotosRecyclerView);
        recentPhotosEmptyText = findViewById(R.id.recentPhotosEmptyText);
        recentPhotoAdapter = new RecentPhotoAdapter(entry -> startActivity(new Intent(this, HistoryActivity.class)));
        recentPhotosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentPhotosRecyclerView.setAdapter(recentPhotoAdapter);
    }

    private void loadRecentPhotos() {
        executor.execute(() -> {
            List<HistoryEntry> all = historyRepository.getAll();
            List<HistoryEntry> recent = all.subList(0, Math.min(RECENT_PHOTOS_LIMIT, all.size()));
            mainHandler.post(() -> {
                if (isFinishing()) {
                    return;
                }
                recentPhotoAdapter.submitEntries(recent);
                boolean hasPhotos = !recent.isEmpty();
                recentPhotosRecyclerView.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
                recentPhotosEmptyText.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
            });
        });
    }

    /** Slow, endless zoom on the hero background — the classic "Ken Burns" documentary pan. */
    private void startKenBurnsZoom() {
        kenBurnsAnimator = ValueAnimator.ofFloat(1f, 1.15f);
        kenBurnsAnimator.setDuration(9000);
        kenBurnsAnimator.setRepeatMode(ValueAnimator.REVERSE);
        kenBurnsAnimator.setRepeatCount(ValueAnimator.INFINITE);
        kenBurnsAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        kenBurnsAnimator.addUpdateListener(a -> {
            float scale = (float) a.getAnimatedValue();
            heroBackgroundView.setScaleX(scale);
            heroBackgroundView.setScaleY(scale);
        });
        kenBurnsAnimator.start();
    }

    /** Cards fade + rise in one after another; once visible each starts its own soft glow pulse. */
    private void fadeInCardsSequentially() {
        View[] cards = {cardGallery, cardCamera, cardAiEnhance, cardHistory};
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            long delay = (long) CARD_FADE_STAGGER_MS * i;
            card.setAlpha(0f);
            card.setTranslationY(48f);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(420)
                    .withEndAction(() -> startGlowPulse(card))
                    .start();
        }
    }

    private void startGlowPulse(View card) {
        View glow = card.findViewById(R.id.cardGlow);
        ObjectAnimator pulse = ObjectAnimator.ofFloat(glow, View.ALPHA, 0.55f, 1f);
        pulse.setDuration(1600);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.start();
    }
}
