package com.example.photofilter.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Local account system backed by SQLite (no server). Every method does
 * blocking disk I/O and must be called from a background thread. Passwords
 * are stored as a SHA-256 hash, never in plain text.
 */
public class AuthRepository {

    private static final String PREFS_NAME = "auth_session";
    private static final String KEY_CURRENT_EMAIL = "current_email";
    private static final String KEY_INTRO_SHOWN = "intro_shown";

    private final UserRepository userRepository;
    private final SharedPreferences prefs;

    public AuthRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.userRepository = new UserRepository(appContext);
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_CURRENT_EMAIL);
    }

    public String getCurrentUserEmail() {
        return prefs.getString(KEY_CURRENT_EMAIL, null);
    }

    /** Theo thiết bị, không theo tài khoản — dùng chung cho mọi user đăng nhập trên máy này. */
    public boolean hasSeenIntro() {
        return prefs.getBoolean(KEY_INTRO_SHOWN, false);
    }

    public void markIntroSeen() {
        prefs.edit().putBoolean(KEY_INTRO_SHOWN, true).apply();
    }

    /** @return null on success, or a user-facing error message. */
    public String signUp(String email, String password) {
        if (userRepository.emailExists(email)) {
            return "Email đã được sử dụng";
        }
        boolean inserted = userRepository.insertUser(email, hash(password));
        if (!inserted) {
            return "Email đã được sử dụng";
        }
        setCurrentUser(email);
        return null;
    }

    /** @return null on success, or a user-facing error message. */
    public String signIn(String email, String password) {
        if (!userRepository.credentialsMatch(email, hash(password))) {
            return "Sai email hoặc mật khẩu";
        }
        setCurrentUser(email);
        return null;
    }

    public void signOut() {
        prefs.edit().remove(KEY_CURRENT_EMAIL).apply();
    }

    private void setCurrentUser(String email) {
        prefs.edit().putString(KEY_CURRENT_EMAIL, email).apply();
    }

    private String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
