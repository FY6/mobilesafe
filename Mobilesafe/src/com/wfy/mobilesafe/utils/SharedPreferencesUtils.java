package com.wfy.mobilesafe.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wfy on 2016/7/24.
 */
public class SharedPreferencesUtils {
    public static final String SP_NAME = "config";

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences mPref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mPref.edit().putBoolean(key, value).commit();
    }


    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences mPref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return mPref.getBoolean(key, defValue);
    }
}
