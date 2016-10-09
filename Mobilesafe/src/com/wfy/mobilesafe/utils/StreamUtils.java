package com.wfy.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
	/**
	 * 把输入流转换为字符串
	 * 
	 * @param in
	 *            输入流
	 * @return 返回in转换为的字符串
	 */
	public static String readFromStream(InputStream in) {
		// 创建字节数组输出流
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// 定义缓冲区，大小为 1024kb
		byte[] buf = new byte[1024];
		int len = -1;
		String result = null;

		try {
			while ((len = in.read(buf)) != -1) {
				stream.write(buf, 0, len);
			}
			result = new String(buf, "utf-8");
			// result = stream.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
					stream = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
