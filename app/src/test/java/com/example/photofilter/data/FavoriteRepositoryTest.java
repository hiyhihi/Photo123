package com.example.photofilter.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class FavoriteRepositoryTest {

    private FavoriteRepository repository;

    @Before
    public void setUp() {
        repository = new FavoriteRepository(RuntimeEnvironment.getApplication());
    }

    @Test
    public void toggleFavorite_addsThenRemoves() {
        assertTrue(repository.getFavoriteIds().isEmpty());

        boolean afterFirstToggle = repository.toggleFavorite("sepia");
        assertTrue(afterFirstToggle);
        Set<String> favorites = repository.getFavoriteIds();
        assertTrue(favorites.contains("sepia"));

        boolean afterSecondToggle = repository.toggleFavorite("sepia");
        assertFalse(afterSecondToggle);
        assertFalse(repository.getFavoriteIds().contains("sepia"));
    }
}
