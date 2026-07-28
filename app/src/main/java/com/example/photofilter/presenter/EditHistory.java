package com.example.photofilter.presenter;

import android.graphics.Bitmap;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Owns the bitmap edit history for one editing session: a pinned
 * {@code pristineOriginal} (the picked/captured image, never evicted) plus a
 * bounded undo/redo stack of committed states. Bitmaps that fall out of the
 * bounded window are recycled; {@code pristineOriginal} is exempt so the
 * Crop "Original" tool can always jump back to it.
 */
final class EditHistory {

    private static final int MAX_ENTRIES = 15;

    private Bitmap pristineOriginal;
    private Entry current;
    private final Deque<Entry> undoStack = new ArrayDeque<>();
    private final Deque<Entry> redoStack = new ArrayDeque<>();

    static final class Entry {
        final Bitmap bitmap;
        final String label;

        Entry(Bitmap bitmap, String label) {
            this.bitmap = bitmap;
            this.label = label;
        }
    }

    /** Starts a fresh session: recycles any previous state, pins {@code freshBitmap} as the pristine original. */
    void reset(Bitmap freshBitmap, String label) {
        clearAll();
        pristineOriginal = freshBitmap;
        current = new Entry(freshBitmap, label);
    }

    Bitmap current() {
        return current != null ? current.bitmap : null;
    }

    String currentLabel() {
        return current != null ? current.label : null;
    }

    Bitmap pristineOriginal() {
        return pristineOriginal;
    }

    boolean canUndo() {
        return !undoStack.isEmpty();
    }

    boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** Pushes the current state onto the undo stack and makes {@code newBitmap} current. Clears the redo stack. */
    void commit(Bitmap newBitmap, String label) {
        if (current != null) {
            undoStack.push(current);
        }
        trimIfNeeded();
        clearStack(redoStack);
        current = new Entry(newBitmap, label);
    }

    Bitmap undo() {
        if (!canUndo()) {
            return current();
        }
        redoStack.push(current);
        current = undoStack.pop();
        return current();
    }

    Bitmap redo() {
        if (!canRedo()) {
            return current();
        }
        undoStack.push(current);
        current = redoStack.pop();
        return current();
    }

    /** Recycles every bitmap this history owns. Call on new-image-pick and on presenter detach. */
    void clearAll() {
        recycleIfPossible(pristineOriginal);
        recycleEntry(current);
        clearStack(undoStack);
        clearStack(redoStack);
        pristineOriginal = null;
        current = null;
    }

    // The pristine entry is only ever pushed onto undoStack (never redoStack — see
    // EditHistoryTest for the traced invariant), and only leaves it by being popped
    // back into `current`. So only trimming needs to skip recycling it.
    private void trimIfNeeded() {
        while (undoStack.size() > MAX_ENTRIES) {
            Entry oldest = undoStack.removeLast();
            if (oldest.bitmap != pristineOriginal) {
                recycleIfPossible(oldest.bitmap);
            }
        }
    }

    private static void clearStack(Deque<Entry> stack) {
        while (!stack.isEmpty()) {
            recycleEntry(stack.pop());
        }
    }

    private static void recycleEntry(Entry entry) {
        if (entry != null) {
            recycleIfPossible(entry.bitmap);
        }
    }

    private static void recycleIfPossible(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
