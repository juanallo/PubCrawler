package com.jalloro.android.pubcrawler.data;

import android.content.ContentResolver;
import android.content.ContentUris;
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

//        public static final String COLUMN_PLANNED_MEN = "planned_men";
//        public static final String COLUMN_PLANNED_WOMEN = "planned_women";
        public static final String COLUMN_PLANNED_UNDEFINED = "planned_undefined";

//        public static final String COLUMN_ACTUAL_MEN = "actual_men";
//        public static final String COLUMN_ACTUAL_WOMEN = "actual_women";
        public static final String COLUMN_ACTUAL_UNDEFINED = "actual_undefined";

        public static final String[] COLUMNS = {
                TABLE_NAME + "." + _ID,
                COLUMN_NAME,
                COLUMN_PRICE,
                COLUMN_COORD_LAT,
                COLUMN_COORD_LONG,
                COLUMN_PLANNED_UNDEFINED,
                COLUMN_ACTUAL_UNDEFINED
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
}
