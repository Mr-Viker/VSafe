package com.viker.android.vsafe.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.viker.android.vsafe.model.BlackNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/5/29.
 * 数据库访问基类，封装了访问黑名单数据库的基本CRUD方法
 */
public class BlackNumberDao {

    private static final String DB_NAME = "black_number";
    private static final int VERSION = 1;

    private SQLiteDatabase db;

    private static BlackNumberDao blackNumberDao;

    /*使用单例模式，减少数据库查询对象的创建与资源的消耗*/
    private BlackNumberDao(Context context) {
        BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(context,
                DB_NAME, null, VERSION);
        db = helper.getWritableDatabase();
    }

    public synchronized static BlackNumberDao getInstance(Context context) {
        if (blackNumberDao == null) {
            blackNumberDao = new BlackNumberDao(context);
        }
        return blackNumberDao;
    }

    /*添加一条黑名单记录*/
    public void insert(BlackNumber blackNumber) {
        if (blackNumber != null) {
            ContentValues values = new ContentValues();
            values.put("number", blackNumber.getNumber());
            values.put("mode", blackNumber.getMode());
            db.insert("BlackNumber", null, values);
        }
    }

    /*删除一条黑名单记录*/
    public void delete(BlackNumber blackNumber) {
        db.delete("BlackNumber", "number=?", new String[]{blackNumber.getNumber()});
    }

    /*更改一条黑名单记录*/
    public void update(BlackNumber newBlackNumber, BlackNumber oldBlackNumber) {
        if (newBlackNumber != null) {
            ContentValues values = new ContentValues();
            values.put("number", newBlackNumber.getNumber());
            values.put("mode", newBlackNumber.getMode());
            db.update("BlackNumber", values, "number=?",
                    new String[]{oldBlackNumber.getNumber()});
        }
    }

    /*查找所有黑名单记录*/
    public List<BlackNumber> queryAll() {
        List<BlackNumber> blackNumberList = new ArrayList<>();
        Cursor cursor = db.query("BlackNumber", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                BlackNumber blackNumber = new BlackNumber();
                blackNumber.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                blackNumber.setMode(cursor.getString(cursor.getColumnIndex("mode")));
                blackNumberList.add(blackNumber);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return blackNumberList;
    }

    /*查找一条黑名单记录，用于在修改黑名单记录时查询，避免修改为已经存在的黑名单记录*/
    public BlackNumber query(BlackNumber blackNumber) {
        Cursor cursor = db.query("BlackNumber", null, "number=?",
                new String[]{blackNumber.getNumber()}, null, null, null);
        BlackNumber savedBlackNumber = new BlackNumber();
        if (cursor.moveToFirst()) {
            savedBlackNumber.setNumber(cursor.getString(cursor.getColumnIndex("number")));
            savedBlackNumber.setMode(cursor.getString(cursor.getColumnIndex("mode")));
        }
        cursor.close();
        return savedBlackNumber;
    }

    /*查找一条黑名单记录的号码，用于在修改黑名单记录时查询，避免修改为已经存在的黑名单记录（拦截模式
    不同）*/
    public String queryNumber(String number) {
        Cursor cursor = db.query("BlackNumber", null, "number=?",
                new String[]{number}, null, null, null);
        String savedNumber = "";
        if (cursor.moveToFirst()) {
            savedNumber = cursor.getString(cursor.getColumnIndex("number"));
        }
        cursor.close();
        return savedNumber;
    }

    /*查找一条黑名单记录的拦截模式，传入号码，返回拦截模式。如果返回空值则代表没有该黑名单号码*/
    public String queryMode(String number) {
        Cursor cursor = db.query("BlackNumber", null, "number=?",
                new String[]{number}, null, null, null);
        String mode = "";
        if (cursor.moveToFirst()) {
            mode = cursor.getString(cursor.getColumnIndex("mode"));
        }
        cursor.close();
        return mode;
    }

}













