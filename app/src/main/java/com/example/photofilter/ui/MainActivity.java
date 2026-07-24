package com.example.photofilter.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.example.photofilter.data.CropRatio;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.presenter.EditorContract;
import com.example.photofilter.presenter.EditorPresenter;
import com.example.photofilter.presenter.FilterThumbnail;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;
import java.util.Set;

/**
 * View layer only: renders whatever {@link EditorPresenter} tells it to and
 * forwards user actions back to the presenter. No filter/bitmap logic here.
 *
 * <p>UI shape: a fixed 5-icon bottom nav (Filters/Crop/Adjust/AI/Export) drives
 * a single Material {@link BottomSheetBehavior} whose content swaps between five
 * panels — only one tab is ever open at a time.
 */
public class MainActivity extends AppCompatActivity implements EditorContract.View {

    public static final String EXTRA_AUTO_ACTION = "com.example.photofilter.extra.AUTO_ACTION";
    public static final String AUTO_ACTION_PICK = "pick";
    public static final String AUTO_ACTION_CAMERA = "camera";
    public static final String AUTO_ACTION_PICK_THEN_AI = "pick_then_ai";

    private static final int ADJUST_DEBOUNCE_MS = 120;
    private static final int TAB_FILTERS = 0;
    private static final int TAB_CROP = 1;
    private static final int TAB_ADJUST = 2;
    private static final int TAB_AI = 3;

    private ImageView mainImageView;
    private TextView emptyStateText;
    private ProgressBar progressBar;

    private BottomSheetBehavior<FrameLayout> sheetBehavior;
    private View[] panels;
    private View[] navButtons;
    private ImageView[] navIcons;
    private TextView[] navLabels;
    private int activeTab = -1;
    private int pendingTab = -1;

    private SeekBar brightnessSeekBar;
    private SeekBar contrastSeekBar;
    private SeekBar saturationSeekBar;
    private SeekBar hueSeekBar;
    private SeekBar exposureSeekBar;
    private View saveTopBarButton;
    private boolean shareUnlocked;

    private CropOverlayView cropOverlayView;
    private View cropCustomActionsRow;
    private boolean customCropActive;

    private FilterAdapter filterAdapter;
    private EditorContract.Presenter presenter;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable adjustRunnable = this::applyAdjustValues;

    private ActivityResultLauncher<String[]> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Runnable pendingStorageAction;
    private Uri pendingCameraUri;
    private boolean openAiTabOnNextImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        GradientTextHelper.applyBrandGradient(findViewById(R.id.wordmarkText));

