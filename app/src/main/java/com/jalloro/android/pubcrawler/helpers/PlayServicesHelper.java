package com.jalloro.android.pubcrawler.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

public class PlayServicesHelper {

    public static boolean isGooglePlayInstalled(Activity activity)
    {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if(status == ConnectionResult.SUCCESS){
            return true;
        }else{
            ((Dialog)GooglePlayServicesUtil.getErrorDialog(status, activity,10)).show();
        }
        return false;
    }

    public static void startLocationUpdates(@NonNull final GoogleApiClient googleApiClient,
                                            @NonNull final LocationRequest locationRequest,
                                            @NonNull final LocationListener locationListener)
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
    }

    public static void stopLocationUpdates(@NonNull final GoogleApiClient googleApiClient,
                                           @NonNull final LocationListener locationListener)
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, locationListener);
    }

    public static LocationRequest createLocationRequest(final int intervalTime, final int fastIntervalTime, final int priority) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(intervalTime);
        locationRequest.setFastestInterval(fastIntervalTime);
        locationRequest.setPriority(priority);
        return locationRequest;
    }

}
