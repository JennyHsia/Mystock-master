package com.example.rui.mystock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.rui.mystock.com.example.rui.mystock.data.StockInfo;
import com.example.rui.mystock.com.example.rui.mystock.data.TradeAccountInfo;

import java.util.ArrayList;

/**
 * Created by jennyxia on 2018/5/23.
 */

public class StockDBHelper extends SQLiteOpenHelper {
    public static final String TAG = "StockDBHelper";

    // 数据库文件名
    public static final String DB_NAME = "my_database.db";
    // 数据库表名
    public static final String TABLE_NAME = "t_stock";
    // 数据库版本号
    public static final int DB_VERSION = 1;

    public static final String ID = "id";
    public static final String ACCOUNT = "account";
    public static final String COST = "cost";
    public static final String QUANTITY = "quantity";
    public static final String[] TRADE_COLUMS = {ACCOUNT, ID, COST, QUANTITY};

    public static final int ID_INDEX = 0;
    public static final int ACCOUNT_INDEX = 1;
    public static final int COST_INDEX = 2;
    public static final int QUANTITY_INDEX = 3;

    public StockDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 当数据库文件创建时，执行初始化操作，并且只执行一次
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        db.execSQL("create table if not exists "
                + TABLE_NAME + " ("
                + ID + " text not null primary key,"
                + ACCOUNT + " text,"
                + COST + " double,"
                + QUANTITY + " integer default 0)");
    }
    public ArrayList<StockInfo> getAccountStocks(String account) {
        ArrayList<StockInfo> list = new ArrayList<StockInfo>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();
            cursor = db.query(TABLE_NAME, TRADE_COLUMS, ACCOUNT + "= '" + account + "'",
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    StockInfo stockInfo = new StockInfo();
                    stockInfo.id = cursor.getString(ID_INDEX);
                    stockInfo.cost = cursor.getString(COST_INDEX);
                    stockInfo.quantity = cursor.getString(QUANTITY_INDEX);
                    list.add(stockInfo);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("SearchHistoryDB",
                    "getHistorys with exception : " + e.getMessage());
        } finally {
            if (db != null) {

                if (cursor != null) {
                    cursor.close();
                }
                db.close();
                close();
            }
        }
        return list;
    }


    public synchronized void addStock(StockInfo stockInfo) {
        if (stockInfo == null) {
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (db != null) {
                db.beginTransaction();
                try {
                    if (db != null) {
                        ContentValues initialValues = new ContentValues();
                        initialValues
                                .put(ID, stockInfo.id);
                        db.replace(TABLE_NAME, null, initialValues);
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e(TAG,
                            "addStock with exception : "
                                    + e.getMessage());
                } finally {
                    db.endTransaction();
                }
            }
        } catch (Exception e) {
            Log.e(TAG,
                    "addStock getWritableDatabase with exception : "
                            + e.getMessage());
        } finally {
            close();
        }
    }

    public synchronized boolean delHistory(String id) {
        int result = 0;
        try {
            SQLiteDatabase db = getWritableDatabase();
            result = db.delete(TABLE_NAME, ID + "= '"
                    + id.replace("'", "''") + "'", null);
        } catch (Exception e) {
            Log.e("DB",
                    "removeStockByStockId with exception : "
                            + e.getMessage());
        } finally {
            close();
        }
        if (result > 0) {
            return true;
        }
        return false;
    }
    // 当数据库版本更新执行该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
