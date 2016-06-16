package com.viker.android.vsafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Viker on 2016/5/29.
 * 创建黑名单数据库的帮助类
 */
public class BlackNumberDBOpenHelper extends SQLiteOpenHelper {

    //创建BlackNumber表的语句
    private static final String CREATE_BLACK_NUMBER = "create table BlackNumber (" +
            "id integer primary key autoincrement," +
            "number text," +
            "mode text )";

    public BlackNumberDBOpenHelper(Context context, String name,
                                   SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BLACK_NUMBER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
