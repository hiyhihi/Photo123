package com.example.photofilter.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class UserRepositoryTest {

    private UserRepository repository;

    @Before
    public void setUp() {
        repository = new UserRepository(RuntimeEnvironment.getApplication());
    }

    @Test
    public void insertUser_thenEmailExists_returnsTrue() {
        assertTrue(repository.insertUser("a@example.com", "hash1"));

        assertTrue(repository.emailExists("a@example.com"));
    }

    @Test
    public void insertUser_withDuplicateEmail_returnsFalse() {
        repository.insertUser("a@example.com", "hash1");

        assertFalse(repository.insertUser("a@example.com", "hash2"));
    }

    @Test
    public void credentialsMatch_withCorrectHash_returnsTrue() {
        repository.insertUser("a@example.com", "hash1");

        assertTrue(repository.credentialsMatch("a@example.com", "hash1"));
    }

    @Test
    public void credentialsMatch_withWrongHash_returnsFalse() {
        repository.insertUser("a@example.com", "hash1");

        assertFalse(repository.credentialsMatch("a@example.com", "wrong"));
    }

    @Test
    public void emailExists_withUnknownEmail_returnsFalse() {
        assertFalse(repository.emailExists("nobody@example.com"));
    }
}
