package com.example.photofilter.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofilter.R;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.presenter.EditorContract;
import com.example.photofilter.presenter.EditorPresenter;
import com.example.photofilter.presenter.FilterThumbnail;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Set;

/**
 * View layer only: renders whatever {@link EditorPresenter} tells it to and
 * forwards user actions back to the presenter. No filter/bitmap logic here.
 */
public class MainActivity extends AppCompatActivity implements EditorContract.View {

    private static final int ADJUST_DEBOUNCE_MS = 120;

    private ImageView mainImageView;
    private TextView emptyStateText;
    private ProgressBar progressBar;
    private Button pickImageButton;
    private Button saveButton;
    private Button shareButton;
    private Button rotateButton;
    private Button cropButton;
    private Button adjustToggleButton;
    private Button aiEnhanceButton;
    private View adjustPanel;
    private SeekBar brightnessSeekBar;
    private SeekBar contrastSeekBar;
    private SeekBar saturationSeekBar;

    private FilterAdapter filterAdapter;
    private EditorContract.Presenter presenter;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable adjustRunnable = this::applyAdjustValues;

    private ActivityResultLauncher<String[]> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Runnable pendingStorageAction;
    private Uri pendingCameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        GradientTextHelper.applyBrandGradient(findViewById(R.id.wordmarkText));

        mainImageView = findViewById(R.id.mainImageView);
        emptyStateText = findViewById(R.id.emptyStateText);
        progressBar = findViewById(R.id.progressBar);
        pickImageButton = findViewById(R.id.pickImageButton);
        saveButton = findViewById(R.id.saveButton);
        shareButton = findViewById(R.id.shareButton);
        rotateButton = findViewById(R.id.rotateButton);
        cropButton = findViewById(R.id.cropButton);
        adjustToggleButton = findViewById(R.id.adjustToggleButton);
        aiEnhanceButton = findViewById(R.id.aiEnhanceButton);
        adjustPanel = findViewById(R.id.adjustPanel);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        contrastSeekBar = findViewById(R.id.contrastSeekBar);
        saturationSeekBar = findViewById(R.id.saturationSeekBar);

        RecyclerView filterRecyclerView = findViewById(R.id.filterRecyclerView);
        filterAdapter = new FilterAdapter(this::onFilterClicked, this::onFilterLongClicked);
        filterRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterRecyclerView.setAdapter(filterAdapter);

        presenter = new EditorPresenter(this);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onImagePicked);
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onPictureTaken);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);

        pickImageButton.setOnClickListener(v -> showPickSourceDialog());
        saveButton.setOnClickListener(v -> withStoragePermission(() -> presenter.onSaveClicked()));
        shareButton.setOnClickListener(v -> withStoragePermission(() -> presenter.onShareClicked()));
        rotateButton.setOnClickListener(v -> presenter.onRotateRequested());
        cropButton.setOnClickListener(v -> showCropDialog());
        adjustToggleButton.setOnClickListener(v -> toggleAdjustPanel());
        aiEnhanceButton.setOnClickListener(v -> presenter.onAiEnhanceRequested());

        SeekBar.OnSeekBarChangeListener adjustListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    scheduleAdjustApply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no-op
            }
        };
        brightnessSeekBar.setOnSeekBarChangeListener(adjustListener);
        contrastSeekBar.setOnSeekBarChangeListener(adjustListener);
        saturationSeekBar.setOnSeekBarChangeListener(adjustListener);

        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        mainHandler.removeCallbacks(adjustRunnable);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPickSourceDialog() {
        String[] options = {getString(R.string.action_library), getString(R.string.action_camera)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_pick_source_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageLauncher.launch(new String[]{"image/*"});
                    } else {
                        launchCamera();
                    }
                })
                .show();
    }

    private void launchCamera() {
        Uri uri = presenter.createCameraOutputUri();
        if (uri == null) {
            return;
        }
        pendingCameraUri = uri;
        takePictureLauncher.launch(uri);
    }

    private void onPictureTaken(boolean success) {
        if (success && pendingCameraUri != null) {
            onImagePicked(pendingCameraUri);
        }
        pendingCameraUri = null;
    }

    private void showCropDialog() {
        String[] options = {
                getString(R.string.crop_ratio_original),
                getString(R.string.crop_ratio_square),
                getString(R.string.crop_ratio_four_three),
                getString(R.string.crop_ratio_sixteen_nine)
        };
        EditorContract.CropRatio[] ratios = {
                EditorContract.CropRatio.ORIGINAL,
                EditorContract.CropRatio.SQUARE,
                EditorContract.CropRatio.FOUR_THREE,
                EditorContract.CropRatio.SIXTEEN_NINE
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_crop_title)
                .setItems(options, (dialog, which) -> presenter.onCropRequested(ratios[which]))
                .show();
    }

    private void toggleAdjustPanel() {
        boolean nowVisible = adjustPanel.getVisibility() != View.VISIBLE;
        adjustPanel.setVisibility(nowVisible ? View.VISIBLE : View.GONE);
    }

    private void scheduleAdjustApply() {
        mainHandler.removeCallbacks(adjustRunnable);
        mainHandler.postDelayed(adjustRunnable, ADJUST_DEBOUNCE_MS);
    }

    private void applyAdjustValues() {
        int brightness = brightnessSeekBar.getProgress() - 100;
        int contrast = contrastSeekBar.getProgress();
        int saturation = saturationSeekBar.getProgress();
        presenter.onAdjustValuesChanged(brightness, contrast, saturation);
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        presenter.onImagePicked(uri, metrics.widthPixels, metrics.heightPixels);
    }

    private void onFilterClicked(FilterItem filterItem) {
        presenter.onFilterSelected(filterItem);
    }

    private void onFilterLongClicked(FilterItem filterItem) {
        presenter.onFavoriteToggled(filterItem);
    }

    private void withStoragePermission(Runnable action) {
        boolean needsRuntimePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;
        if (!needsRuntimePermission) {
            action.run();
            return;
        }
        pendingStorageAction = action;
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void onPermissionResult(boolean granted) {
        Runnable action = pendingStorageAction;
        pendingStorageAction = null;
        if (granted && action != null) {
            action.run();
        } else if (!granted) {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    // ----- EditorContract.View -----

    @Override
    public void showFilterList(List<FilterItem> filters) {
        filterAdapter.submitFilterList(filters);
    }

    @Override
    public void showOriginalImage(Bitmap bitmap) {
        emptyStateText.setVisibility(View.GONE);
        mainImageView.setImageBitmap(bitmap);
        saveButton.setEnabled(true);
        shareButton.setEnabled(true);
        rotateButton.setEnabled(true);
        cropButton.setEnabled(true);
        adjustToggleButton.setEnabled(true);
        aiEnhanceButton.setEnabled(true);
    }

    @Override
    public void showFilteredImage(Bitmap bitmap) {
        mainImageView.setImageBitmap(bitmap);
    }

    @Override
    public void showFilterThumbnails(List<FilterThumbnail> thumbnails) {
        filterAdapter.submitThumbnails(thumbnails);
    }

    @Override
    public void showFavoriteIds(Set<String> favoriteFilterIds) {
        filterAdapter.submitFavorites(favoriteFilterIds);
    }

    @Override
    public void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSaveResult(boolean success, Uri savedUri) {
        if (success) {
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void launchShareIntent(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)));
    }
}
