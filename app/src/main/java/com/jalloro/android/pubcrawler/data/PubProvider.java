package com.jalloro.android.pubcrawler.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PubProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PubDbHelper pubDbHelper;

    static final int CURRENT = 100;
    static final int PUBS = 101;
    static final int CURRENT_PUB = 102;

    //first Crawler
    private static final String currentCrawlerLocation =
            PubContract.CrawlerLocation.TABLE_NAME;

    //location.location_setting = ?
    private static final String currentPubSelection =
            PubContract.WhatIsHot.TABLE_NAME+
                    "." + PubContract.WhatIsHot._ID + " = ? ";


    @Override
    public boolean onCreate() {
        pubDbHelper = new PubDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "CURRENT CRAWLER/*/*"
            case CURRENT:
            {
                retCursor = pubDbHelper.getReadableDatabase().query(
                        PubContract.CrawlerLocation.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "PUBS/*"
            case PUBS: {
                retCursor = pubDbHelper.getReadableDatabase().query(
                        PubContract.WhatIsHot.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CURRENT_PUB:{
                String pubAdddres = PubContract.WhatIsHot.getAdddressSettingFromUri(uri);
                selection = currentPubSelection;
                selectionArgs = new String[]{pubAdddres};
                retCursor = pubDbHelper.getReadableDatabase().query(
                        PubContract.WhatIsHot.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case CURRENT:
                return PubContract.CrawlerLocation.CONTENT_ITEM_TYPE;
            case PUBS:
                return PubContract.WhatIsHot.CONTENT_TYPE;
            case CURRENT_PUB:
                return PubContract.WhatIsHot.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = pubDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CURRENT: {
                long _id = db.replace(PubContract.CrawlerLocation.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PubContract.CrawlerLocation.buildCrawlerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PUBS: {
                long _id = db.replace(PubContract.WhatIsHot.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PubContract.WhatIsHot.buildPubsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = pubDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case CURRENT:
                rowsDeleted = db.delete(
                        PubContract.CrawlerLocation.TABLE_NAME, selection, selectionArgs);
                break;
            case PUBS:
                rowsDeleted = db.delete(
                        PubContract.WhatIsHot.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = pubDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CURRENT:
                rowsUpdated = db.update(PubContract.CrawlerLocation.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case PUBS:
                rowsUpdated = db.update(PubContract.WhatIsHot.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PubContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PubContract.PATH_WHAT_IS_HOT, PUBS);
        matcher.addURI(authority, PubContract.PATH_WHAT_IS_HOT + "/*", CURRENT_PUB);

        matcher.addURI(authority, PubContract.PATH_LOCATION, CURRENT);
        return matcher;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        pubDbHelper.close();
        super.shutdown();
    }
}
