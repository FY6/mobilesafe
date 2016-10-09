package com.wfy.mobilesafe.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wfy.domain.Antivirus;
import com.wfy.domain.AntivirusScannerInfo;

/**
 * 病毒查杀Dao
 * Created by wfy on 2016/8/2.
 */
public class AntivirusDao {
    private static final String PATH = "data/data/com.wfy.mobilesafe/files/antivirus.db";

    public static boolean checkAntivirus(String md5, AntivirusScannerInfo info) {
        boolean isAntivirus = false;
        Cursor cursor = null;
        SQLiteDatabase db = null;

        String sql = "select desc from datable where md5 = ?";
        try {
            db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(sql, new String[]{md5});
            if (cursor.moveToNext()) {
                info.setDesc(cursor.getString(0));
                isAntivirus = true;
            } else {
                isAntivirus = false;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return isAntivirus;
    }

    /**
     * 查找数据库中是否已经存在该条记录
     *
     * @param md5
     * @return
     */
    public boolean findByMD5(String md5) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery("select desc from datable where md5 = ?", new String[]{md5});

            if (cursor.moveToNext()) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    //添加数据到病毒数据库
    public void add(Antivirus antivirus) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues contentValues = new ContentValues();
            contentValues.put("md5", antivirus.getMD5());
            contentValues.put("desc", antivirus.getDesc());
            contentValues.put("type", antivirus.getType());
            contentValues.put("name", antivirus.getName());

            db.insert("datable", null, contentValues);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
