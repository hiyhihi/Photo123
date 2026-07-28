package com.example.photofilter.presenter;

import android.graphics.Bitmap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class EditHistoryTest {

    private static Bitmap bitmap() {
        return Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
    }

    @Test
    public void reset_setsCurrentAndPristineToSameBitmap() {
        EditHistory history = new EditHistory();
        Bitmap original = bitmap();

        history.reset(original, "Màu gốc");

        assertSame(original, history.current());
        assertSame(original, history.pristineOriginal());
        assertEquals("Màu gốc", history.currentLabel());
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void commit_pushesPreviousStateOntoUndoStack() {
        EditHistory history = new EditHistory();
        history.reset(bitmap(), "Màu gốc");
        Bitmap committed = bitmap();

        history.commit(committed, "Sepia");

        assertSame(committed, history.current());
        assertEquals("Sepia", history.currentLabel());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void undo_restoresPreviousStateAndEnablesRedo() {
        EditHistory history = new EditHistory();
        Bitmap original = bitmap();
        history.reset(original, "Màu gốc");
        history.commit(bitmap(), "Sepia");

        Bitmap restored = history.undo();

        assertSame(original, restored);
        assertSame(original, history.current());
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    public void redo_reappliesUndoneState() {
        EditHistory history = new EditHistory();
        history.reset(bitmap(), "Màu gốc");
        Bitmap committed = bitmap();
        history.commit(committed, "Sepia");
        history.undo();

        Bitmap redone = history.redo();

        assertSame(committed, redone);
        assertSame(committed, history.current());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void commit_afterUndo_clearsRedoStack() {
        EditHistory history = new EditHistory();
        history.reset(bitmap(), "Màu gốc");
        history.commit(bitmap(), "Sepia");
        history.undo();

        history.commit(bitmap(), "Vintage");

        assertFalse(history.canRedo());
    }

    @Test
    public void undo_whenStackEmpty_isNoOpAndReturnsCurrent() {
        EditHistory history = new EditHistory();
        Bitmap original = bitmap();
        history.reset(original, "Màu gốc");

        Bitmap result = history.undo();

        assertSame(original, result);
        assertFalse(history.canUndo());
    }

    @Test
    public void pristineOriginal_survivesExceedingUndoCap() {
        EditHistory history = new EditHistory();
        Bitmap original = bitmap();
        history.reset(original, "Màu gốc");

        // 16 commits: 1 more than the 15-entry cap, forces eviction of the oldest undo entry.
        for (int i = 0; i < 16; i++) {
            history.commit(bitmap(), "Bước " + i);
        }

        assertSame(original, history.pristineOriginal());
        assertFalse(original.isRecycled());
    }

    @Test
    public void clearAll_recyclesEverythingAndResetsState() {
        EditHistory history = new EditHistory();
        Bitmap original = bitmap();
        history.reset(original, "Màu gốc");
        Bitmap committed = bitmap();
        history.commit(committed, "Sepia");

        history.clearAll();

        assertTrue(original.isRecycled());
        assertTrue(committed.isRecycled());
        assertNull(history.current());
        assertNull(history.pristineOriginal());
    }
}
