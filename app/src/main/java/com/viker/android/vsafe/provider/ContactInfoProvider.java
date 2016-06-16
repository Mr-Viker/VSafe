package com.viker.android.vsafe.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.viker.android.vsafe.model.ContactInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/5/27.
 * 用于获取电话本信息并提供联系人列表
 */
public class ContactInfoProvider {

    private Context context;

    public ContactInfoProvider(Context context) {
        this.context = context;
    }

    /*返回所有的联系人的信息*/
    public List<ContactInfo> getContactInfoList() {
        List<ContactInfo> contactInfoList = new ArrayList<>();
        //获取raw_contacts表所对应的Uri
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        //获取data表所对应的Uri
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        //获取raw_contacts表中联系人id的结果集
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{"contact_id"}, null, null, null);
        while (cursor.moveToNext()) {
            //只需查询一列数据，所以传入0即可
            String id = cursor.getString(0);
            ContactInfo info = new ContactInfo();
            //通过在raw_contacts表中查询得到的id来查询data表中的具体联系人信息
            //因为raw_contacts表和data表通过该id来进行关联
            Cursor dataCursor = context.getContentResolver().query(dataUri,
                    null, "raw_contact_id=?", new String[]{id}, null);
            while (dataCursor.moveToNext()) {
                //获取类型是联系人名字的数据
                if ("vnd.android.cursor.item/name".equals(dataCursor.getString(
                        dataCursor.getColumnIndex("mimetype")))) {
                    info.setName(dataCursor.getString(
                            dataCursor.getColumnIndex("data1")));
                } else if ("vnd.android.cursor.item/phone_v2".equals(
                        dataCursor.getString(dataCursor.getColumnIndex("mimetype")))) {
                    //获取类型是联系人手机号码的数据
                    info.setPhone(dataCursor.getString(
                            dataCursor.getColumnIndex("data1")));
                }
            }
            //每查询一个联系人就将其添加到集合中
            contactInfoList.add(info);
            info = null;
            dataCursor.close(); //关闭结果集
        }
        cursor.close();
        return contactInfoList;
    }

}














