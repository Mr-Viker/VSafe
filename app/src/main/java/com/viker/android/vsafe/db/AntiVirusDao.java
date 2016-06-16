package com.viker.android.vsafe.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Viker on 2016/6/6.
 * 病毒特征码数据库的查询类
 */
public class AntiVirusDao {

    private Context context;
    private static AntiVirusDao dao;
    private SQLiteDatabase db;
    private String path; //数据库的路径


    /*使用单例模式，减少数据库访问对象的创建与资源的消耗*/
    private AntiVirusDao(Context context) {
        this.context = context;
        //设置病毒数据库的路径
        path = "/data/data/com.viker.android.vsafe/files/antivirus.db";
        //打开数据库
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    public synchronized static AntiVirusDao getInstance(Context context) {
        if (dao == null) {
            dao = new AntiVirusDao(context);
        }
        return dao;
    }

    /*查询数据库中是否含有该特征码，并将查询结果返回给调用者，null为数据库文件中不存在该特征码*/
    public String getVirusInfo(String md5) {
        String result = null;
        Cursor cursor = db.rawQuery("select desc from datable where md5=?",
                new String[]{md5});
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

}
