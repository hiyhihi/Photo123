# Editor Undo/Redo + Apply/Cancel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the "Crop → Original" no-op bug and give every editor tab (Bộ lọc/Cắt/Tuỳ chỉnh/AI) a draft → Apply/Cancel flow backed by a global, bounded Undo/Redo history.

**Architecture:** New package-private `EditHistory` class owns a pinned `pristineOriginal` bitmap plus a bounded (15-entry) undo/redo stack of committed bitmap snapshots. `EditorPresenter` gains a draft layer (`draftBaseBitmap`/`draftBitmap`/`draftLabel`) that every tool method mutates; `onApplyRequested()` commits the draft into `EditHistory`, `onCancelRequested()` discards it. Bộ lọc/Tuỳ chỉnh recompute from `draftBaseBitmap` (non-cumulative); Cắt/AI recompute from `draftBitmap` (cumulative within one tab session). "Original" always sources from `history.pristineOriginal()`, bypassing the cumulative chain — this is the actual bug fix.

**Tech Stack:** Java, Android SDK, Robolectric + JUnit for tests (existing stack, no new dependencies).

## Global Constraints

- `EditHistory` cap: exactly 15 entries in the undo stack (spec: `docs/superpowers/specs/2026-07-28-editor-history-apply-cancel-design.md`), `pristineOriginal` never evicted regardless of cap.
- No new libraries/dependencies.
- Reuse existing strings `action_confirm`/`action_cancel` for the new shared Apply/Cancel row (do not invent new ones).
- Package for `EditHistory`: `com.example.photofilter.presenter` (package-private, same visibility pattern as `FakeView`/`ImmediateExecutorService`).
- Keep `MainActivity`/`ui/` free of bitmap logic (existing project convention, see `README.md` "Kiến trúc" section) — all draft/history logic stays in `EditorPresenter`/`EditHistory`.

---

### Task 1: `EditHistory` class + unit tests

**Files:**
- Create: `app/src/main/java/com/example/photofilter/presenter/EditHistory.java`
- Test: `app/src/test/java/com/example/photofilter/presenter/EditHistoryTest.java`

**Interfaces:**
- Produces: `EditHistory` with methods `reset(Bitmap, String)`, `current()`, `currentLabel()`, `pristineOriginal()`, `canUndo()`, `canRedo()`, `commit(Bitmap, String)`, `undo()`, `redo()`, `clearAll()`. All package-private (no `public` modifier), matching `FakeView`'s visibility style.

- [ ] **Step 1: Write the failing test file**

Create `app/src/test/java/com/example/photofilter/presenter/EditHistoryTest.java`:

```java
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
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew testDebugUnitTest --tests "com.example.photofilter.presenter.EditHistoryTest" --console=plain`
Expected: FAIL — compile error, `EditHistory` does not exist yet.

- [ ] **Step 3: Implement `EditHistory`**

Create `app/src/main/java/com/example/photofilter/presenter/EditHistory.java`:

```java
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
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew testDebugUnitTest --tests "com.example.photofilter.presenter.EditHistoryTest" --console=plain`
Expected: PASS, 9 tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/photofilter/presenter/EditHistory.java app/src/test/java/com/example/photofilter/presenter/EditHistoryTest.java
git commit -m "feat(presenter): them EditHistory - undo/redo stack voi pristine-original ghim rieng"
```

---

### Task 2: Refactor `EditorContract` + `EditorPresenter` to the draft/history model, rewrite `EditorPresenterTest`

This task must land as one unit: the contract, presenter, `FakeView`, and presenter tests are mutually dependent and won't compile individually.

**Files:**
- Modify: `app/src/main/java/com/example/photofilter/presenter/EditorContract.java`
- Modify: `app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java`
- Modify: `app/src/test/java/com/example/photofilter/presenter/FakeView.java`
- Modify: `app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java`

**Interfaces:**
- Consumes: `EditHistory` from Task 1 (`reset`, `current`, `currentLabel`, `pristineOriginal`, `canUndo`, `canRedo`, `commit`, `undo`, `redo`, `clearAll`).
- Produces: `EditorContract.Presenter.onToolTabOpened()/onApplyRequested()/onCancelRequested()/onUndoRequested()/onRedoRequested()`; `EditorContract.View.showImage(Bitmap)` and `showUndoRedoAvailability(boolean, boolean)` (replacing `showOriginalImage`/`showFilteredImage`) — consumed by `MainActivity` in Task 5.

- [ ] **Step 1: Update `EditorContract`**

In `app/src/main/java/com/example/photofilter/presenter/EditorContract.java`, replace the `View` interface's `showOriginalImage`/`showFilteredImage` and add to `Presenter`:

```java
    interface View {
        void showFilterList(List<FilterItem> filters);

        void showImage(Bitmap bitmap);

        void showFilterThumbnails(List<FilterThumbnail> thumbnails);

        void showFavoriteIds(Set<String> favoriteFilterIds);

        void showLoading(boolean loading);

        void showError(String message);

        void showSaveResult(boolean success, Uri savedUri);

        void showUndoRedoAvailability(boolean canUndo, boolean canRedo);

        void launchShareIntent(Uri uri);
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void onImagePicked(Uri imageUri, int viewportWidth, int viewportHeight);

        /** Synchronous: creates the MediaStore target the camera app should write into. Returns null on failure. */
        Uri createCameraOutputUri();

        /** Call when a tool tab (Bộ lọc/Cắt/Tuỳ chỉnh/AI) becomes visible: starts a new draft from the current committed image. */
        void onToolTabOpened();

        /** Commits the current draft as one Undo step. No-op (same as Cancel) if the draft is unchanged. */
        void onApplyRequested();

        /** Discards the current draft; the displayed image reverts to the last committed state. */
        void onCancelRequested();

        void onUndoRequested();

        void onRedoRequested();

        void onFilterSelected(FilterItem filterItem);

        void onFavoriteToggled(FilterItem filterItem);

        void onAdjustValuesChanged(int brightness, int contrast, int saturation, int hue, int exposure);

        void onRotateRequested();

        void onFlipRequested();

        void onCropRequested(CropRatio ratio);

        /** @param normalizedRect free-form crop rect as fractions (0..1) of the image, from the drag-handle overlay. */
        void onCustomCropRequested(RectF normalizedRect);

        /** @param scalePercent target size as a percentage of the current image, e.g. 150 = 150%. */
        void onResizeRequested(int scalePercent);

        void onSharpenRequested();

        void onRemoveNoiseRequested();

        void onUpscaleRequested();

        void onBackgroundRemovalRequested();

        void onSaveClicked();

        void onShareClicked();
    }
