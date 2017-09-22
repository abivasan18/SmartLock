package com.abi.smartlogin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abi.smartlogin.entity.User;
import com.abi.smartlogin.entity.UserRole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    public void insert(User user) {
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("role", user.getRole().toString());
        values.put("wallpaper", user.getWallpaperId());
        values.put("confidence", user.getConfidence());
        database.insert("User", null, values);
    }

    public void update(User user) {
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("role", user.getRole().toString());
        values.put("wallpaper", user.getWallpaperId());
        values.put("confidence", user.getConfidence());
        database.update("User", values, "id = " + user.getId(), null);
    }

    public User getUser(int id) {
        User user = null;
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE id = " + id, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            user = this.fillUser(cursor);
        }
        cursor.close();
        return user;
    }

    public User getUser(String username) {
        User user = null;
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE username = ?", new String[]{username});
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            user = this.fillUser(cursor);
        }
        cursor.close();
        return user;
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM User", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            users.add(this.fillUser(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return users;
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        byte[] array = out.toByteArray();
        os.close();
        return array;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Object object = is.readObject();
        is.close();
        return object;
    }

    private User fillUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(0));
        user.setUsername(cursor.getString(1));
        user.setPassword(cursor.getString(2));
        user.setRole(UserRole.valueOf(cursor.getString(3)));
        user.setWallpaperId(cursor.getInt(4));
        user.setConfidence(cursor.getInt(5));
        return user;
    }


}
