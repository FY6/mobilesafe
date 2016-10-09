package com.wfy.domain;

/**
 * 病毒扫描信息类
 * Created by wfy on 2016/8/2.
 */
public class AntivirusScannerInfo {
    private boolean isAntivirus;//是否是病毒
    private String appName;//应用名称
    private String packageName;//应用包名
    public static int scannerNum;//扫描数
    private int antivirusNum;//病毒数
    private String desc;//病毒描述

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getAntivirusNum() {
        return antivirusNum;
    }

    public void setAntivirusNum(int antivirusNum) {
        this.antivirusNum = antivirusNum;
    }

    public boolean isAntivirus() {
        return isAntivirus;
    }

    public void setAntivirus(boolean antivirus) {
        isAntivirus = antivirus;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
