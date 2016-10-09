package com.wfy.mobilesafe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密
 *
 * @author wfy
 */
public class MD5Utils {
    //加密
    public static String encode(String password) {

        MessageDigest digest = null;
        StringBuilder sb = new StringBuilder();
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] b = digest.digest(password.getBytes());

            for (byte c : b) {
                int i = c & 0xff;// 获取字节的低八位有效值 0000000011111111
                String hexString = Integer.toHexString(i);

                if (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取文件的MD5（病毒特征码）
     * 对文件进行md5加密
     *
     * @param sourceDir
     * @return
     */
    public static String getMD5File(String sourceDir) {
        File file = new File(sourceDir);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            while ((len = fis.read(buff)) != -1) {
                md5.update(buff, 0, len);
            }
            byte[] digest = md5.digest();
            StringBuffer sb = new StringBuffer();
            for (byte d : digest) {
                int b = d & 0xff;
                String s = Integer.toHexString(b);
                if (s.length() < 2) {
                    sb.append("0" + s);
                } else {
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
