package com.example.photofilter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Raw SQLite schema for the edit-history table (one row per saved photo). */
class HistoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "photofilter_history.db";
    private static final int DATABASE_VERSION = 1;

    static final String TABLE_HISTORY = "history";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_FILTER_NAME = "filter_name";
    static final String COLUMN_IMAGE_URI = "image_uri";
    static final String COLUMN_CREATED_AT = "created_at";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FILTER_NAME + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_URI + " TEXT NOT NULL, " +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL)";

    HistoryDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }
}
