package com.wfy.mobilesafe.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wfy on 2016/5/31.
 */
public class BlackNumberOpenHelper extends SQLiteOpenHelper {

    public BlackNumberOpenHelper(Context context) {
        super(context, "safe.db", null, 1);
    }


    /**
     * blacknumber:表名
     * _id：主键 自增长
     * number：电话号码
     * mode ：拦截模式  --> 电话拦截 短信拦截 全部拦截
     *
     * @param sqLiteDatabase
     */
    //第一次创建数据库时，调用次方法
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table blacknumber(_id Integer primary key autoincrement," +
                "number varchar(20), mode varchar(2))";
        sqLiteDatabase.execSQL(sql);
    }

    //数据库升级，调用此方法
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