        mainImageView = findViewById(R.id.mainImageView);
        emptyStateText = findViewById(R.id.emptyStateText);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.imageContainer).setOnClickListener(v -> {
            if (emptyStateText.getVisibility() == View.VISIBLE) {
                showPickSourceDialog();
            }
        });

        setUpBottomSheet();
        setUpFilterList();
        setUpCropPanel();
        setUpAdjustPanel();
        setUpAiPanel();
        setUpSaveButton();

        presenter = new EditorPresenter(this);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onImagePicked);
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onPictureTaken);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);

        presenter.attachView(this);
        runAutoActionIfRequested();
    }

    private void setUpBottomSheet() {
        FrameLayout sheet = findViewById(R.id.toolSheet);
        sheetBehavior = BottomSheetBehavior.from(sheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && pendingTab != -1) {
                    int tab = pendingTab;
                    pendingTab = -1;
                    showTab(tab);
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                // no-op
            }
        });

        panels = new View[]{
                findViewById(R.id.filterRecyclerView),
                findViewById(R.id.cropSheetPanel),
                findViewById(R.id.adjustSheetPanel),
                findViewById(R.id.aiSheetPanel)
        };
        navButtons = new View[]{
                findViewById(R.id.navFiltersButton),
                findViewById(R.id.navCropButton),
                findViewById(R.id.navAdjustButton),
                findViewById(R.id.navAiButton)
        };
        navIcons = new ImageView[]{
                findViewById(R.id.navFiltersIcon),
                findViewById(R.id.navCropIcon),
                findViewById(R.id.navAdjustIcon),
                findViewById(R.id.navAiIcon)
        };
        navLabels = new TextView[]{
                findViewById(R.id.navFiltersLabel),
                findViewById(R.id.navCropLabel),
                findViewById(R.id.navAdjustLabel),
                findViewById(R.id.navAiLabel)
        };
        for (int i = 0; i < navButtons.length; i++) {
            int tab = i;
            navButtons[i].setOnClickListener(v -> onTabTapped(tab));
        }
    }

    /** Tapping the open tab collapses it; tapping a different tab collapses the old one, then expands the new one once hidden. */
    private void onTabTapped(int tab) {
        boolean sheetOpen = sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                || sheetBehavior.getState() == BottomSheetBehavior.STATE_SETTLING;
        if (activeTab == tab && sheetOpen) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            setNavSelected(-1);
            activeTab = -1;
            if (customCropActive) {
                cancelCustomCrop();
            }
            return;
        }
        if (sheetOpen) {
            pendingTab = tab;
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            showTab(tab);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void showTab(int tab) {
        if (tab != TAB_CROP && customCropActive) {
            cancelCustomCrop();
        }
        for (View panel : panels) {
            panel.setVisibility(View.GONE);
        }
        panels[tab].setVisibility(View.VISIBLE);
        setNavSelected(tab);
        activeTab = tab;
    }

    private void setNavSelected(int tab) {
        for (int i = 0; i < navIcons.length; i++) {
            int color = ContextCompat.getColor(this, i == tab ? R.color.accent_yellow : R.color.text_muted);
            navIcons[i].setColorFilter(color);
            navLabels[i].setTextColor(color);
        }
    }

    private void setUpFilterList() {
        RecyclerView filterRecyclerView = findViewById(R.id.filterRecyclerView);
        filterAdapter = new FilterAdapter(this::onFilterClicked, this::onFilterLongClicked);
        filterRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterRecyclerView.setAdapter(filterAdapter);
    }

    private void setUpCropPanel() {
        setUpToolIcon(R.id.cropOriginalButton, R.drawable.ic_ratio_original, R.string.crop_ratio_original,
                () -> presenter.onCropRequested(CropRatio.ORIGINAL));
        setUpToolIcon(R.id.cropSquareButton, R.drawable.ic_ratio_square, R.string.crop_ratio_square,
                () -> presenter.onCropRequested(CropRatio.SQUARE));
        setUpToolIcon(R.id.cropFourThreeButton, R.drawable.ic_ratio_four_three, R.string.crop_ratio_four_three,
                () -> presenter.onCropRequested(CropRatio.FOUR_THREE));
        setUpToolIcon(R.id.cropSixteenNineButton, R.drawable.ic_ratio_sixteen_nine, R.string.crop_ratio_sixteen_nine,
                () -> presenter.onCropRequested(CropRatio.SIXTEEN_NINE));
        setUpToolIcon(R.id.cropCustomButton, R.drawable.ic_crop_custom, R.string.action_crop_custom, this::toggleCustomCrop);
        setUpToolIcon(R.id.rotateButton, R.drawable.ic_rotate, R.string.action_rotate, () -> presenter.onRotateRequested());
        setUpToolIcon(R.id.flipButton, R.drawable.ic_flip, R.string.action_flip, () -> presenter.onFlipRequested());
        setUpToolIcon(R.id.resize75Button, R.drawable.ic_resize, R.string.resize_75, () -> presenter.onResizeRequested(75));
        setUpToolIcon(R.id.resize100Button, R.drawable.ic_resize, R.string.resize_100, () -> presenter.onResizeRequested(100));
        setUpToolIcon(R.id.resize125Button, R.drawable.ic_resize, R.string.resize_125, () -> presenter.onResizeRequested(125));
        setUpToolIcon(R.id.resize150Button, R.drawable.ic_resize, R.string.resize_150, () -> presenter.onResizeRequested(150));
        setUpToolIcon(R.id.resize200Button, R.drawable.ic_resize, R.string.resize_200, () -> presenter.onResizeRequested(200));

        cropOverlayView = findViewById(R.id.cropOverlayView);
        cropCustomActionsRow = findViewById(R.id.cropCustomActionsRow);
        findViewById(R.id.cropCancelButton).setOnClickListener(v -> cancelCustomCrop());
        findViewById(R.id.cropConfirmButton).setOnClickListener(v -> confirmCustomCrop());
    }

    /** Shows/hides the drag-handle crop overlay over the image; the ratio/rotate/flip/resize icons still work normally either way. */
    private void toggleCustomCrop() {
        customCropActive = !customCropActive;
        if (customCropActive) {
            cropOverlayView.setImageBounds(computeImageDisplayBounds());
            cropOverlayView.setVisibility(View.VISIBLE);
            cropCustomActionsRow.setVisibility(View.VISIBLE);
        } else {
            cropOverlayView.setVisibility(View.GONE);
            cropCustomActionsRow.setVisibility(View.GONE);
        }
    }

    private void cancelCustomCrop() {
        customCropActive = false;
        cropOverlayView.setVisibility(View.GONE);
        cropCustomActionsRow.setVisibility(View.GONE);
    }

    private void confirmCustomCrop() {
        RectF normalizedRect = cropOverlayView.getNormalizedCropRect();
        cancelCustomCrop();
        presenter.onCustomCropRequested(normalizedRect);
    }

    /** Where the image actually sits inside mainImageView (fitCenter can letterbox), in the ImageView's own pixel space. */
    private RectF computeImageDisplayBounds() {
        Drawable drawable = mainImageView.getDrawable();
        if (drawable == null) {
            return new RectF(0, 0, mainImageView.getWidth(), mainImageView.getHeight());
        }
        RectF bounds = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        mainImageView.getImageMatrix().mapRect(bounds);
        return bounds;
    }

    private void setUpAdjustPanel() {
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        contrastSeekBar = findViewById(R.id.contrastSeekBar);
        saturationSeekBar = findViewById(R.id.saturationSeekBar);
        hueSeekBar = findViewById(R.id.hueSeekBar);
        exposureSeekBar = findViewById(R.id.exposureSeekBar);

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
        hueSeekBar.setOnSeekBarChangeListener(adjustListener);
        exposureSeekBar.setOnSeekBarChangeListener(adjustListener);
    }

    private void setUpAiPanel() {
        setUpToolIcon(R.id.aiEnhanceButton, R.drawable.ic_tab_ai, R.string.ai_tool_enhance, () -> presenter.onAiEnhanceRequested());
        setUpToolIcon(R.id.aiSharpenButton, R.drawable.ic_ai_sharpen, R.string.ai_tool_sharpen, () -> presenter.onSharpenRequested());
        setUpToolIcon(R.id.aiDenoiseButton, R.drawable.ic_ai_denoise, R.string.ai_tool_remove_noise, () -> presenter.onRemoveNoiseRequested());
        setUpToolIcon(R.id.aiUpscaleButton, R.drawable.ic_ai_upscale, R.string.ai_tool_upscale, () -> presenter.onUpscaleRequested());
        setUpToolIcon(R.id.aiBgRemovalButton, R.drawable.ic_ai_bg_removal, R.string.ai_tool_background_removal, () -> presenter.onBackgroundRemovalRequested());
    }

    private void setUpSaveButton() {
        saveTopBarButton = findViewById(R.id.saveTopBarButton);
        saveTopBarButton.setEnabled(false);
        saveTopBarButton.setOnClickListener(this::showSaveSharePopup);
    }

    /** Small popup anchored under the toolbar's Save icon offering Save / Share (Share stays locked until a save succeeds). */
    private void showSaveSharePopup(View anchor) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_save_share, null);
        View popupSaveRow = popupView.findViewById(R.id.popupSaveRow);
        View popupShareRow = popupView.findViewById(R.id.popupShareRow);
        popupShareRow.setEnabled(shareUnlocked);
        popupShareRow.setAlpha(shareUnlocked ? 1f : 0.4f);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(12f);

        popupSaveRow.setOnClickListener(v -> {
            popupWindow.dismiss();
            withStoragePermission(() -> presenter.onSaveClicked());
        });
        popupShareRow.setOnClickListener(v -> {
            if (!shareUnlocked) {
                return;
            }
            popupWindow.dismiss();
            withStoragePermission(() -> presenter.onShareClicked());
        });

        int gapPx = (int) (8 * getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchor, 0, gapPx, Gravity.END);
    }

    private void setUpToolIcon(int includeRootId, int iconRes, int labelRes, Runnable action) {
        View root = findViewById(includeRootId);
        ImageView icon = root.findViewById(R.id.toolIcon);
        TextView label = root.findViewById(R.id.toolLabel);
        icon.setImageResource(iconRes);
        label.setText(labelRes);
        root.setContentDescription(getString(labelRes));
        root.setOnClickListener(v -> action.run());
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        mainHandler.removeCallbacks(adjustRunnable);
        super.onDestroy();
    }

    private void runAutoActionIfRequested() {
        String autoAction = getIntent().getStringExtra(EXTRA_AUTO_ACTION);
        if (AUTO_ACTION_PICK.equals(autoAction)) {
            pickImageLauncher.launch(new String[]{"image/*"});
        } else if (AUTO_ACTION_CAMERA.equals(autoAction)) {
            withStoragePermission(this::launchCamera);
        } else if (AUTO_ACTION_PICK_THEN_AI.equals(autoAction)) {
            openAiTabOnNextImage = true;
            pickImageLauncher.launch(new String[]{"image/*"});
        }
    }

    private void showPickSourceDialog() {
        String[] options = {getString(R.string.action_library), getString(R.string.action_camera)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_pick_source_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageLauncher.launch(new String[]{"image/*"});
                    } else {
                        withStoragePermission(this::launchCamera);
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

    private void scheduleAdjustApply() {
        mainHandler.removeCallbacks(adjustRunnable);
        mainHandler.postDelayed(adjustRunnable, ADJUST_DEBOUNCE_MS);
    }

    private void applyAdjustValues() {
        int brightness = brightnessSeekBar.getProgress() - 100;
        int contrast = contrastSeekBar.getProgress();
        int saturation = saturationSeekBar.getProgress();
        int hue = hueSeekBar.getProgress() - 180;
        int exposure = exposureSeekBar.getProgress() - 100;
        presenter.onAdjustValuesChanged(brightness, contrast, saturation, hue, exposure);
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
        saveTopBarButton.setEnabled(true);
        shareUnlocked = false;
        for (View navButton : navButtons) {
            navButton.setEnabled(true);
        }
        if (openAiTabOnNextImage) {
            openAiTabOnNextImage = false;
            onTabTapped(TAB_AI);
        }
    }

    @Override
    public void showFilteredImage(Bitmap bitmap) {
        mainImageView.setImageBitmap(bitmap);
        shareUnlocked = false;
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
            shareUnlocked = true;
            Intent intent = new Intent(this, SaveResultActivity.class);
            intent.putExtra(SaveResultActivity.EXTRA_SAVED_URI, savedUri);
            startActivity(intent);
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
