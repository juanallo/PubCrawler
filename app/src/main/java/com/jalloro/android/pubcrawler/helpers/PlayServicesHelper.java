package com.jalloro.android.pubcrawler.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

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

    public static JSONObject getLocationInfo(double lat, double lng) {

        HttpGet httpGet = new HttpGet("http://maps.googleapis.com/maps/api/geocode/json?latlng="+ lat+","+lng +"&sensor=true");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static String getCurrentLocationViaJSON(double lat, double lng) {

        JSONObject jsonObj = getLocationInfo(lat, lng);
        Log.i("JSON string =>", jsonObj.toString());

        String currentLocation = "testing";
        String street_address = null;
        String postal_code = null;

        try {
            String status = jsonObj.getString("status").toString();
            Log.i("status", status);

            if(status.equalsIgnoreCase("OK")){
                JSONArray results = jsonObj.getJSONArray("results");
                int i = 0;
                Log.i("i", i+ "," + results.length() ); //TODO delete this
                do{

                    JSONObject r = results.getJSONObject(i);
                    JSONArray typesArray = r.getJSONArray("types");
                    String types = typesArray.getString(0);

                    if(types.equalsIgnoreCase("street_address")){
                        street_address = r.getString("formatted_address").split(",")[0];
                        Log.i("street_address", street_address);
                    }else if(types.equalsIgnoreCase("postal_code")){
                        postal_code = r.getString("formatted_address");
                        Log.i("postal_code", postal_code);
                    }

                    if(street_address!=null && postal_code!=null){
                        currentLocation = street_address + "," + postal_code;
                        Log.i("Current Location =>", currentLocation); //Delete this
                        i = results.length();
                    }

                    i++;
                }while(i<results.length());

                Log.i("JSON Geo Locatoin =>", currentLocation);
                return currentLocation;
            }

        } catch (JSONException e) {
            Log.e("testing","Failed to load JSON");
            e.printStackTrace();
        }
        return null;
    }

}
