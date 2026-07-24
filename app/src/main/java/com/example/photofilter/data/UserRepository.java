package com.example.photofilter.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Reads/writes the local accounts table. Every method does blocking disk I/O
 * and must be called from a background thread (never the main thread).
 */
public class UserRepository {

    private final UserDbHelper dbHelper;

    public UserRepository(Context context) {
        this.dbHelper = new UserDbHelper(context);
    }

    /** @return false if the email is already registered. */
    public boolean insertUser(String email, String passwordHash) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDbHelper.COLUMN_EMAIL, email);
        values.put(UserDbHelper.COLUMN_PASSWORD_HASH, passwordHash);
        try {
            return db.insertOrThrow(UserDbHelper.TABLE_USERS, null, values) != -1;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean credentialsMatch(String email, String passwordHash) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = UserDbHelper.COLUMN_EMAIL + " = ? AND " + UserDbHelper.COLUMN_PASSWORD_HASH + " = ?";
        try (Cursor cursor = db.query(
                UserDbHelper.TABLE_USERS,
                new String[]{UserDbHelper.COLUMN_ID},
                selection, new String[]{email, passwordHash}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                UserDbHelper.TABLE_USERS,
                new String[]{UserDbHelper.COLUMN_ID},
                UserDbHelper.COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }
}
