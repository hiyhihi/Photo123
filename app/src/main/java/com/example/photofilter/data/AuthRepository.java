package com.example.photofilter.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Thin wrapper around Firebase Authentication (email/password). No UI logic here. */
public class AuthRepository {

    public interface AuthCallback {
        void onSuccess();

        void onError(String message);
    }

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public void signIn(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(describeError(e)));
    }

    public void signUp(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(describeError(e)));
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    private String describeError(Exception e) {
        String message = e.getMessage();
        return message != null ? message : e.getClass().getSimpleName();
    }
}
