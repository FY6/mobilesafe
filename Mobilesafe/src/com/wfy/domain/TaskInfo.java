package com.wfy.domain;

import android.graphics.drawable.Drawable;

/**
 * 进程信息实体类
 * Created by wfy on 2016/7/3.
 */
public class TaskInfo {
    private Drawable icon;
    private String packageName;
    private long memorySize;
    private boolean isChecked;
    //进程名
    private String appName;
    //是否是用户进程
    private boolean isUserApp;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isUserApp() {
        return isUserApp;
    }

    public void setUserApp(boolean userApp) {
        isUserApp = userApp;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
