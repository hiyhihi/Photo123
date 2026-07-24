package com.example.photofilter.ui;

import android.content.Intent;
import android.os.Bundle;
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

/** Email/password sign-up. Firebase logs the new user in automatically on success. */
public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository authRepository = new AuthRepository();

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
        authRepository.signUp(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                showError(message);
            }
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
