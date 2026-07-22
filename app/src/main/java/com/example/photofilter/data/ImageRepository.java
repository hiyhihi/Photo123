package com.example.photofilter.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles all image I/O: reading a picked photo down-sampled to a target
 * size (so we never hold a full-resolution bitmap we don't need), and
 * writing the result back to the device gallery via MediaStore.
 */
public class ImageRepository {

    public Bitmap loadDownsampled(Context context, Uri uri, int reqWidth, int reqHeight) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        try (InputStream boundsStream = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(boundsStream, null, boundsOptions);
        }
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            throw new IOException("Không đọc được kích thước ảnh");
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight);
        decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap;
        try (InputStream dataStream = resolver.openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(dataStream, null, decodeOptions);
        }
        if (bitmap == null) {
            throw new IOException("Không thể giải mã ảnh");
        }

        return correctOrientation(resolver, uri, bitmap);
    }

    public Uri saveToGallery(Context context, Bitmap bitmap, String displayName) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PhotoFilter");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri itemUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (itemUri == null) {
            throw new IOException("Không thể tạo mục ảnh mới trong thư viện");
        }

        try (OutputStream out = resolver.openOutputStream(itemUri)) {
            if (out == null || !bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                throw new IOException("Nén/ghi ảnh thất bại");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues pendingDone = new ContentValues();
            pendingDone.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(itemUri, pendingDone, null, null);
        }
        return itemUri;
    }

    /** Creates a fresh MediaStore entry to hand to the camera app as the capture target. */
    public Uri createCameraOutputUri(Context context) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        String name = "HATFilter_camera_" + System.currentTimeMillis() + ".jpg";
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HATFilter");
        }
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Không thể tạo nơi lưu ảnh chụp");
        }
        return uri;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (reqWidth <= 0 || reqHeight <= 0) {
            return inSampleSize;
        }
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap correctOrientation(ContentResolver resolver, Uri uri, Bitmap bitmap) throws IOException {
        int rotationDegrees;
        try (InputStream exifStream = resolver.openInputStream(uri)) {
            if (exifStream == null) {
                return bitmap;
            }
            ExifInterface exif = new ExifInterface(exifStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            rotationDegrees = orientationToDegrees(orientation);
        }
        if (rotationDegrees == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) {
            bitmap.recycle();
        }
        return rotated;
    }

    private static int orientationToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
}
