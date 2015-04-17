package com.jalloro.android.pubcrawler.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.DatabaseUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.data.PubContract;
import com.jalloro.android.pubcrawler.detail.FetchPlaceIntentService;
import com.jalloro.android.pubcrawler.model.Place;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    ContentResolver contentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
        Log.i(LOG_CAT, "Sync Called");
        Firebase.setAndroidContext(getContext());
        final Firebase firebase = new Firebase("https://boiling-fire-4188.firebaseio.com/crawlers");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Place> places = new HashMap<>();
                for (DataSnapshot pl : dataSnapshot.getChildren()){
                    Map<String, Object> newPlace = (Map<String, Object>) pl.getValue();
                    final String address = newPlace.get("lastAddress").toString();
                    //TODO check timeStamp
                    if(places.containsKey(address)){
                        final Place place = places.get(address);
                        place.addCrawler();
                    }
                    else {
                        final Map<String, Object> lastLocation = (Map<String, Object>) newPlace.get("lastLocation");
                        final double latitude = Double.parseDouble(lastLocation.get("latitude").toString());
                        final double longitude = Double.parseDouble(lastLocation.get("longitude").toString());
                        Place place = new Place(new SimplifiedLocation(latitude, longitude), address);
                        place.addCrawler();
                        places.put(address, place);
                    }
                 }
                for (Place place : places.values()){
                    // Defines an object to contain the new values to insert
                    ContentValues mNewValues = new ContentValues();

                    mNewValues.put(PubContract.WhatIsHot._ID, place.getAddress());
                    mNewValues.put(PubContract.WhatIsHot.COLUMN_ACTUAL_UNDEFINED, place.getRealAmountOfUndefined());

                    final String condition = PubContract.WhatIsHot._ID + " = " + DatabaseUtils.sqlEscapeString(place.getAddress());

                    if(PubContract.existsValue(contentResolver, PubContract.WhatIsHot.CONTENT_URI, condition)){

                        mNewValues.put(PubContract.WhatIsHot.COLUMN_COORD_LAT, place.getLocation().getLatitude());
                        mNewValues.put(PubContract.WhatIsHot.COLUMN_COORD_LONG, place.getLocation().getLongitude());

                        contentResolver.insert(
                                PubContract.WhatIsHot.CONTENT_URI,
                                mNewValues
                        );
                        Intent intent = new Intent(getContext(), FetchPlaceIntentService.class);
                        intent.putExtra(FetchPlaceIntentService.Constants.RECEIVER, new ResultReceiver(new Handler()));
                        intent.putExtra(FetchPlaceIntentService.Constants.ADDRESS_DATA_EXTRA, place.getAddress());
                        intent.putExtra(FetchPlaceIntentService.Constants.LOCATION_DATA_EXTRA, place.getLocation());
                        getContext().startService(intent);
                    }
                    else {
                        contentResolver.update(
                               PubContract.WhatIsHot.CONTENT_URI,
                               mNewValues,
                               condition,
                               null
                       );
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_CAT, firebaseError.getMessage());
            }
        });
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static final int SYNC_INTERVAL = 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    public static final String LOG_CAT = SyncAdapter.class.getName();
}