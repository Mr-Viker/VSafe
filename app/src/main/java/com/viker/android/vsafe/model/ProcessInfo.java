package com.viker.android.vsafe.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Viker on 2016/6/4.
 * [进程信息]模型类，属于[进程管理]模块
 */
public class ProcessInfo {

    private String packName; //应用程序包名
    private Drawable icon; //图标
    private String appName; //应用程序名
    private long memorySize; //所占用的RAM空间，单位byte
    private boolean userProcess; //是否是用户进程
    private int pid; //进程的标记 process id

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private boolean checked; //应用程序在Item中是否处于被选中状态[默认没有]

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    public boolean isUserProcess() {
        return userProcess;
    }

    public void setUserProcess(boolean userProcess) {
        this.userProcess = userProcess;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
