package com.example.photofilter.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.photofilter.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Shown right after a successful save: previews the saved photo and offers
 * Share or "keep editing" (which just returns to the still-alive MainActivity
 * underneath). Read-only, no business logic, so no MVP contract needed.
 */
public class SaveResultActivity extends AppCompatActivity {

    public static final String EXTRA_SAVED_URI = "com.example.photofilter.extra.SAVED_URI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_result);

        Uri savedUri = readSavedUriExtra();

        MaterialToolbar toolbar = findViewById(R.id.saveResultToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView imageView = findViewById(R.id.saveResultImageView);
        Glide.with(imageView).load(savedUri).into(imageView);

        findViewById(R.id.editMoreButton).setOnClickListener(v -> finish());
        findViewById(R.id.shareResultButton).setOnClickListener(v -> {
            if (savedUri == null) {
                return;
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, savedUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)));
        });
    }

    @SuppressWarnings("deprecation")
    private Uri readSavedUriExtra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getParcelableExtra(EXTRA_SAVED_URI, Uri.class);
        }
        return getIntent().getParcelableExtra(EXTRA_SAVED_URI);
    }
}
