package com.example.photofilter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Raw SQLite schema for local accounts (email + hashed password). */
class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "photofilter_users.db";
    private static final int DATABASE_VERSION = 1;

    static final String TABLE_USERS = "users";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_EMAIL = "email";
    static final String COLUMN_PASSWORD_HASH = "password_hash";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD_HASH + " TEXT NOT NULL)";

    UserDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
