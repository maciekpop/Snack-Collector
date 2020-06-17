package com.example.snackcollector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.snackcollector.ProductContract.*;

class DataBaseHelper extends SQLiteOpenHelper {

    public static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "snack_list.db";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createTable = "CREATE TABLE " +
                ProductEntry.TABLE_NAME + " (" +
                ProductEntry.PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.PRODUCT_NAME + " TEXT, " +
                ProductEntry.PRODUCT_PRICE + " TEXT, " +
                ProductEntry.PRODUCT_ACCESSIBILITY + " TEXT, " +
                ProductEntry.PRODUCT_RATING + " REAL, " +
                ProductEntry.PRODUCT_FILE_PATH + " TEXT);";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + ProductEntry.TABLE_NAME);
        onCreate(db);
    }

}
