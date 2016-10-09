package com.wfy.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 归属地查询工具
 * 操作数据库
 *
 * @author wfy
 */
public class CommonNumberDao {

    // 为了确保我们的数据库文件存在，所以我们在闪屏页，做了数据库文件的拷贝，把数据库文件拷到files目录下
    // 注意，该路径必须是这个路径，否则数据库访问不到
    private static final String PATH = "data/data/com.wfy.mobilesafe/files/commonnum.db";

    /**
     * 获取组的信息
     *
     * @return
     */
    public static List<GroupInfo> getCommonNumberGroup() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.query("classlist", new String[]{"name", "idx"}, null, null, null, null, null);

        ArrayList<GroupInfo> groupInfos = new ArrayList<>();

        while (cursor.moveToNext()) {
            GroupInfo groupInfo = new GroupInfo();
            String name = cursor.getString(0);
            String idx = cursor.getString(1);
            groupInfo.name = name;
            groupInfo.idx = idx;

            //获取该组下的所有孩子,并封装到组对象中
            groupInfo.childInfos = getCommonNumberChildren(db, idx);
            groupInfos.add(groupInfo);
        }
        cursor.close();
        db.close();
        return groupInfos;
    }

    /**
     * 获取孩子的信息
     *
     * @param db
     * @param idx
     * @return
     */
    public static List<ChildInfo> getCommonNumberChildren(SQLiteDatabase db, String idx) {

        ArrayList<ChildInfo> childInfos = new ArrayList<>();
        Cursor cursor = db.query("table" + idx, new String[]{"number", "name"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            ChildInfo childInfo = new ChildInfo();
            String number = cursor.getString(0);
            String name = cursor.getString(1);

            childInfo.number = number;
            childInfo.name = name;

            childInfos.add(childInfo);
        }
        cursor.close();
        return childInfos;
    }

    /**
     * 封装组的信息
     */
    public static class GroupInfo {
        public String name;
        public String idx;
        //封装所有孩子的信息
        public List<ChildInfo> childInfos;
    }

    /**
     * 封装孩子的信息
     */
    public static class ChildInfo {
        public String number;
        public String name;
    }
}
