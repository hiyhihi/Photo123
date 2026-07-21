package com.example.photofilter.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
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

import java.util.List;

/**
 * View layer only: renders whatever {@link EditorPresenter} tells it to and
 * forwards user actions back to the presenter. No filter/bitmap logic here.
 */
public class MainActivity extends AppCompatActivity implements EditorContract.View {

    private ImageView mainImageView;
    private TextView emptyStateText;
    private ProgressBar progressBar;
    private Button pickImageButton;
    private Button saveButton;
    private Button shareButton;

    private FilterAdapter filterAdapter;
    private EditorContract.Presenter presenter;

    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Runnable pendingStorageAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainImageView = findViewById(R.id.mainImageView);
        emptyStateText = findViewById(R.id.emptyStateText);
        progressBar = findViewById(R.id.progressBar);
        pickImageButton = findViewById(R.id.pickImageButton);
        saveButton = findViewById(R.id.saveButton);
        shareButton = findViewById(R.id.shareButton);

        RecyclerView filterRecyclerView = findViewById(R.id.filterRecyclerView);
        filterAdapter = new FilterAdapter(this::onFilterClicked);
        filterRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterRecyclerView.setAdapter(filterAdapter);

        presenter = new EditorPresenter(this);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onImagePicked);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);

        pickImageButton.setOnClickListener(v -> pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        saveButton.setOnClickListener(v -> withStoragePermission(() -> presenter.onSaveClicked()));
        shareButton.setOnClickListener(v -> withStoragePermission(() -> presenter.onShareClicked()));

        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
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
