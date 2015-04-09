package com.jalloro.android.pubcrawler.welcome;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.data.PubContract;
import com.jalloro.android.pubcrawler.detail.PubDetailActivity;
import com.jalloro.android.pubcrawler.detail.PubDetailFragment;
import com.jalloro.android.pubcrawler.helpers.GoogleConnectionApiClientListener;
import com.jalloro.android.pubcrawler.helpers.PlayServicesHelper;
import com.jalloro.android.pubcrawler.model.Crawler;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.hot.WhatIsHotActivity;

public class CheckInFragment extends Fragment
        implements GoogleConnectionApiClientListener, LocationListener, LoaderManager.LoaderCallbacks<Cursor>
{
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private ResultReceiver addressReceiver;
    private String currentAddress;
    private Firebase userInfo;
    private Crawler currentCrawler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_checkin, container, false);

        if(savedInstanceState!= null){
            currentCrawler = savedInstanceState.getParcelable(CRAWLER);
            currentAddress = savedInstanceState.getString(CURRENT_ADDRESS);
            lastLocation = savedInstanceState.getParcelable(LAST_LOCATION);
        }
        else {
            final TextView checkInText = (TextView)rootView.findViewById(R.id.checkInText);
            final ImageButton checkInButton = (ImageButton)rootView.findViewById(R.id.checkinButton);
            final Button whatIsHot = (Button) rootView.findViewById(R.id.what_is_hot);
            final TextView viewDetails = (TextView)rootView.findViewById(R.id.open_details);
            viewDetails.setVisibility(View.GONE);
            checkInText.setVisibility(View.GONE);
            checkInButton.setVisibility(View.GONE);
            whatIsHot.setVisibility(View.GONE);
        }

        if(PlayServicesHelper.isGooglePlayInstalled(getActivity())){
            //setting Firebase
            Firebase.setAndroidContext(getActivity());

            //creating Google Api for location
            googleApiClient = PlayServicesHelper.createGoogleApiClient(this.getActivity(), this);

            //creating a location request
            locationRequest = PlayServicesHelper.createLocationRequest(10000, 50000, LocationRequest.PRIORITY_HIGH_ACCURACY);

            //connecting to google api
            googleApiClient.connect();

        }
        else {
            //isGooglePlayInstalled will show a dialog to the user.
            disabledUi(rootView);
        }

        return rootView;
    }

    private void disabledUi(View rootView) {
        final ImageButton checkInButton = (ImageButton)rootView.findViewById(R.id.checkinButton);
        Button whatIsHot = (Button) rootView.findViewById(R.id.what_is_hot);
        final Button viewDetails = (Button)rootView.findViewById(R.id.open_details);
        whatIsHot.setVisibility(View.GONE);
        checkInButton.setEnabled(false);
        viewDetails.setVisibility(View.GONE);
    }

    private void openDetails() {
        Intent intent = new Intent(getActivity(), PubDetailActivity.class);
        intent.putExtra(PUB_ADDRESS, currentCrawler.getLastAddress());
        //current and pub location are the same
        intent.putExtra(PubDetailFragment.PUB_LOCATION, currentCrawler.getLastLocation());
        intent.putExtra(PubDetailFragment.CURRENT_LOCATION, currentCrawler.getLastLocation());
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CRAWLER,currentCrawler);
        outState.putString(CURRENT_ADDRESS, currentAddress);
        outState.putParcelable(LAST_LOCATION, lastLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnected(Bundle bundle) {
        lastLocation =  LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        fetchAddress();

        final View rootView = getView();
        if(lastLocation != null && rootView != null){

            final ImageButton checkInButton = (ImageButton) rootView.findViewById(R.id.checkinButton);
            checkInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!currentCrawler.isCheckedIn(lastLocation)){
                        checkIn(rootView);
                    }
                }
            });

            final Button viewDetails = (Button) rootView.findViewById(R.id.open_details);
            viewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetails();
                }
            });

            Button whatIsHot = (Button) rootView.findViewById(R.id.what_is_hot);
            whatIsHot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WhatIsHotActivity.class);
                    //current and pub location are the same
                    intent.putExtra(PubDetailFragment.CURRENT_LOCATION, new SimplifiedLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                    intent.putExtra(PUB_ADDRESS,currentCrawler.getLastAddress());
                    intent.putExtra(PubDetailFragment.PUB_LOCATION,currentCrawler.getLastLocation());
                    startActivity(intent);
                }
            });
            PlayServicesHelper.startLocationUpdates(googleApiClient, locationRequest, this);

            if(currentCrawler != null){
                updateChecked(rootView,currentCrawler.isCheckedIn(lastLocation));
            }
        }
        else {
            //TODO add toast if root view is null
            if(rootView != null){
                //TODO add toast indicating that the location is not there.
                disabledUi(rootView);
            }
        }
    }

    private void checkIn(View rootView) {
        currentCrawler.checkIn(new SimplifiedLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), currentAddress);
        userInfo.setValue(currentCrawler);

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        mNewValues.put(PubContract.CrawlerLocation.GENDER, currentCrawler.getGender().name());
        mNewValues.put(PubContract.CrawlerLocation.COLUMN_LAST_ADDRESS, currentCrawler.getLastAddress());
        mNewValues.put(PubContract.CrawlerLocation.COLUMN_TIMESTAMP, currentCrawler.getCheckInTimeStamp());
        mNewValues.put(PubContract.CrawlerLocation.COLUMN_COORD_LAT, currentCrawler.getLastLocation().getLatitude());
        mNewValues.put(PubContract.CrawlerLocation.COLUMN_COORD_LONG, currentCrawler.getLastLocation().getLongitude());

        getActivity().getContentResolver().update(
                PubContract.CrawlerLocation.CONTENT_URI,
                mNewValues,
                null,
                null
        );

        updateChecked(rootView, true);

        openDetails();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_CAT,"location connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_CAT,"location connection failed " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        final double distance = PlayServicesHelper.distance(lastLocation, location);
        if(distance > Crawler.GEO_DISTANCE){
            Log.i(LOG_CAT, "Distance between old and new location:" + distance);
            lastLocation = location;
            fetchAddress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(googleApiClient != null && googleApiClient.isConnected()){
            PlayServicesHelper.stopLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            PlayServicesHelper.startLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private void updateChecked(@NonNull final View view, final boolean checkedIn) {
        final TextView checkInText = (TextView)view.findViewById(R.id.checkInText);
        final ImageButton checkInButton = (ImageButton)view.findViewById(R.id.checkinButton);
        final Button whatIsHot = (Button) view.findViewById(R.id.what_is_hot);
        final TextView viewDetails = (TextView)view.findViewById(R.id.open_details);
        checkInText.setVisibility(View.VISIBLE);
        checkInButton.setVisibility(View.VISIBLE);
        whatIsHot.setVisibility(View.VISIBLE);
        if(checkedIn){
            checkInText.setText(R.string.checked_in);
            checkInButton.setBackground(view.getResources().getDrawable(R.drawable.checked_in_button));
            viewDetails.setVisibility(View.VISIBLE);
        }
        else {
            checkInText.setText(R.string.checkIn);
            checkInButton.setBackground(view.getResources().getDrawable(R.drawable.checkin_button));
            viewDetails.setVisibility(View.GONE);
        }
    }

    private void fetchAddress() {
        if(addressReceiver == null){
            //creating the service addressReceiver.
            addressReceiver = new AddressResultReceiver(new Handler());
        }
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, addressReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, lastLocation);
        getActivity().startService(intent);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri crawlerUri = PubContract.CrawlerLocation.buildCrawlerUri();

        return new CursorLoader(getActivity(),
                crawlerUri,
                PubContract.CrawlerLocation.COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            final String id = data.getString(data.getColumnIndex(PubContract.CrawlerLocation._ID));
            final Crawler.Gender gender = Crawler.Gender.valueOf(data.getString(data.getColumnIndex(PubContract.CrawlerLocation.GENDER)));
            currentCrawler = new Crawler(id, gender);

            final String address = data.getString(data.getColumnIndex(PubContract.CrawlerLocation.COLUMN_LAST_ADDRESS));

            if(address != null && !address.isEmpty()){

                currentCrawler.setLastAddress(address);

                final long checkInTimeStamp = data.getLong(data.getColumnIndex(PubContract.CrawlerLocation.COLUMN_TIMESTAMP));
                currentCrawler.setCheckInTimeStamp(checkInTimeStamp);

                final double latitude = data.getDouble(data.getColumnIndex(PubContract.CrawlerLocation.COLUMN_COORD_LAT));
                final double longitude = data.getDouble(data.getColumnIndex(PubContract.CrawlerLocation.COLUMN_COORD_LONG));
                currentCrawler.setLastLocation(new SimplifiedLocation(latitude,longitude));
            }

            if(lastLocation != null){
                updateChecked(getView(), currentCrawler.isCheckedIn(lastLocation));
            }

            userInfo = new Firebase("https://boiling-fire-4188.firebaseio.com/crawlers/" + currentCrawler.getUserId());
        }
        else {
            Log.e(LOG_CAT, "Default user not found in BD, forcing creation.");
            final String androidId = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            currentCrawler = new Crawler(androidId, Crawler.Gender.UNDEFINED);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CRAWLER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        //nothing to do!
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            currentAddress = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                updateChecked(getView(),currentCrawler != null && currentAddress.equals(currentCrawler.getLastAddress()));
            }
        }
    }

    private final static String LOG_CAT = CheckInFragment.class.getName();
    private static final String CRAWLER = "CRAWLER";
    public static final String PUB_ADDRESS = "PUB_ADDRESS";
    private static final String CURRENT_ADDRESS = "CURRENT_ADDRESS" ;
    private static final String LAST_LOCATION = "LAST_LOCATION";
    private static final int CRAWLER_LOADER = 0;
}
