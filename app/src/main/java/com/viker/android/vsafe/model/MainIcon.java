package com.viker.android.vsafe.model;

/**
 * Created by Viker on 2016/5/26.
 * 主页面中的九宫格图标和其对应的名称 模型
 */
public class MainIcon  {

    private int icon;
    private String name;

    public MainIcon(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

}
