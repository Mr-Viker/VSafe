package com.viker.android.vsafe.model;

/**
 * Created by Viker on 2016/5/25.
 * 封装解析好的实体数据信息
 */
public class UpdateInfo {

    private String version; //服务端的版本号
    private String description; //服务端的升级提示
    private String apkUrl; //服务端的apk下载地址

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }


}
