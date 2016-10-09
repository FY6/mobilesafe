package com.wfy.domain;

/**
 * 黑名单信息
 * Created by wfy on 2016/5/31.
 */
public class BlackNumberInfo {
    //黑名单电话号码
    private String number;
    /**
     * 黑名单拦截模式
     * 1、全部拦截
     * 2、电话号码拦截
     * 3、短信拦截
     */
    private String mode;

    public BlackNumberInfo(String number, String mode) {
        this.number = number;
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
