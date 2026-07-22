package com.example.photofilter.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class HistoryRepositoryTest {

    private HistoryRepository repository;

    @Before
    public void setUp() {
        repository = new HistoryRepository(RuntimeEnvironment.getApplication());
    }

    @Test
    public void getAll_returnsEmptyListWhenNothingSaved() {
        assertTrue(repository.getAll().isEmpty());
    }

    @Test
    public void insert_thenGetAll_returnsNewestFirst() {
        repository.insert("Trắng đen", "content://a", 1_000L);
        repository.insert("Sepia", "content://b", 2_000L);

        List<HistoryEntry> entries = repository.getAll();

        assertEquals(2, entries.size());
        assertEquals("Sepia", entries.get(0).getFilterName());
        assertEquals("Trắng đen", entries.get(1).getFilterName());
    }
}
