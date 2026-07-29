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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * App entry point. Skips straight to {@link HomeActivity} if a local session
 * already exists; otherwise collects email/password and signs in against the
 * SQLite-backed account table.
 */
public class LoginActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AuthRepository authRepository;

    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorText;
    private Button loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(getApplicationContext());

        if (authRepository.isLoggedIn()) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.loginErrorText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.loginProgressBar);

        loginButton.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.goToRegisterText).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            String error = authRepository.signIn(email, password);
            mainHandler.post(() -> {
                if (isFinishing()) {
                    return;
                }
                if (error == null) {
                    goToHome();
                } else {
                    setLoading(false);
                    showError(error);
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void goToHome() {
        Class<?> target = authRepository.hasSeenIntro() ? HomeActivity.class : IntroActivity.class;
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
