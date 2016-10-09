package com.wfy.mobilesafe.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Debug;

import com.wfy.domain.TaskInfo;
import com.wfy.mobilesafe.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 进程工具类
 * Created by wfy on 2016/7/3.
 */
public class TaskInfoParser {
    public static List<TaskInfo> getTaskInfos(Context context) {
        ArrayList<TaskInfo> taskInfos = new ArrayList<>();
        //拿到进程管理器（任务管理器或活动管理器）
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //拿到包管理器
        PackageManager packageManager = context.getPackageManager();
        //拿到所有正在运行的进程
        List<ActivityManager.RunningAppProcessInfo> appTasks = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : appTasks) {

            TaskInfo taskInfo = new TaskInfo();
            String processName = processInfo.processName;//进程名称
            int pid = processInfo.pid;//进程id
            try {
                /**
                 *  String[] pkgList = processInfo.pkgList;
                 *  for (String pkg : pkgList){System.out.println(processInfo.processName + "---" + pkg);}
                 *
                 * 通过打印一个进程中的所有包，知道有些包名和进程名称是不一致的，所以我们需要做一些处理
                 *系统：
                 * system---android
                 * ystem.out: system---com.android.providers.settings
                 * 核心：
                 * android.process.acore---com.android.providers.applications
                 * android.process.acore---com.android.providers.contacts
                 * android.process.acore---com.android.providers.userdictionary
                 * 媒体：
                 * android.process.media---com.android.providers.drm
                 * android.process.media---com.android.providers.downloads
                 * android.process.media---com.android.providers.media
                 */

                if ("system".equals(processName)) {
                    processName = "android";
                }
                if ("android.process.acore".equals(processName)) {
                    processName = "com.android.providers.applications";
                }
                if ("android.process.media".equals(processName)) {
                    processName = "com.android.providers.media";
                }

                taskInfo.setPackageName(processName);
                PackageInfo packageInfo = packageManager.getPackageInfo(processName, 0);
                //拿到图标
                Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                taskInfo.setIcon(icon);
                //进程名
                String appName = (String) packageInfo.applicationInfo.loadLabel(packageManager);
                taskInfo.setAppName(appName);
                //通过pid拿到进程的占用内存大小，其中数组只有一个元素
                Debug.MemoryInfo[] processMemoryInfo = activityManager.getProcessMemoryInfo(new int[]{pid});
                //获取进程占用内存的大小，Dirty弄脏
                int totalPrivateDirty = processMemoryInfo[0].getTotalPrivateDirty() * 1024;
                taskInfo.setMemorySize(totalPrivateDirty);
                /**
                 * 判断是否是用户app：
                 * 如果apk文件安装在/data目录的就是用户
                 * 在/system 目录下是系统的
                 */
                //配给包的完整路径的数据目录。Full path to the base APK for this application
                //这个应用完整的apk文件路径
               /* String sourceDir = packageInfo.applicationInfo.sourceDir;
                //返回 /data目录
                String path = Environment.getDataDirectory().getPath();
                if (sourceDir.startsWith(path)) {
                    taskInfo.setUserApp(true);
                } else {
                    taskInfo.setUserApp(false);
                }*/

                /**
                 * int 32bit:
                 * 0000 0000 0000 0000 0000 0000 0000 0001  1<<0  1
                 * 0000 0000 0000 0000 0000 0000 0000 0010  1<<1  2
                 * 0000 0000 0000 0000 0000 0000 0000 0100  1<<2  4
                 * 0000 0000 0000 0000 0000 0000 0000 1000  1<<3  8
                 */
                int flags = packageInfo.applicationInfo.flags;//每个应用都有一个falgs
                if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    //系统应用
                    taskInfo.setUserApp(false);
                } else {
                    taskInfo.setUserApp(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                taskInfo.setAppName(processName);
                taskInfo.setIcon(context.getResources().getDrawable(R.drawable.ic_launcher2));
            }
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    //杀死所有进程
    public static void killAll(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo rap : runningAppProcesses) {
            if (context.getPackageName().equals(rap.processName)) {
                continue;
            }
            activityManager.killBackgroundProcesses(rap.processName);
        }
    }

    //拿到手机正在运行数量
    public static int getRunningAppProcCount(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //拿到当前手机上所有正在运行中的进程
        List<ActivityManager.RunningAppProcessInfo> appTasks = activityManager.getRunningAppProcesses();
        return appTasks.size();
    }

    //获取手机可用内存
    public static long getAvailMem(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }
}
