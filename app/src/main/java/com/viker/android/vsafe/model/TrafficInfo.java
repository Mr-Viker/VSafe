package com.viker.android.vsafe.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Viker on 2016/6/5.
 * [流量信息]模型，属于[流量统计]模块
 */
public class TrafficInfo {

    private Drawable appIcon;
    private String appName;
    private long rx;
    private long tx;

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

}
