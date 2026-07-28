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
import static org.junit.Assert.assertNotSame;
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
    public void onCropRequested_original_producesBitmapDistinctFromPristine() {
        // Regression guard for aliasing history.pristineOriginal() into the draft: AOSP's
        // Bitmap.createBitmap(Bitmap, 0, 0, w, h) has a fast path that returns the SAME
        // object (not a copy) for an immutable source when the full width/height is
        // requested with no matrix. Robolectric's Bitmap shadow may not reproduce that
        // short-circuit, so this test can pass even with the buggy code under test — it's
        // here to document/pin the identity contract, not to be the sole detection method.
        pickImage();
        Bitmap pristine = view.images.get(0);

        presenter.onToolTabOpened();
        presenter.onCropRequested(CropRatio.ORIGINAL);
        idleMainLooper();
        presenter.onApplyRequested();
        idleMainLooper();

        Bitmap result = view.images.get(view.images.size() - 1);
        assertNotSame("Crop 'Original' must never hand the pristine Bitmap object itself into the "
                + "draft/history chain — it must always be a fresh copy.", pristine, result);
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
