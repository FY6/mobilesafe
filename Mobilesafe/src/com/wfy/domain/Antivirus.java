package com.wfy.domain;

/**
 * 病毒特征类
 * Created by wfy on 2016/8/3.
 */
public class Antivirus {
    private String MD5;
    private String desc;
    private String type;
    private String name;

    public Antivirus() {
    }

    public Antivirus(String md5, String desc, String type, String name) {
        this.MD5 = md5;
        this.desc = desc;
        this.type = type;
        this.name = name;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
