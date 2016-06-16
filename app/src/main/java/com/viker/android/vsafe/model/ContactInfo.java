package com.viker.android.vsafe.model;

/**
 * Created by Viker on 2016/5/27.
 * 联系人模型 [在防盗设置中设置安全号码的时候需要]
 */
public class ContactInfo {

    private String name;
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
