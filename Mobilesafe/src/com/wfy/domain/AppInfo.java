package com.wfy.domain;

import android.graphics.drawable.Drawable;

/**
 * 封装应用程序信息
 * Created by wfy on 2016/6/8.
 */
public class AppInfo {
    //应用程序图标
    private Drawable apkIcon;
    //应用程序名称
    private String apkName;
    //应用程序包占用空间大小
    private long apkSize;
    //是否是第三方apk。true表示是，false表示不是
    private boolean isUserApp;
    //安装位置，安装在内部存储还是在sd卡中
    private boolean isRom;
    //apk包名
    private String apkPackageName;

    public AppInfo() {
    }

    public AppInfo(Drawable apkIcon, String apkName,
                   long apkSize, boolean isUserApp,
                   boolean isRom, String apkPackageName) {
        this.apkIcon = apkIcon;
        this.apkName = apkName;
        this.apkSize = apkSize;
        this.isUserApp = isUserApp;
        this.isRom = isRom;
        this.apkPackageName = apkPackageName;
    }

    public Drawable getApkIcon() {
        return apkIcon;
    }

    public void setApkIcon(Drawable apkIcon) {
        this.apkIcon = apkIcon;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public long getApkSize() {
        return apkSize;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }

    public boolean isUserApp() {
        return isUserApp;
    }

    public void setUserApp(boolean userApp) {
        isUserApp = userApp;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }

    public void setApkPackageName(String apkPackageName) {
        this.apkPackageName = apkPackageName;
    }

    public boolean isRom() {
        return isRom;
    }

    public void setRom(boolean rom) {
        isRom = rom;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "isRom=" + isRom +
                ", apkPackageName='" + apkPackageName + '\'' +
                ", isUserApp=" + isUserApp +
                ", apkSize=" + apkSize +
                ", apkName='" + apkName + '\'' +
                '}';
    }
}
