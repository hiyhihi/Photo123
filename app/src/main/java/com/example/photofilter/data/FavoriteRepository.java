package com.example.photofilter.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.Set;

/** Tracks which filter ids the user has starred. All methods block on disk I/O. */
public class FavoriteRepository {

    private final FavoriteDbHelper dbHelper;

    public FavoriteRepository(Context context) {
        this.dbHelper = new FavoriteDbHelper(context);
    }

    public Set<String> getFavoriteIds() {
        Set<String> ids = new HashSet<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(FavoriteDbHelper.TABLE_FAVORITES,
                null, null, null, null, null, null)) {
            int idIndex = cursor.getColumnIndexOrThrow(FavoriteDbHelper.COLUMN_FILTER_ID);
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(idIndex));
            }
        }
        return ids;
    }

    public boolean toggleFavorite(String filterId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean isFavorite = getFavoriteIds().contains(filterId);
        if (isFavorite) {
            db.delete(FavoriteDbHelper.TABLE_FAVORITES,
                    FavoriteDbHelper.COLUMN_FILTER_ID + " = ?", new String[]{filterId});
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(FavoriteDbHelper.COLUMN_FILTER_ID, filterId);
        db.insert(FavoriteDbHelper.TABLE_FAVORITES, null, values);
        return true;
    }
}
