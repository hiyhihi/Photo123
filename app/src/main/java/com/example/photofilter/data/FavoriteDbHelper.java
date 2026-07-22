package com.example.photofilter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Raw SQLite schema for the favorite-filter set (one row per favorited filter id). */
class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "photofilter_favorites.db";
    private static final int DATABASE_VERSION = 1;

    static final String TABLE_FAVORITES = "favorites";
    static final String COLUMN_FILTER_ID = "filter_id";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_FILTER_ID + " TEXT PRIMARY KEY NOT NULL)";

    FavoriteDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}
