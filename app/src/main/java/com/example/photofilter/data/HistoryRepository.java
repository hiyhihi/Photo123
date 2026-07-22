package com.example.photofilter.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads/writes the edit-history table. Every method does blocking disk I/O
 * and must be called from a background thread (never the main thread).
 */
public class HistoryRepository {

    private final HistoryDbHelper dbHelper;

    public HistoryRepository(Context context) {
        this.dbHelper = new HistoryDbHelper(context);
    }

    public void insert(String filterName, String imageUri, long timestampMillis) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HistoryDbHelper.COLUMN_FILTER_NAME, filterName);
        values.put(HistoryDbHelper.COLUMN_IMAGE_URI, imageUri);
        values.put(HistoryDbHelper.COLUMN_CREATED_AT, timestampMillis);
        db.insert(HistoryDbHelper.TABLE_HISTORY, null, values);
    }

    public List<HistoryEntry> getAll() {
        List<HistoryEntry> entries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String orderBy = HistoryDbHelper.COLUMN_CREATED_AT + " DESC";
        try (Cursor cursor = db.query(
                HistoryDbHelper.TABLE_HISTORY,
                null, null, null, null, null, orderBy)) {
            int idIndex = cursor.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_FILTER_NAME);
            int uriIndex = cursor.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_IMAGE_URI);
            int timeIndex = cursor.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_CREATED_AT);
            while (cursor.moveToNext()) {
                entries.add(new HistoryEntry(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(uriIndex),
                        cursor.getLong(timeIndex)));
            }
        }
        return entries;
    }
}
