package com.wfy.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 归属地查询工具 
 * 操作数据库
 * 
 * @author wfy
 * 
 */
public class AddressDao {

	// 为了确保我们的数据库文件存在，所以我们在闪屏页，做了数据库文件的拷贝，把数据库文件拷到files目录下
	private static final String PATH = "data/data/com.wfy.mobilesafe/files/address.db";// 注意，该路径必须是这个路径，否则数据库访问不到

	/**
	 * 归属地查询
	 * 
	 * @param number
	 * @return
	 */
	public static String getAddress(String number) {
		String address = "未知号码";
		// 获取数据库对象
		SQLiteDatabase database = SQLiteDatabase.openDatabase(PATH, null,
				SQLiteDatabase.OPEN_READONLY);
		String sql = "select location from data2 where id = (select outkey from data1 where id = ?)";

		// 手机号码 特点：1 + 345678 + （9位数字）
		// ^1[3-8]\\d{9}$
		// 匹配手机号码
		if (number.matches("^1[3-8]\\d{9}$")) {

			Cursor cursor = database.rawQuery(sql,
					new String[] { number.substring(0, 7) });
			if (cursor.moveToNext()) {
				address = cursor.getString(0);
			}
			cursor.close();
		} else if (number.matches("^\\d+$")) {

			switch (number.length()) {
			case 3:
				address = "报警号码";
				break;
			case 4:
				address = "模拟器";
				break;
			case 5:
				address = "客服电话";
				break;
			case 7:
			case 8:
				address = "本地电话";
				break;
			default:
				if (number.startsWith("0") && number.length() > 10) {// 有可能是长途电话
					// 区号有4位和3位(包括0)
					// 先查询4位
					Cursor cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { number.substring(1, 4) });
					if (cursor.moveToNext()) {
						address = cursor.getString(0);
					} else {
						cursor.close();

						cursor = database.rawQuery(
								"select location from data2 where area = ?",
								new String[] { number.substring(1, 3) });
						if (cursor.moveToNext()) {
							address = cursor.getString(0);
						}
						cursor.close();
					}
				}
				break;
			}
		}
		database.close();// 关闭数据库
		return address;
	}
}
