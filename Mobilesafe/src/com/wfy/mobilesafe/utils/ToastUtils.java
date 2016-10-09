package com.wfy.mobilesafe.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtils {
    public static void showToast(final Activity cxt, final String text) {
        if ("main".equals(Thread.currentThread().getName())) {
            Toast.makeText(cxt, text, Toast.LENGTH_SHORT).show();
        } else {
            cxt.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(cxt, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
