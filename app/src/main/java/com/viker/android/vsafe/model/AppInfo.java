package com.viker.android.vsafe.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Viker on 2016/6/3.
 * 关于应用程序信息的模型类，属于程序管理模块
 */
public class AppInfo {

    private String packageName; //包名
    private String appVersion; //版本号
    private String appName; //程序名
    private Drawable appIcon; //程序图标
    private boolean userApp; //是否属于用户程序

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }
}
