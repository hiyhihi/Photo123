package com.example.photofilter.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AuthRepositoryTest {

    private AuthRepository repository;

    @Before
    public void setUp() {
        repository = new AuthRepository(RuntimeEnvironment.getApplication());
    }

    @Test
    public void isLoggedIn_initiallyFalse() {
        assertFalse(repository.isLoggedIn());
    }

    @Test
    public void signUp_withNewEmail_logsUserInAndReturnsNull() {
        String error = repository.signUp("a@example.com", "password1");

        assertNull(error);
        assertTrue(repository.isLoggedIn());
        assertEquals("a@example.com", repository.getCurrentUserEmail());
    }

    @Test
    public void signUp_withExistingEmail_returnsError() {
        repository.signUp("a@example.com", "password1");
        repository.signOut();

        String error = repository.signUp("a@example.com", "password2");

        assertNotNull(error);
    }

    @Test
    public void signIn_withCorrectPassword_logsUserIn() {
        repository.signUp("a@example.com", "password1");
        repository.signOut();

        String error = repository.signIn("a@example.com", "password1");

        assertNull(error);
        assertTrue(repository.isLoggedIn());
    }

    @Test
    public void signIn_withWrongPassword_returnsErrorAndStaysLoggedOut() {
        repository.signUp("a@example.com", "password1");
        repository.signOut();

        String error = repository.signIn("a@example.com", "wrong-password");

        assertNotNull(error);
        assertFalse(repository.isLoggedIn());
    }

    @Test
    public void signOut_clearsSession() {
        repository.signUp("a@example.com", "password1");

        repository.signOut();

        assertFalse(repository.isLoggedIn());
        assertNull(repository.getCurrentUserEmail());
    }

    @Test
    public void hasSeenIntro_initiallyFalse() {
        assertFalse(repository.hasSeenIntro());
    }

    @Test
    public void markIntroSeen_persists() {
        repository.markIntroSeen();

        assertTrue(repository.hasSeenIntro());
    }
}
