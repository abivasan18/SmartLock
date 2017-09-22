package com.abi.smartlogin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abi.smartlogin.R;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smartlogin.db";
    private static final int DB_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE User(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, role TEXT, wallpaper INTEGER, confidence INTEGER);");
        db.execSQL("INSERT INTO User(username, password, role, wallpaper, confidence) VALUES ('admin', 'admin', 'ADMIN', " + R.drawable.wallpaper_1 + ", 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}