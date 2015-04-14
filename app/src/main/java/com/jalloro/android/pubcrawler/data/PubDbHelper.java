package com.jalloro.android.pubcrawler.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;

import com.jalloro.android.pubcrawler.model.Crawler;

public class PubDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    final String androidId;

    static final String DATABASE_NAME = "pubCrawler.db";

    public PubDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        androidId = android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_CRAWLER_TABLE = "CREATE TABLE " + PubContract.CrawlerLocation.TABLE_NAME + " (" +
                PubContract.CrawlerLocation._ID + " TEXT PRIMARY KEY," +
                PubContract.CrawlerLocation.COLUMN_TIMESTAMP + " LONG UNIQUE, " +
                PubContract.CrawlerLocation.COLUMN_LAST_ADDRESS + " TEXT, " +
                PubContract.CrawlerLocation.COLUMN_COORD_LAT + " REAL, " +
                PubContract.CrawlerLocation.COLUMN_COORD_LONG + " REAL, " +
                PubContract.CrawlerLocation.GENDER + " TEXT " +
                " );";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + PubContract.WhatIsHot.TABLE_NAME + " (" +
                PubContract.WhatIsHot._ID + " TEXT PRIMARY KEY," +
                PubContract.WhatIsHot.COLUMN_NAME + " TEXT, " +
                PubContract.WhatIsHot.COLUMN_PRICE + " TEXT, " +
                PubContract.WhatIsHot.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                PubContract.WhatIsHot.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                PubContract.WhatIsHot.COLUMN_PLANNED_UNDEFINED + " INTEGER, " +
                PubContract.WhatIsHot.COLUMN_ACTUAL_UNDEFINED + " INTEGER" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_CRAWLER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

        createDefaultUser(sqLiteDatabase);
    }

    private void createDefaultUser(SQLiteDatabase database) {

        ContentValues values = new ContentValues();
        values.put(PubContract.CrawlerLocation._ID, androidId);
        values.put(PubContract.CrawlerLocation.GENDER, Crawler.Gender.UNDEFINED.name());
        database.insert(PubContract.CrawlerLocation.TABLE_NAME, null, values);
//        database.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PubContract.CrawlerLocation.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PubContract.WhatIsHot.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}