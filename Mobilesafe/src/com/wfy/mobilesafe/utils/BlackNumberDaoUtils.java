package com.wfy.mobilesafe.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wfy.mobilesafe.db.dao.BlackNumberOpenHelper;

/**
 * 黑名单Dao工具类
 * 释放数据库和游标的资源
 * Created by wfy on 2016/6/3.
 */
public class BlackNumberDaoUtils {
    public static SQLiteDatabase openWritableDatabase(BlackNumberOpenHelper helper) {
        SQLiteDatabase db = null;
        if (helper != null) {
            db = helper.getWritableDatabase();
        }
        return db;
    }

    public static SQLiteDatabase opentReadableDatabase(BlackNumberOpenHelper helper) {
        SQLiteDatabase db = null;
        if (helper != null) {
            db = helper.getReadableDatabase();
        }
        return db;
    }

    //释放资源
    public static void release(SQLiteDatabase db, Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
