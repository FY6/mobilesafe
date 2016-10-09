package com.wfy.mobilesafe.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 短信备份工具类
 * Created by wfy on 2016/6/24.
 */
public class SmsUtils {
    public interface SMSCallBack {
        void before(int count);

        void onBackUp(int progress);
    }

    public static boolean backup(Context context, SMSCallBack callBack) {
        /**
         * 步骤：
         * 1、判断设备的sd卡是否已经挂载，如果已经挂载就开始备份，否则不进行备份操作
         * 2、sms数据库是系统数据，没有权限直接进行操作，只能通过ContentProvider提供的接口对sms数据库操作
         * 3、把短信写到sd卡
         */
        //sd卡已经挂载
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.parse("content://sms");
            Cursor cursor = resolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);

            if (cursor != null) {
                int count = cursor.getCount();
                callBack.before(count);
                int progress = 0;

                FileOutputStream os = null;
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
                    os = new FileOutputStream(file);
                    XmlSerializer xmlSerializer = Xml.newSerializer();
                    xmlSerializer.setOutput(os, "UTF-8");

                    xmlSerializer.startDocument("UTF-8", true);
                    xmlSerializer.startTag(null, "smss");
                    xmlSerializer.attribute(null, "size", String.valueOf(count));
                    while (cursor.moveToNext()) {
                        xmlSerializer.startTag(null, "sms");
                        xmlSerializer.startTag(null, "address");
                        xmlSerializer.text(cursor.getString(0));
                        xmlSerializer.endTag(null, "address");

                        xmlSerializer.startTag(null, "date");
                        //格式化时间
                        Date d = new Date(Long.parseLong(cursor.getString(1)));
                        DateFormat simpleDateFormat = new SimpleDateFormat(("yyyy-MM-dd hh:mm:ss"));
                        String date = simpleDateFormat.format(d);
                        xmlSerializer.text(date);
                        xmlSerializer.endTag(null, "date");


                        xmlSerializer.startTag(null, "type");
                        xmlSerializer.text(cursor.getString(2));
                        xmlSerializer.endTag(null, "type");

                        xmlSerializer.startTag(null, "body");
                        //加密短信内容，github
                        //第一个参数：加密的种子，即秘钥 第二个参数：加密内容
                        xmlSerializer.text(Crypto.encrypt("123", cursor.getString(3)));
                        xmlSerializer.endTag(null, "body");
                        xmlSerializer.endTag(null, "sms");

                        SystemClock.sleep(300);
                        progress++;
                        callBack.onBackUp(progress);//更新进度
                    }
                    xmlSerializer.endTag(null, "smss");
                    xmlSerializer.endDocument();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                    try {
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(context, "没有可备份的短信", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
}
