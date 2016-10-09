package com.wfy.mobilesafe.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import java.util.List;

/**
 * 服务状态工具类
 *
 * @author wfy
 */
public class ServiceStatusUtils {

    /**
     * 检测服务是否正在运行：
     * 拿到设备上所有正在运行的服务，通过类名对比，可知道该服务是否在运行
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String className) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningServices = am.getRunningServices(100);

        for (RunningServiceInfo runningServiceInfo : runningServices) {
            // System.out.println(runningServiceInfo.service.getClassName());
            if (className.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