```

- [ ] **Step 2: Rewrite `EditorPresenter`**

Replace the full content of `app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java`:

```java
package com.example.photofilter.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.photofilter.R;
import com.example.photofilter.data.AiToolsRepository;
import com.example.photofilter.data.BackgroundRemovalRepository;
import com.example.photofilter.data.CropRatio;
import com.example.photofilter.data.CropUtils;
import com.example.photofilter.data.FavoriteRepository;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.data.FilterRepository;
import com.example.photofilter.data.HistoryRepository;
import com.example.photofilter.data.ImageRepository;
import com.example.photofilter.domain.filter.ColorAdjustFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorPresenter implements EditorContract.Presenter {

    private static final int THUMBNAIL_MAX_DIMENSION = 200;

    private final Context appContext;
    private final ImageRepository imageRepository;
    private final FilterRepository filterRepository;
    private final HistoryRepository historyRepository;
    private final FavoriteRepository favoriteRepository;
    private final AiToolsRepository aiToolsRepository = new AiToolsRepository();
    private final BackgroundRemovalRepository backgroundRemovalRepository = new BackgroundRemovalRepository();
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final EditHistory history = new EditHistory();

    private EditorContract.View view;
    private List<FilterItem> availableFilters;
    private List<FilterThumbnail> currentThumbnails;
    private Set<String> favoriteIds;
    private Uri lastSavedUri;

    /** The image + label a tool tab started editing from; recomputed by non-cumulative tools (Bộ lọc/Tuỳ chỉnh). */
    private Bitmap draftBaseBitmap;
    /** The live preview; recomputed by cumulative tools (Cắt/AI) on top of itself, and shown to the View. */
    private Bitmap draftBitmap;
    private String draftLabel;

    /** Bumped on every new user action; stale async results compare against it and are discarded. */
    private int requestId;

    public EditorPresenter(Context context) {
        this(context, Executors.newSingleThreadExecutor(), new ImageRepository(),
                new HistoryRepository(context), new FavoriteRepository(context));
    }

    /** Visible for testing: lets tests inject a same-thread executor and fake repositories for deterministic results. */
    EditorPresenter(Context context, ExecutorService executor, ImageRepository imageRepository,
                     HistoryRepository historyRepository, FavoriteRepository favoriteRepository) {
        this.appContext = context.getApplicationContext();
        this.imageRepository = imageRepository;
        this.filterRepository = new FilterRepository();
        this.historyRepository = historyRepository;
        this.favoriteRepository = favoriteRepository;
        this.executor = executor;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void attachView(EditorContract.View view) {
        this.view = view;
        if (availableFilters == null) {
            availableFilters = filterRepository.getAvailableFilters(appContext);
        }
        view.showFilterList(availableFilters);
        if (history.current() != null) {
            view.showImage(history.current());
            view.showUndoRedoAvailability(history.canUndo(), history.canRedo());
        }
        if (currentThumbnails != null) {
            view.showFilterThumbnails(currentThumbnails);
        }
        if (favoriteIds != null) {
            view.showFavoriteIds(favoriteIds);
        } else {
            executor.execute(() -> {
                Set<String> loaded = favoriteRepository.getFavoriteIds();
                mainHandler.post(() -> {
                    favoriteIds = loaded;
                    if (view != null) {
                        view.showFavoriteIds(favoriteIds);
                    }
                });
            });
        }
    }

    @Override
    public void detachView() {
        this.view = null;
        executor.shutdown(); // let an in-flight save finish writing rather than corrupting the file
        clearDraft();
        history.clearAll();
        recycleThumbnails(currentThumbnails);
        currentThumbnails = null;
    }

    @Override
    public Uri createCameraOutputUri() {
        try {
            return imageRepository.createCameraOutputUri(appContext);
        } catch (IOException e) {
            if (view != null) {
                view.showError(appContext.getString(R.string.error_camera));
            }
            return null;
        }
    }

    @Override
    public void onImagePicked(Uri imageUri, int viewportWidth, int viewportHeight) {
        final int myRequestId = ++requestId;
        if (view != null) {
            view.showLoading(true);
        }
        executor.execute(() -> {
            try {
                Bitmap loaded = imageRepository.loadDownsampled(appContext, imageUri, viewportWidth, viewportHeight);
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        loaded.recycle();
                        return;
                    }
                    clearDraft();
                    history.reset(loaded, appContext.getString(R.string.filter_original));
                    view.showLoading(false);
                    afterHistoryChange();
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        return;
                    }
                    view.showLoading(false);
                    view.showError(appContext.getString(R.string.error_open_image));
                });
            }
        });
    }

    private void generateThumbnails(int myRequestId, Bitmap source) {
        executor.execute(() -> {
            Bitmap thumbSource = createDownscaledCopy(source, THUMBNAIL_MAX_DIMENSION);
            boolean sourceIsCopy = thumbSource != source;
            List<FilterThumbnail> thumbnails = new ArrayList<>(availableFilters.size());
            for (FilterItem item : availableFilters) {
                Bitmap result = item.getFilter().apply(thumbSource);
                thumbnails.add(new FilterThumbnail(item, result));
            }
            if (sourceIsCopy) {
                thumbSource.recycle();
            }
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    recycleThumbnails(thumbnails);
                    return;
                }
                currentThumbnails = thumbnails;
                view.showFilterThumbnails(thumbnails);
            });
        });
    }

    @Override
    public void onToolTabOpened() {
        if (history.current() == null) {
            return;
        }
        draftBaseBitmap = history.current();
        draftBitmap = draftBaseBitmap;
        draftLabel = history.currentLabel();
    }

    @Override
    public void onApplyRequested() {
        if (draftBitmap == null || draftBitmap == draftBaseBitmap) {
            onCancelRequested();
            return;
        }
        Bitmap committed = draftBitmap;
        String label = draftLabel != null ? draftLabel : history.currentLabel();
        draftBitmap = null;
        draftBaseBitmap = null;
        draftLabel = null;
        history.commit(committed, label);
        afterHistoryChange();
    }

    @Override
    public void onCancelRequested() {
        ++requestId; // discard any in-flight draft computation
        clearDraft();
        if (view != null) {
            view.showImage(history.current());
        }
    }

    @Override
    public void onUndoRequested() {
        if (!history.canUndo()) {
            return;
        }
        history.undo();
        afterHistoryChange();
    }

    @Override
    public void onRedoRequested() {
        if (!history.canRedo()) {
            return;
        }
        history.redo();
        afterHistoryChange();
    }

    /** Common tail for anything that changes the committed image: image pick, Apply, Undo, Redo. */
    private void afterHistoryChange() {
        lastSavedUri = null;
        if (view != null) {
            view.showImage(history.current());
            view.showUndoRedoAvailability(history.canUndo(), history.canRedo());
        }
        final int myRequestId = ++requestId;
        generateThumbnails(myRequestId, history.current());
    }

    private void clearDraft() {
        if (draftBitmap != null && draftBitmap != draftBaseBitmap) {
            recycleIfPossible(draftBitmap);
        }
        draftBitmap = null;
        draftBaseBitmap = null;
        draftLabel = null;
    }

    /** Replaces the draft bitmap, recycling the previous one unless it's still `draftBaseBitmap`/history-owned. */
    private void updateDraft(Bitmap newDraft, String label) {
        if (draftBitmap != null && draftBitmap != draftBaseBitmap) {
            recycleIfPossible(draftBitmap);
        }
        draftBitmap = newDraft;
        draftLabel = label;
    }

    @Override
    public void onFilterSelected(FilterItem filterItem) {
        final Bitmap source = draftBaseBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        executor.execute(() -> {
            Bitmap result = filterItem.getFilter().apply(source);
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                updateDraft(result, filterItem.getDisplayName());
                view.showImage(result);
            });
        });
    }

    @Override
    public void onFavoriteToggled(FilterItem filterItem) {
        executor.execute(() -> {
            boolean nowFavorite = favoriteRepository.toggleFavorite(filterItem.getId());
            mainHandler.post(() -> {
                if (favoriteIds == null) {
                    favoriteIds = new HashSet<>();
                }
                if (nowFavorite) {
                    favoriteIds.add(filterItem.getId());
                } else {
                    favoriteIds.remove(filterItem.getId());
                }
                if (view != null) {
                    view.showFavoriteIds(favoriteIds);
                }
            });
        });
    }

    @Override
    public void onAdjustValuesChanged(int brightness, int contrast, int saturation, int hue, int exposure) {
        final Bitmap source = draftBaseBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        executor.execute(() -> {
            Bitmap result = new ColorAdjustFilter(brightness, contrast, saturation, hue, exposure).apply(source);
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                updateDraft(result, appContext.getString(R.string.filter_adjust));
                view.showImage(result);
            });
        });
    }

    @Override
    public void onRotateRequested() {
        applyGeometryDraftOp(CropUtils::rotate90);
    }

    @Override
    public void onFlipRequested() {
        applyGeometryDraftOp(CropUtils::flipHorizontal);
    }

    @Override
    public void onCropRequested(CropRatio ratio) {
        if (ratio == CropRatio.ORIGINAL) {
            // Bypasses the cumulative draft chain on purpose: "Original" always means
            // the true pristine image, not "undo my last crop within this session".
            applyGeometryOpFrom(history.pristineOriginal(), source -> CropUtils.centerCrop(source, CropRatio.ORIGINAL));
            return;
        }
        applyGeometryDraftOp(source -> CropUtils.centerCrop(source, ratio));
    }

    @Override
    public void onCustomCropRequested(RectF normalizedRect) {
        applyGeometryDraftOp(source -> CropUtils.customCrop(source, normalizedRect));
    }

    @Override
    public void onResizeRequested(int scalePercent) {
        if (scalePercent == 100) {
            return;
        }
        applyGeometryDraftOp(source -> CropUtils.resize(source, scalePercent));
    }

    /** Cumulative geometry op: reads and replaces `draftBitmap` (Cắt tab keeps stacking Rotate/Flip/Crop/Resize). */
    private void applyGeometryDraftOp(GeometryOp op) {
        applyGeometryOpFrom(draftBitmap, op);
    }

    private void applyGeometryOpFrom(Bitmap source, GeometryOp op) {
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        executor.execute(() -> {
            Bitmap result = op.apply(source);
            mainHandler.post(() -> {
                if (myRequestId != requestId || view == null) {
                    result.recycle();
                    return;
                }
                updateDraft(result, draftLabel);
                view.showImage(result);
            });
        });
    }

    private interface GeometryOp {
        Bitmap apply(Bitmap source);
    }

    @Override
    public void onSharpenRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_sharpen), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::sharpen);
    }

    @Override
    public void onRemoveNoiseRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_remove_noise), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::removeNoise);
    }

    @Override
    public void onUpscaleRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_upscale), appContext.getString(R.string.error_ai_tool),
                aiToolsRepository::upscale);
    }

    @Override
    public void onBackgroundRemovalRequested() {
        applyAiToolDraft(appContext.getString(R.string.ai_tool_background_removal), appContext.getString(R.string.error_ai_tool),
                backgroundRemovalRepository::removeBackground);
    }

    /** Cumulative AI op: reads and replaces `draftBitmap`, so e.g. Sharpen then Upscale can stack before one Apply. */
    private void applyAiToolDraft(String resultLabel, String errorMessage, BitmapOp op) {
        final Bitmap source = draftBitmap;
        if (source == null) {
            return;
        }
        final int myRequestId = ++requestId;
        if (view != null) {
            view.showLoading(true);
        }
        executor.execute(() -> {
            try {
                Bitmap result = op.apply(source);
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        result.recycle();
                        return;
                    }
                    updateDraft(result, resultLabel);
                    view.showLoading(false);
                    view.showImage(result);
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (myRequestId != requestId || view == null) {
                        return;
                    }
                    view.showLoading(false);
                    String detail = e.getMessage();
                    view.showError(detail != null && !detail.isEmpty() ? errorMessage + ": " + detail : errorMessage);
                });
            }
        });
    }

    private interface BitmapOp {
        Bitmap apply(Bitmap source) throws IOException;
    }

    @Override
    public void onSaveClicked() {
        performSave(uri -> {
            if (view != null) {
                view.showSaveResult(true, uri);
            }
        }, message -> {
            if (view != null) {
                view.showSaveResult(false, null);
                view.showError(message);
            }
        });
    }

    @Override
    public void onShareClicked() {
        performSave(uri -> {
            if (view != null) {
                view.launchShareIntent(uri);
            }
        }, message -> {
            if (view != null) {
                view.showError(message);
            }
        });
    }

    private void performSave(OnSaved onSaved, OnSaveFailed onFailed) {
        Bitmap toSave = history.current();
        if (toSave == null) {
            return;
        }
        if (lastSavedUri != null) {
            onSaved.run(lastSavedUri);
            return;
        }
        String filterName = history.currentLabel();
        executor.execute(() -> {
            try {
                String name = "PhotoFilter_" + System.currentTimeMillis() + ".jpg";
                Uri uri = imageRepository.saveToGallery(appContext, toSave, name);
                historyRepository.insert(filterName, uri.toString(), System.currentTimeMillis());
                mainHandler.post(() -> {
                    lastSavedUri = uri;
                    onSaved.run(uri);
                });
            } catch (IOException e) {
                mainHandler.post(() -> onFailed.run(appContext.getString(R.string.error_save_image)));
            }
        });
    }

    private static Bitmap createDownscaledCopy(Bitmap source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scale = Math.min(1f, (float) maxDimension / Math.max(width, height));
        if (scale >= 1f) {
            return source;
        }
        int newWidth = Math.max(1, Math.round(width * scale));
        int newHeight = Math.max(1, Math.round(height * scale));
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    private static void recycleIfPossible(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private static void recycleThumbnails(List<FilterThumbnail> thumbnails) {
        if (thumbnails == null) {
            return;
        }
        for (FilterThumbnail thumbnail : thumbnails) {
            recycleIfPossible(thumbnail.getThumbnail());
        }
    }

    private interface OnSaved {
        void run(Uri uri);
    }

    private interface OnSaveFailed {
        void run(String message);
    }
}
```

- [ ] **Step 3: Update `FakeView`**

Replace the full content of `app/src/test/java/com/example/photofilter/presenter/FakeView.java`:

```java
package com.example.photofilter.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.photofilter.data.FilterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Records every call the presenter makes so tests can assert on interaction order. */
final class FakeView implements EditorContract.View {

    final List<List<FilterItem>> filterLists = new ArrayList<>();
    final List<Bitmap> images = new ArrayList<>();
    final List<List<FilterThumbnail>> thumbnailBatches = new ArrayList<>();
    final List<Boolean> loadingStates = new ArrayList<>();
    final List<String> errors = new ArrayList<>();
    final List<Boolean> undoAvailableStates = new ArrayList<>();
    final List<Boolean> redoAvailableStates = new ArrayList<>();
    Boolean lastSaveSuccess;
    Uri lastSavedUri;
    Uri lastSharedUri;
    Set<String> lastFavoriteIds;

    @Override
    public void showFilterList(List<FilterItem> filters) {
        filterLists.add(filters);
    }

    @Override
    public void showImage(Bitmap bitmap) {
        images.add(bitmap);
    }

    @Override
    public void showFilterThumbnails(List<FilterThumbnail> thumbnails) {
        thumbnailBatches.add(thumbnails);
    }

    @Override
    public void showLoading(boolean loading) {
        loadingStates.add(loading);
    }

    @Override
    public void showError(String message) {
        errors.add(message);
    }

    @Override
    public void showSaveResult(boolean success, Uri savedUri) {
        lastSaveSuccess = success;
        lastSavedUri = savedUri;
    }

    @Override
    public void showUndoRedoAvailability(boolean canUndo, boolean canRedo) {
        undoAvailableStates.add(canUndo);
        redoAvailableStates.add(canRedo);
    }

    @Override
    public void launchShareIntent(Uri uri) {
        lastSharedUri = uri;
    }

    @Override
    public void showFavoriteIds(Set<String> favoriteFilterIds) {
        lastFavoriteIds = favoriteFilterIds;
    }
}
```

- [ ] **Step 4: Rewrite `EditorPresenterTest`**

Replace the full content of `app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java`:

```java
package com.example.photofilter.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;

import com.example.photofilter.data.CropRatio;
import com.example.photofilter.data.FavoriteRepository;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.data.HistoryRepository;
import com.example.photofilter.data.ImageRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class EditorPresenterTest {

    private static final Uri FAKE_IMAGE_URI = Uri.parse("content://test/fake-image");
    private static final int FILTER_COUNT = 13;

    /**
     * Avoids depending on Robolectric's (currently unsupported) real image decoding.
     * Deliberately non-square (20x30): a square fixture would make width/height
     * assertions pass for rotate/square-crop/"Original" regardless of whether the
     * logic under test is actually correct.
     */
    private static class FakeImageRepository extends ImageRepository {
        @Override
        public Bitmap loadDownsampled(Context context, Uri uri, int reqWidth, int reqHeight) {
            return Bitmap.createBitmap(20, 30, Bitmap.Config.ARGB_8888);
        }
    }

    private EditorPresenter presenter;
    private FakeView view;

    @Before
    public void setUp() {
        presenter = new EditorPresenter(
                RuntimeEnvironment.getApplication(),
                new ImmediateExecutorService(),
                new FakeImageRepository(),
                new HistoryRepository(RuntimeEnvironment.getApplication()),
                new FavoriteRepository(RuntimeEnvironment.getApplication()));
        view = new FakeView();
        presenter.attachView(view);
        idleMainLooper();
    }

    private void idleMainLooper() {
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    private void pickImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();
    }

    @Test
    public void attachView_showsAllAvailableFilters() {
        assertEquals(1, view.filterLists.size());
        assertEquals(FILTER_COUNT, view.filterLists.get(0).size());
    }

    @Test
    public void onImagePicked_showsLoadingThenImageAndThumbnails() {
        pickImage();

        assertEquals(2, view.loadingStates.size());
        assertTrue("Phải bật loading trước", view.loadingStates.get(0));
        assertTrue("Phải tắt loading sau khi xong", !view.loadingStates.get(1));
        assertEquals(1, view.images.size());
        assertEquals(1, view.thumbnailBatches.size());
        assertEquals(FILTER_COUNT, view.thumbnailBatches.get(0).size());
    }

    @Test
    public void onFilterSelected_withoutImagePicked_doesNothing() {
        FilterItem grayscale = view.filterLists.get(0).get(1);

        presenter.onToolTabOpened();
        presenter.onFilterSelected(grayscale);
        idleMainLooper();

        assertEquals(0, view.images.size()); // no image ever picked, so showImage is never called
    }

    @Test
    public void onFilterSelected_thenApply_commitsOneHistoryStep() {
        pickImage();
        int imagesBefore = view.images.size();
        FilterItem grayscale = view.filterLists.get(0).get(1);

        presenter.onToolTabOpened();
        presenter.onFilterSelected(grayscale);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        assertTrue(view.images.size() > imagesBefore);
        assertTrue(view.undoAvailableStates.get(view.undoAvailableStates.size() - 1));
    }

    @Test
    public void onFilterSelected_thenCancel_doesNotEnableUndo() {
        pickImage();
        FilterItem grayscale = view.filterLists.get(0).get(1);

        presenter.onToolTabOpened();
        presenter.onFilterSelected(grayscale);
        idleMainLooper();
        presenter.onCancelRequested();

        assertFalse(view.undoAvailableStates.contains(true));
    }

    @Test
    public void onApplyRequested_withNoChange_doesNotEnableUndo() {
        pickImage();

        presenter.onToolTabOpened();
        presenter.onApplyRequested();
        idleMainLooper();

        assertFalse(view.undoAvailableStates.contains(true));
    }

    @Test
    public void onFavoriteToggled_updatesFavoriteIds() {
        FilterItem sepia = view.filterLists.get(0).get(3);

        presenter.onFavoriteToggled(sepia);
        idleMainLooper();

        assertTrue(view.lastFavoriteIds.contains(sepia.getId()));

        presenter.onFavoriteToggled(sepia);
        idleMainLooper();

        assertTrue(!view.lastFavoriteIds.contains(sepia.getId()));
    }

    @Test
    public void onAdjustValuesChanged_thenApply_showsAdjustedImage() {
        pickImage();
        int imagesBefore = view.images.size();

        presenter.onToolTabOpened();
        presenter.onAdjustValuesChanged(10, 110, 90, 0, 0);
        idleMainLooper();

        assertTrue(view.images.size() > imagesBefore);
    }

    @Test
    public void onSharpenRequested_thenApply_showsSharpenedImage() {
        pickImage();
        int imagesBefore = view.images.size();

        presenter.onToolTabOpened();
        presenter.onSharpenRequested();
        idleMainLooper();

        assertTrue(view.images.size() > imagesBefore);
    }

    @Test
    public void onResizeRequested_thenApply_replacesCommittedImage() {
        pickImage();

        presenter.onToolTabOpened();
        presenter.onResizeRequested(150);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        Bitmap committed = view.images.get(view.images.size() - 1);
        assertEquals(30, committed.getWidth());
    }

    @Test
    public void onResizeRequested_with100Percent_doesNothing() {
        pickImage();
        int imagesBefore = view.images.size();

        presenter.onToolTabOpened();
        presenter.onResizeRequested(100);
        idleMainLooper();

        assertEquals(imagesBefore, view.images.size());
    }

    @Test
    public void onRotateRequested_thenApply_swapsWidthAndHeight() {
        pickImage();
        int originalWidth = view.images.get(0).getWidth();
        int originalHeight = view.images.get(0).getHeight();

        presenter.onToolTabOpened();
        presenter.onRotateRequested();
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        Bitmap rotated = view.images.get(view.images.size() - 1);
        assertEquals(originalHeight, rotated.getWidth());
        assertEquals(originalWidth, rotated.getHeight());
    }

    @Test
    public void onCropRequested_original_afterPriorCropInEarlierSession_restoresPristineDimensions() {
        pickImage();
        int pristineWidth = view.images.get(0).getWidth();
        int pristineHeight = view.images.get(0).getHeight();

        // Session 1: crop to square and Apply — this is the committed state "Original" must escape.
        presenter.onToolTabOpened();
        presenter.onCropRequested(CropRatio.SQUARE);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        // Session 2: rotate (further squeezing the cumulative draft chain), then ask for Original.
        presenter.onToolTabOpened();
        presenter.onRotateRequested();
        idleMainLooper();
        presenter.onCropRequested(CropRatio.ORIGINAL);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        Bitmap result = view.images.get(view.images.size() - 1);
        assertEquals(pristineWidth, result.getWidth());
        assertEquals(pristineHeight, result.getHeight());
    }

    @Test
    public void onCropRequested_withSquareRatio_thenApply_replacesCommittedImage() {
        pickImage();
        int imagesBefore = view.images.size();

        presenter.onToolTabOpened();
        presenter.onCropRequested(CropRatio.SQUARE);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        assertTrue(view.images.size() > imagesBefore);
    }

    @Test
    public void onFlipRequested_thenApply_replacesCommittedImage() {
        pickImage();
        int imagesBefore = view.images.size();

        presenter.onToolTabOpened();
        presenter.onFlipRequested();
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        assertTrue(view.images.size() > imagesBefore);
    }

    @Test
    public void onUndoRequested_afterApply_restoresPreviousImageAndEnablesRedo() {
        pickImage();
        Bitmap beforeCrop = view.images.get(view.images.size() - 1);

        presenter.onToolTabOpened();
        presenter.onCropRequested(CropRatio.SQUARE);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        presenter.onUndoRequested();
        idleMainLooper();

        Bitmap afterUndo = view.images.get(view.images.size() - 1);
        assertEquals(beforeCrop.getWidth(), afterUndo.getWidth());
        assertEquals(beforeCrop.getHeight(), afterUndo.getHeight());
        assertTrue(view.redoAvailableStates.get(view.redoAvailableStates.size() - 1));
    }

    @Test
    public void onRedoRequested_afterUndo_reappliesChange() {
        pickImage();

        presenter.onToolTabOpened();
        presenter.onCropRequested(CropRatio.SQUARE);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();
        Bitmap afterCrop = view.images.get(view.images.size() - 1);

        presenter.onUndoRequested();
        idleMainLooper();
        presenter.onRedoRequested();
        idleMainLooper();

        Bitmap afterRedo = view.images.get(view.images.size() - 1);
        assertEquals(afterCrop.getWidth(), afterRedo.getWidth());
        assertEquals(afterCrop.getHeight(), afterRedo.getHeight());
    }
}
```

- [ ] **Step 5: Run all presenter tests**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew testDebugUnitTest --console=plain`
Expected: PASS — every test module (including domain filter tests, `CropUtilsTest`, `EditHistoryTest`) green.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/photofilter/presenter/EditorContract.java app/src/main/java/com/example/photofilter/presenter/EditorPresenter.java app/src/test/java/com/example/photofilter/presenter/FakeView.java app/src/test/java/com/example/photofilter/presenter/EditorPresenterTest.java
git commit -m "feat(presenter): chuyen EditorPresenter sang mo hinh draft + EditHistory, sua bug Crop Original"
```

---

### Task 3: New strings + Undo/Redo icons

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/drawable/ic_undo.xml`
- Create: `app/src/main/res/drawable/ic_redo.xml`

**Interfaces:**
- Produces: `@string/action_undo`, `@string/action_redo`, `@drawable/ic_undo`, `@drawable/ic_redo` — consumed by `activity_main.xml` in Task 4.

- [ ] **Step 1: Add the two new strings**

In `app/src/main/res/values/strings.xml`, right after the `action_cancel` line:

```xml
    <string name="action_confirm">Xác nhận</string>
    <string name="action_cancel">Huỷ</string>
    <string name="action_undo">Hoàn tác</string>
    <string name="action_redo">Làm lại</string>
    <string name="section_filters">Bộ lọc</string>
```

- [ ] **Step 2: Create the icons**

Create `app/src/main/res/drawable/ic_undo.xml` (counter-clockwise arrow, matches the existing hand-built 24dp stroke-arc style of `ic_rotate.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:pathData="M20,15 A8,8 0 1,0 9,6.5"
        android:strokeColor="#FFFFFF"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:fillColor="#00000000" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M10.5,2.5 L10.5,8 L5,5.2 Z" />
</vector>
```

Create `app/src/main/res/drawable/ic_redo.xml` (mirror of `ic_undo.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:pathData="M4,15 A8,8 0 1,1 15,6.5"
        android:strokeColor="#FFFFFF"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:fillColor="#00000000" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M13.5,2.5 L13.5,8 L19,5.2 Z" />
</vector>
```

- [ ] **Step 3: Verify resources compile**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew :app:processDebugResources --console=plain`
Expected: BUILD SUCCESSFUL (no "resource not found" / XML parse errors).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/drawable/ic_undo.xml app/src/main/res/drawable/ic_redo.xml
git commit -m "feat(resource): them chuoi va icon cho Hoan tac/Lam lai"
```

---

### Task 4: Layout — Undo/Redo top-bar buttons + shared Apply/Cancel row

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`

**Interfaces:**
- Consumes: `@string/action_undo`, `@string/action_redo`, `@drawable/ic_undo`, `@drawable/ic_redo` from Task 3.
- Produces: view ids `undoTopBarButton`, `redoTopBarButton`, `toolActionsRow`, `toolCancelButton`, `toolApplyButton` — consumed by `MainActivity` in Task 5.

- [ ] **Step 1: Add Undo/Redo buttons to the toolbar**

In `app/src/main/res/layout/activity_main.xml`, the `MaterialToolbar` currently ends with just `saveTopBarButton`. Insert the two new buttons before it (so `saveTopBarButton` stays rightmost):

```xml
        <ImageButton
            android:id="@+id/undoTopBarButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical|end"
            android:layout_marginEnd="96dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/action_undo"
            android:padding="10dp"
            android:src="@drawable/ic_undo"
            android:tint="@color/accent_yellow" />

        <ImageButton
            android:id="@+id/redoTopBarButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical|end"
            android:layout_marginEnd="48dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/action_redo"
            android:padding="10dp"
            android:src="@drawable/ic_redo"
            android:tint="@color/accent_yellow" />

        <ImageButton
            android:id="@+id/saveTopBarButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical|end"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="@string/action_save"
            android:padding="10dp"
            android:src="@drawable/ic_export_save"
            android:tint="@color/accent_yellow" />
```

(This replaces just the existing `saveTopBarButton` block — keep it as the last of the three, unchanged, and add the two new `ImageButton`s directly above it.)

- [ ] **Step 2: Wrap the scrollable panels + add the shared Apply/Cancel row**

Currently `toolSheet` (a `FrameLayout`) contains only the `NestedScrollView`. `FrameLayout` children overlay instead of stacking, so wrap both the scroll view and the new row in a vertical `LinearLayout`. Change:

```xml
    <FrameLayout
        android:id="@+id/toolSheet"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_sheet_top_rounded"
        android:elevation="16dp"
        android:paddingBottom="76dp"
        app:layout_behavior="@string/bottom_sheet_behavior"
        app:behavior_hideable="true"
        app:behavior_skipCollapsed="true"
        app:behavior_peekHeight="0dp">

        <androidx.core.widget.NestedScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:fillViewport="false">
```

to:

```xml
    <FrameLayout
        android:id="@+id/toolSheet"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_sheet_top_rounded"
        android:elevation="16dp"
        android:paddingBottom="76dp"
        app:layout_behavior="@string/bottom_sheet_behavior"
        app:behavior_hideable="true"
        app:behavior_skipCollapsed="true"
        app:behavior_peekHeight="0dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

        <androidx.core.widget.NestedScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:fillViewport="false">
```

Then, right after the existing `</androidx.core.widget.NestedScrollView>` closing tag (and before the outer `</FrameLayout>` that closes `toolSheet`), add the new row and close the wrapping `LinearLayout`. Change:

```xml
        </androidx.core.widget.NestedScrollView>

    </FrameLayout>

    <!-- Fixed icon-only editing toolbar
```

to:

```xml
        </androidx.core.widget.NestedScrollView>

        <LinearLayout
            android:id="@+id/toolActionsRow"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:paddingHorizontal="16dp"
            android:paddingTop="4dp"
            android:paddingBottom="12dp">

            <Button
                android:id="@+id/toolCancelButton"
                android:layout_width="0dp"
                android:layout_height="44dp"
                android:layout_weight="1"
                android:background="@drawable/bg_button_ghost"
                android:backgroundTint="@null"
                app:backgroundTint="@null"
                android:fontFamily="@font/inter_semibold"
                android:text="@string/action_cancel"
                android:textAllCaps="false"
                android:textColor="@color/accent_yellow"
                android:elevation="0dp" />

            <Button
                android:id="@+id/toolApplyButton"
                android:layout_width="0dp"
                android:layout_height="44dp"
                android:layout_weight="1"
                android:layout_marginStart="8dp"
                android:background="@drawable/bg_button_primary"
                android:backgroundTint="@null"
                app:backgroundTint="@null"
                android:fontFamily="@font/inter_semibold"
                android:text="@string/action_confirm"
                android:textAllCaps="false"
                android:textColor="@color/text_on_accent"
                android:elevation="0dp" />

        </LinearLayout>

        </LinearLayout>

    </FrameLayout>

    <!-- Fixed icon-only editing toolbar
```

- [ ] **Step 3: Verify the layout compiles**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew :app:processDebugResources --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/activity_main.xml
git commit -m "feat(ui): them nut Hoan tac/Lam lai tren top bar va hang Ap dung/Huy dung chung cho ca 4 tab"
```

---

### Task 5: Wire `MainActivity` to the new contract

**Files:**
- Modify: `app/src/main/java/com/example/photofilter/ui/MainActivity.java`

**Interfaces:**
- Consumes: `EditorContract.Presenter.onToolTabOpened/onApplyRequested/onCancelRequested/onUndoRequested/onRedoRequested` (Task 2), `EditorContract.View.showImage/showUndoRedoAvailability` (Task 2), view ids `undoTopBarButton`/`redoTopBarButton`/`toolActionsRow`/`toolCancelButton`/`toolApplyButton` (Task 4).

- [ ] **Step 1: Replace `showOriginalImage`/`showFilteredImage` with `showImage`, add `showUndoRedoAvailability`**

In `app/src/main/java/com/example/photofilter/ui/MainActivity.java`, replace:

```java
    @Override
    public void showOriginalImage(Bitmap bitmap) {
        emptyStateText.setVisibility(View.GONE);
        mainImageView.setImageBitmap(bitmap);
        saveTopBarButton.setEnabled(true);
        for (View navButton : navButtons) {
            navButton.setEnabled(true);
        }
        if (openAiTabOnNextImage) {
            openAiTabOnNextImage = false;
            onTabTapped(TAB_AI);
        }
    }

    @Override
    public void showFilteredImage(Bitmap bitmap) {
        mainImageView.setImageBitmap(bitmap);
    }
```

with:

```java
    @Override
    public void showImage(Bitmap bitmap) {
        emptyStateText.setVisibility(View.GONE);
        mainImageView.setImageBitmap(bitmap);
        saveTopBarButton.setEnabled(true);
        for (View navButton : navButtons) {
            navButton.setEnabled(true);
        }
        if (openAiTabOnNextImage) {
            openAiTabOnNextImage = false;
            onTabTapped(TAB_AI);
        }
    }

    @Override
    public void showUndoRedoAvailability(boolean canUndo, boolean canRedo) {
        undoTopBarButton.setEnabled(canUndo);
        undoTopBarButton.setAlpha(canUndo ? 1f : 0.35f);
        redoTopBarButton.setEnabled(canRedo);
        redoTopBarButton.setAlpha(canRedo ? 1f : 0.35f);
    }
```

- [ ] **Step 2: Add the new top-bar button fields and wire them**

Add two fields next to the existing `saveTopBarButton` field declaration:

```java
    private View saveTopBarButton;
    private View undoTopBarButton;
    private View redoTopBarButton;
```

Add a new setup method, mirroring `setUpSaveButton()`:

```java
    private void setUpUndoRedoButtons() {
        undoTopBarButton = findViewById(R.id.undoTopBarButton);
        redoTopBarButton = findViewById(R.id.redoTopBarButton);
        undoTopBarButton.setEnabled(false);
        redoTopBarButton.setEnabled(false);
        undoTopBarButton.setAlpha(0.35f);
        redoTopBarButton.setAlpha(0.35f);
        undoTopBarButton.setOnClickListener(v -> presenter.onUndoRequested());
        redoTopBarButton.setOnClickListener(v -> presenter.onRedoRequested());
    }
```

Call it from `onCreate`, next to the other `setUp...()` calls:

```java
        setUpBottomSheet();
        setUpFilterList();
        setUpCropPanel();
        setUpAdjustPanel();
        setUpAiPanel();
        setUpSaveButton();
        setUpUndoRedoButtons();
        setUpToolActionsRow();
```

- [ ] **Step 3: Add the shared Apply/Cancel row wiring**

Add a new method:

```java
    private void setUpToolActionsRow() {
        findViewById(R.id.toolCancelButton).setOnClickListener(v -> {
            if (customCropActive) {
                cancelCustomCrop();
            }
            presenter.onCancelRequested();
            closeSheet();
        });
        findViewById(R.id.toolApplyButton).setOnClickListener(v -> {
            if (customCropActive) {
                cancelCustomCrop();
            }
            presenter.onApplyRequested();
            closeSheet();
        });
    }

    private void closeSheet() {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        setNavSelected(-1);
        activeTab = -1;
    }
```

- [ ] **Step 4: Drive drafts from tab open/close**

Replace `onTabTapped` and `showTab`:

```java
    /** Tapping the open tab collapses it (cancelling its draft); tapping a different tab cancels the old draft, then opens a fresh one once hidden. */
    private void onTabTapped(int tab) {
        boolean sheetOpen = sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                || sheetBehavior.getState() == BottomSheetBehavior.STATE_SETTLING;
        if (activeTab == tab && sheetOpen) {
            if (customCropActive) {
                cancelCustomCrop();
            }
            presenter.onCancelRequested();
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            setNavSelected(-1);
            activeTab = -1;
            return;
        }
        if (sheetOpen) {
            if (customCropActive) {
                cancelCustomCrop();
            }
            presenter.onCancelRequested();
            pendingTab = tab;
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            showTab(tab);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void showTab(int tab) {
        if (tab != TAB_CROP && customCropActive) {
            cancelCustomCrop();
        }
        for (View panel : panels) {
            panel.setVisibility(View.GONE);
        }
        panels[tab].setVisibility(View.VISIBLE);
        setNavSelected(tab);
        activeTab = tab;
        presenter.onToolTabOpened();
    }
```

- [ ] **Step 5: Build the app**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew assembleDebug --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/photofilter/ui/MainActivity.java
git commit -m "feat(ui): noi Undo/Redo va hang Ap dung/Huy dung chung vao MainActivity"
```

---

### Task 6: Manual verification on a running app

**Files:** none (verification only).

- [ ] **Step 1: Install and launch**

Run: `JAVA_HOME="/c/Program Files/Java/jdk-17" ANDROID_HOME="/d/Android/Sdk" ./gradlew installDebug --console=plain` with an emulator/device running, then open the app.

- [ ] **Step 2: Walk through the regression scenario for the reported bug**

Pick an image → open Cắt → tap 1:1 → Apply → open Cắt again → tap Xoay → tap "Gốc" (Original) → Apply. Expected: image returns to the exact original picked photo (not the squared/rotated intermediate).

- [ ] **Step 3: Walk through Apply/Cancel on every tab**

For each of Bộ lọc / Cắt / Tuỳ chỉnh / AI: open the tab, make a change, tap Huỷ — expect the image to revert with no Undo entry created (Undo button stays disabled if it was the first edit). Repeat and tap Xác nhận instead — expect the change to stick and the Undo button to become enabled.

- [ ] **Step 4: Walk through Undo/Redo**

Apply 3 different edits (e.g. filter, crop, adjust). Tap Undo three times — expect to land back on the original image, Undo disabled. Tap Redo three times — expect to return to the last edited state, Redo disabled again.

- [ ] **Step 5: Confirm Save still works**

Tap Lưu after a few edits — expect the save-result screen to show the currently-edited image (not a stale one).

- [ ] **Step 6: Report back**

No commit for this task — report results in chat; file follow-up bugs as new tasks if anything above fails.
