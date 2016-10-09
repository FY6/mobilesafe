package com.wfy.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import com.wfy.domain.BlackNumberInfo;
import com.wfy.mobilesafe.utils.BlackNumberDaoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单Dao
 * Created by wfy on 2016/5/31.
 */
public class BlackNumberDao {

    private final BlackNumberOpenHelper helper;

    public BlackNumberDao(Context context) {
        helper = new BlackNumberOpenHelper(context);
    }

    /**
     * 添加黑名单
     *
     * @param number 电话号码
     * @param mode   拦截模式
     */
    public boolean add(String number, String mode) {
        SQLiteDatabase db = null;
        try {
            // db = helper.getWritableDatabase();
            db = BlackNumberDaoUtils.openWritableDatabase(helper);
            ContentValues contentValues = new ContentValues();
            contentValues.put("number", number);
            contentValues.put("mode", mode);
            long row = db.insert("blacknumber", null, contentValues);
            /**
             * if(row<0){ return false}else{return true}
             */
            return row < 0 ? false : true;
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, null);
        }

    }

    /**
     * 通过电话号码，删除黑名单
     *
     * @param number 电话号码
     * @return 被删除记录的_id
     */
    public boolean delete(String number) {
        SQLiteDatabase db = null;
        try {
            //db = helper.getWritableDatabase();
            db = BlackNumberDaoUtils.openWritableDatabase(helper);
            int rowId = db.delete("blacknumber", "number = ?", new String[]{number});
            /**
             *  if (rowId == 0) {return false;} else {return true;}
             */
            return rowId == 0 ? false : true;
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, null);
        }


    }

    /**
     * 通过电话号码 修改拦截的模式，即，是要拦截电话？还是拦截短信，还是电话和短信全部拦截
     *
     * @param number 电话号码
     * @return
     */
    public boolean changNumbrerMode(String number, String mode) {
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put("mode", mode);
        try {
            // db = helper.getWritableDatabase();
            db = BlackNumberDaoUtils.openWritableDatabase(helper);
            int blacknumber = db.update("blacknumber", values, "number = ?",
                    new String[]{number});
            /**
             * if(blacknumber==0){ return false}else{return true}
             */
            return blacknumber == 0 ? false : true;
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, null);
        }

    }

    /**
     * 通过电话号码，查找该号码的拦截模式
     *
     * @param number 电话号码
     * @return
     */
    public String findBlackNumber(String number) {
        String mode = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            // db = helper.getReadableDatabase();
            db = BlackNumberDaoUtils.opentReadableDatabase(helper);
            cursor = db.query("blacknumber", new String[]{"mode"}, "number = ?",
                    new String[]{number}, null, null, null);
            if (cursor.moveToNext()) {
                mode = cursor.getString(cursor.getColumnIndex("mode"));
            }
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, cursor);
        }
        return mode;
    }

    /**
     * 获取所有的黑名单
     *
     * @return List<BlackNumberInfo>
     */
    public List<BlackNumberInfo> findAllBlackNumber() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<BlackNumberInfo> blackNumberInfos = null;
        try {
            //db = helper.getReadableDatabase();
            db = BlackNumberDaoUtils.opentReadableDatabase(helper);
            //模拟耗时操作
            SystemClock.sleep(3000);
            cursor = db.query("blacknumber", null, null, null, null, null, null);
            blackNumberInfos = new ArrayList<>();
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex("number"));
                String mode = cursor.getString(cursor.getColumnIndex("mode"));
                BlackNumberInfo numberInfo = new BlackNumberInfo(number, mode);
                blackNumberInfos.add(numberInfo);
            }
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, cursor);
        }
        return blackNumberInfos;
    }

    /**
     * 分页加载数据
     *
     * @param pageNum  页码
     * @param pageSize 一页显示多少条数据
     *                 <p/>
     *                 limit 限制只返回几条数据
     *                 offset 从第几条开始返回数据
     * @return
     */
    public List<BlackNumberInfo> findPar(int pageNum, int pageSize) {
        SQLiteDatabase db = null;
        List<BlackNumberInfo> infos = new ArrayList<>();
        Cursor cursor = null;
        try {
            db = BlackNumberDaoUtils.opentReadableDatabase(helper);
            cursor = db.rawQuery("select number,mode from blacknumber limit ? offset ?",
                    new String[]{String.valueOf(pageSize), String.valueOf(pageNum * pageSize)});
            while (cursor.moveToNext()) {
                String number = cursor.getString(0);
                String mode = cursor.getString(1);
                BlackNumberInfo numberInfo = new BlackNumberInfo(number, mode);
                infos.add(numberInfo);
            }
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, cursor);
        }
        return infos;
    }

    /**
     * 分批加载
     *
     * @param startIndex 开始的索引
     * @param maxCount   最大返回几条数据
     * @return
     */
    public List<BlackNumberInfo> findPar2(int startIndex, int maxCount) {
        SQLiteDatabase db = null;
        List<BlackNumberInfo> infos = new ArrayList<>();
        Cursor cursor = null;
        try {
            db = BlackNumberDaoUtils.opentReadableDatabase(helper);
            SystemClock.sleep(3000);
            cursor = db.rawQuery("select number,mode from blacknumber limit ? offset ?",
                    new String[]{String.valueOf(maxCount), String.valueOf(startIndex)});
            while (cursor.moveToNext()) {
                String number = cursor.getString(0);
                String mode = cursor.getString(1);
                BlackNumberInfo numberInfo = new BlackNumberInfo(number, mode);
                infos.add(numberInfo);
            }
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, cursor);
        }
        return infos;
    }

    /**
     * 获取总的页数
     *
     * @return
     */
    public int getTotalPageNumber() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int total = 0;
        try {
            db = BlackNumberDaoUtils.opentReadableDatabase(helper);
            cursor = db.rawQuery("select count(*) from blacknumber", null);
            if (cursor.moveToNext()) {
                total = Integer.parseInt(cursor.getString(0));
            }
        } finally {
            //释放资源
            BlackNumberDaoUtils.release(db, cursor);
        }
        return total;
    }
}
