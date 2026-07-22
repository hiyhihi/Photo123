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
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class EditorPresenterTest {

    private static final Uri FAKE_IMAGE_URI = Uri.parse("content://test/fake-image");
    private static final int FILTER_COUNT = 13;

    /** Avoids depending on Robolectric's (currently unsupported) real image decoding. */
    private static class FakeImageRepository extends ImageRepository {
        @Override
        public Bitmap loadDownsampled(Context context, Uri uri, int reqWidth, int reqHeight) {
            return Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
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

    @Test
    public void attachView_showsAllAvailableFilters() {
        assertEquals(1, view.filterLists.size());
        assertEquals(FILTER_COUNT, view.filterLists.get(0).size());
    }

    @Test
    public void onImagePicked_showsLoadingThenOriginalImageAndThumbnails() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        assertEquals(2, view.loadingStates.size());
        assertTrue("Phải bật loading trước", view.loadingStates.get(0));
        assertTrue("Phải tắt loading sau khi xong", !view.loadingStates.get(1));
        assertEquals(1, view.originalImages.size());
        assertEquals(1, view.thumbnailBatches.size());
        assertEquals(FILTER_COUNT, view.thumbnailBatches.get(0).size());
    }

    @Test
    public void onFilterSelected_withoutImagePicked_doesNothing() {
        FilterItem grayscale = view.filterLists.get(0).get(1);

        presenter.onFilterSelected(grayscale);
        idleMainLooper();

        assertTrue(view.filteredImages.isEmpty());
    }

    @Test
    public void onFilterSelected_afterImagePicked_showsFilteredImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        FilterItem grayscale = view.filterLists.get(0).get(1);
        presenter.onFilterSelected(grayscale);
        idleMainLooper();

        assertEquals(1, view.filteredImages.size());
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
    public void onAdjustValuesChanged_afterImagePicked_showsFilteredImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        presenter.onAdjustValuesChanged(10, 110, 90, 0, 0);
        idleMainLooper();

        assertEquals(1, view.filteredImages.size());
    }

    @Test
    public void onSharpenRequested_afterImagePicked_showsFilteredImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        presenter.onSharpenRequested();
        idleMainLooper();

        assertEquals(1, view.filteredImages.size());
    }

    @Test
    public void onRemoveNoiseRequested_afterImagePicked_showsFilteredImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        presenter.onRemoveNoiseRequested();
        idleMainLooper();

        assertEquals(1, view.filteredImages.size());
    }

    @Test
    public void onUpscaleRequested_afterImagePicked_showsFilteredImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        presenter.onUpscaleRequested();
        idleMainLooper();

        assertEquals(1, view.filteredImages.size());
    }

    @Test
    public void onResizeRequested_afterImagePicked_replacesOriginalImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();
        int countBefore = view.originalImages.size();

        presenter.onResizeRequested(150);
        idleMainLooper();
        idleMainLooper();

        assertEquals(countBefore + 1, view.originalImages.size());
        Bitmap resized = view.originalImages.get(view.originalImages.size() - 1);
        assertEquals(30, resized.getWidth());
    }

    @Test
    public void onResizeRequested_with100Percent_doesNothing() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();
        int countBefore = view.originalImages.size();

        presenter.onResizeRequested(100);
        idleMainLooper();

        assertEquals(countBefore, view.originalImages.size());
    }

    @Test
    public void onRotateRequested_swapsWidthAndHeight() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();

        int originalWidth = view.originalImages.get(0).getWidth();
        int originalHeight = view.originalImages.get(0).getHeight();

        presenter.onRotateRequested();
        idleMainLooper();
        idleMainLooper();

        Bitmap rotated = view.originalImages.get(view.originalImages.size() - 1);
        assertEquals(originalHeight, rotated.getWidth());
        assertEquals(originalWidth, rotated.getHeight());
    }

    @Test
    public void onFlipRequested_afterImagePicked_replacesOriginalImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();
        int countBefore = view.originalImages.size();

        presenter.onFlipRequested();
        idleMainLooper();
        idleMainLooper();

        assertEquals(countBefore + 1, view.originalImages.size());
    }

    @Test
    public void onCropRequested_withOriginalRatio_doesNothing() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();
        int countBefore = view.originalImages.size();

        presenter.onCropRequested(CropRatio.ORIGINAL);
        idleMainLooper();

        assertEquals(countBefore, view.originalImages.size());
    }

    @Test
    public void onCropRequested_withSquareRatio_replacesOriginalImage() {
        presenter.onImagePicked(FAKE_IMAGE_URI, 100, 100);
        idleMainLooper();
        idleMainLooper();
        int countBefore = view.originalImages.size();

        presenter.onCropRequested(CropRatio.SQUARE);
        idleMainLooper();
        idleMainLooper();

        assertEquals(countBefore + 1, view.originalImages.size());
    }
}
