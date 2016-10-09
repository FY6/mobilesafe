package com.wfy.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序锁Dao
 * Created by wfy on 2016/8/5.
 */
public class AppLocksDao {
    AppLocksOpenHelper helper = null;

    public AppLocksDao(Context context) {
        helper = new AppLocksOpenHelper(context);
    }

    public void add(String packageName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packageName", packageName);
        db.insert("locks", null, values);
        db.close();
    }

    public boolean find(String packageName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select packageName from locks where packageName = ?", new String[]{packageName});
        if (cursor.moveToNext()) {
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }

    public List<String> getAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ArrayList<String> strings = new ArrayList<>();
        Cursor cursor = db.rawQuery("select packageName from locks", null);
        while (cursor.moveToNext()) {
            strings.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return strings;
    }

    public int delete(String packageName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("locks", "packageName = ?", new String[]{packageName});
        db.close();
        return row;
    }

    public int deleteAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("locks", null, null);
    }
}
