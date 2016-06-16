package com.viker.android.vsafe.model;

import android.content.pm.PackageInfo;

/**
 * Created by Viker on 2016/6/6.
 * 该模型属于[手机杀毒]模块，模型中主要包含了变量包信息，以及该包是安全的还是含有病毒的。
 */
public class PackageVirusInfo {

    private PackageInfo packageInfo;
    private boolean Virus;

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public boolean isVirus() {
        return Virus;
    }

    public void setVirus(boolean virus) {
        Virus = virus;
    }
}
