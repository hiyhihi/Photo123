package com.example.photofilter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photofilter.R;
import com.example.photofilter.data.AuthRepository;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Email/password sign-up against the local SQLite account table; logs the new user in on success. */
public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AuthRepository authRepository;

    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private TextView errorText;
    private Button registerButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(getApplicationContext());

        MaterialToolbar toolbar = findViewById(R.id.registerToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        errorText = findViewById(R.id.registerErrorText);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.registerProgressBar);

        registerButton.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.goToLoginText).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    private void attemptRegister() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            showError(getString(R.string.error_empty_fields));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(getString(R.string.error_password_too_short));
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(getString(R.string.error_password_mismatch));
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            String error = authRepository.signUp(email, password);
            mainHandler.post(() -> {
                if (isFinishing()) {
                    return;
                }
                if (error == null) {
                    Class<?> target = authRepository.hasSeenIntro() ? HomeActivity.class : IntroActivity.class;
                    Intent intent = new Intent(RegisterActivity.this, target);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    setLoading(false);
                    showError(error);
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
