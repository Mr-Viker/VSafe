package com.viker.android.vsafe.model;

/**
 * Created by Viker on 2016/5/29.
 * 通信卫士模块中的黑名单模型
 */
public class BlackNumber {

    private String number; //黑名单手机号码
    private String mode; //拦截模式：电话拦截，短信拦截，全部拦截

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
