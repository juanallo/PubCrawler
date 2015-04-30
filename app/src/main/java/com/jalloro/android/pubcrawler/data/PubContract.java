package com.jalloro.android.pubcrawler.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class PubContract {

    public static final String CONTENT_AUTHORITY = "com.jalloro.android.pubcrawler.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LOCATION = "location";
    public static final String PATH_WHAT_IS_HOT = "hot";



    public static final class CrawlerLocation implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = PATH_LOCATION;

        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
        public static final String COLUMN_LAST_ADDRESS = "last_address";
        public static final String GENDER = "gender";

        public static Uri buildCrawlerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCrawlerUri() {
            return CONTENT_URI;
        }

        public static final String[] COLUMNS = {
                PubContract.CrawlerLocation.TABLE_NAME + "." + PubContract.CrawlerLocation._ID,
                PubContract.CrawlerLocation.COLUMN_TIMESTAMP,
                PubContract.CrawlerLocation.COLUMN_COORD_LAT,
                PubContract.CrawlerLocation.COLUMN_COORD_LONG,
                PubContract.CrawlerLocation.COLUMN_LAST_ADDRESS,
                PubContract.CrawlerLocation.GENDER
        };
    }

    public static final class WhatIsHot implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WHAT_IS_HOT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WHAT_IS_HOT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WHAT_IS_HOT;

        // Table name
        public static final String TABLE_NAME = PATH_WHAT_IS_HOT;

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static final String COLUMN_HISTORIC = "historic";

        public static final String COLUMN_NOW = "now";

        public static final String HOT_ID = TABLE_NAME + "." + _ID;

        public static final String[] COLUMNS = {
                HOT_ID,
                COLUMN_NAME,
                COLUMN_PRICE,
                COLUMN_COORD_LAT,
                COLUMN_COORD_LONG,
                COLUMN_HISTORIC,
                COLUMN_NOW
        };

        public static Uri buildPubsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCurrentPubUri(String address) {
            return CONTENT_URI.buildUpon().appendPath(address).build();
        }

//        public static Uri buildCurrentPubUri() {
//            return CONTENT_URI;
//        }

        public static String getAdddressSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static boolean existsValue(ContentResolver contentProvider, Uri contentUri, String condition){
        Cursor c = contentProvider.query(contentUri,null,
                condition, null, null);
        final int rowsFound = c.getCount();
        c.close();
        return rowsFound > 0;
    }
}
