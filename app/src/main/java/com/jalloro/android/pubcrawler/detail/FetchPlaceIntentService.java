package com.jalloro.android.pubcrawler.detail;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.jalloro.android.pubcrawler.helpers.PlayServicesHelper;
import com.jalloro.android.pubcrawler.model.AddressInfo;
import com.jalloro.android.pubcrawler.model.PriceRange;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.welcome.FetchAddressIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchPlaceIntentService extends IntentService {

    private static final String LOG_TAG = FetchPlaceIntentService.class.getName();
    private static final String FOURSQUARE_BASE_URL = "https://api.foursquare.com/v2/venues/search";
    private static final String LOCATION_PARAM = "ll";
    private static final String CLIENT_PARAM = "client_id";
    private static final String SECRET_PARAM = "client_secret";
    private static final String CLIENT_ID = "QMUKHTYBO5WWETB1JPZUWXTEOG4JBF2ESFASQO11QFGURCE1";
    private static final String CLIENT_SECRET = "XISM01N0H4CBHG3JSOPDKZSTEPPYAR41OW3UVDXQ4DO4LAFX";
    private static final String VERSION_PARAM = "v";
    private static final String VERSION_USED = "20140806";
    private ResultReceiver resultReceiver;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public FetchPlaceIntentService() {
        super(FetchAddressIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //get address info from intent
        String address = intent.getStringExtra(Constants.ADDRESS_DATA_EXTRA);
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        SimplifiedLocation location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);;

        try {
            //connect to foursquare and get info
            String placesJson = getPlaceInfoFromFoursquare(location);
            //send info to receiver.
            List<AddressInfo> addresses = getAddressesFromJson(placesJson);
            AddressInfo addressInfo = findNearestPlace(addresses, location, address);
            deliverResultToReceiver(Constants.SUCCESS_RESULT, addressInfo);
        } catch (JSONException | FailToRetrievePlaceException e) {
            AddressInfo addressInfo = new AddressInfo(address,"Are you on the moon?", PriceRange.UNKNOWN, location);
           deliverResultToReceiver(Constants.FAILURE_RESULT,addressInfo);
        }
    }

    private AddressInfo findNearestPlace(List<AddressInfo> addresses, SimplifiedLocation location, String address)
            throws FailToRetrievePlaceException {
        String simpleAddress = address.split("\n")[0];
        AddressInfo nearest = null;
        double nearestDistance = 0;
        AddressInfo addressMatch = null;
        for (AddressInfo addressInfo : addresses){
            final double distance = PlayServicesHelper.distance(location, addressInfo.getLocation());
            if(nearest == null || distance < nearestDistance){
                if(nearest == null || !nearest.isVerified() || nearest.isVerified() && addressInfo.isVerified()){
                    nearest = addressInfo;
                    nearestDistance = distance;
                    if(addressInfo.getAddress().contains(simpleAddress)){
                        addressMatch = addressInfo;
                    }
                }

            }
        }
        if(nearest == null){
            throw new FailToRetrievePlaceException();
        }
        if(addressMatch != null){
            nearest = addressMatch;
        }
        return nearest;
    }

    private List<AddressInfo> getAddressesFromJson(String placesJson) throws JSONException {
        List<AddressInfo> addresses = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(placesJson);
        JSONObject reponse = jsonObject.optJSONObject("response");
        final JSONArray venues = reponse.getJSONArray("venues");
        for (int i = 0; i < venues.length(); i++)
        {
            final JSONObject placeJson = venues.getJSONObject(i);
            boolean verified = placeJson.getBoolean("verified");
            String name = placeJson.getString("name");
            PriceRange priceRange;
            if(placeJson.isNull("price")){
              priceRange =  PriceRange.UNKNOWN;
            }
            else {
                String price = placeJson.getString("price");
               priceRange = PriceRange.fromValue(Integer.parseInt(price));
            }

            JSONObject location = placeJson.getJSONObject("location");
            String address = location.getString("formattedAddress");
            String latitude = location.getString("lat");
            String longitude = location.getString("lng");

            AddressInfo place = new AddressInfo(address, name,priceRange, new SimplifiedLocation(Double.parseDouble(latitude),Double.parseDouble(longitude)));
            place.setVerified(verified);
            addresses.add(place);
        }

        return addresses;
    }

    private String getPlaceInfoFromFoursquare(SimplifiedLocation location) throws FailToRetrievePlaceException {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String placeInfo;
        try {

            Uri builtUri = Uri.parse(FOURSQUARE_BASE_URL).buildUpon()
                    .appendQueryParameter(CLIENT_PARAM, CLIENT_ID)
                    .appendQueryParameter(SECRET_PARAM,CLIENT_SECRET)
                    .appendQueryParameter(VERSION_PARAM, VERSION_USED)
                    .build();

            URL url = new URL(builtUri.toString() + "&" + LOCATION_PARAM + "=" + location.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
               throw new FailToRetrievePlaceException();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                throw new FailToRetrievePlaceException();
            }
            placeInfo =  buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            throw new FailToRetrievePlaceException();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return placeInfo;
    }



    private void deliverResultToReceiver(int resultCode, AddressInfo addressInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY, addressInfo);
        resultReceiver.send(resultCode, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationplace";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String ADDRESS_DATA_EXTRA = PACKAGE_NAME +
                ".ADDRESS_DATA_EXTRA";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }
}
