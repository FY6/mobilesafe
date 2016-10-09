package com.wfy.mobilesafe.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.wfy.domain.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取所有应用程序的信息:
 * <p/>
 * 系统：
 * packageName :com.android.systemui
 * dataDir ://data/datacom.android.systemui----安装的位置在哪
 * sourceDir : /system/app/SystemUI.apk-----   可以知道是系统应用，，还可以获取大小
 * loadLabel : 系统用户界面
 * icon 2130837522
 * 第三方：
 * packageName :com.wfy.contact
 * dataDir :/data/data/com.wfy.contact---安装的位置在哪
 * sourceDir : /data/app/com.wfy.contact-1.apk----可以知道是第三方应用，，还可以获取大小
 * loadLabel : 是你的通讯录
 * icon 2130837506
 */
public class AppInfosUtils {
    public static List<AppInfo> getAppInfos(Context mContext) {
        List<AppInfo> infos = new ArrayList<>();
        PackageManager packageManager = mContext.getPackageManager();
        //List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        //通过包也能拿到app信息，Android中应用的信息都是用包来统一管理
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : installedPackages) {
            AppInfo mAppInfo = new AppInfo();
            Drawable drawable = packageInfo.applicationInfo.loadIcon(packageManager);//获取应用图标
            mAppInfo.setApkIcon(drawable);
            String apkName = packageInfo.applicationInfo.loadLabel(packageManager).toString();//获取应用的名字
            mAppInfo.setApkName(apkName);
            String packageName = packageInfo.applicationInfo.packageName;//获取应用的包名
            mAppInfo.setApkPackageName(packageName);
            String sourceDir = packageInfo.applicationInfo.sourceDir;//获取应用资源目录 sourceDir : /data/app/com.wfy.contact-1.apk
            File apkFile = new File(sourceDir);
            long pakSize = apkFile.length();
            mAppInfo.setApkSize(pakSize);

            String dataDir = packageInfo.applicationInfo.dataDir;
            String path = Environment.getDataDirectory().getPath();// /data
            if (sourceDir.startsWith(path)) {
                //表示系统应用
                mAppInfo.setUserApp(true);
            } else {
                mAppInfo.setUserApp(false);
            }
            if (dataDir.startsWith(path)) {
                //在内部存储
                mAppInfo.setRom(true);
            } else {
                //在SD卡存储
                mAppInfo.setRom(false);
            }
            infos.add(mAppInfo);
        }
        return infos;
    }
}
