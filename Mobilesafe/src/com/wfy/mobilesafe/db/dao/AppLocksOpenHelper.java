package com.wfy.mobilesafe.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 程序锁OpenHelper，操作数据库
 * Created by wfy on 2016/8/5.
 */
public class AppLocksOpenHelper extends SQLiteOpenHelper {
    public AppLocksOpenHelper(Context context) {
        super(context, "appLocks.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table locks(_id integer primary key autoincrement, packageName text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
