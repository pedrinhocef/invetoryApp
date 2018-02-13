package com.gplabs.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.gplabs.inventoryapp.data.StockContract.StockEntry.CREATE_TABLE_STOCK;

/**
 * Created by pedronice on 21/01/17.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "inventory.db";
    public static final int DB_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context,DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STOCK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_STOCK);
        onCreate(db);
    }

}

