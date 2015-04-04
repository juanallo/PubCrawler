package com.jalloro.android.pubcrawler.welcome;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.detail.PubDetailActivity;
import com.jalloro.android.pubcrawler.detail.PubDetailFragment;
import com.jalloro.android.pubcrawler.helpers.PlayServicesHelper;
import com.jalloro.android.pubcrawler.model.Crawler;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;

import java.util.Map;

public class WelcomeFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private ResultReceiver addressReceiver;
    private String currentAddress;
    private Firebase userInfo;
    private Crawler currentCrawler;


    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_welcome_checkin, container, false);
        final ImageButton checkInButton = (ImageButton)rootView.findViewById(R.id.checkinButton);

        if(savedInstanceState!= null){
            currentCrawler = savedInstanceState.getParcelable(CRAWLER);
            currentAddress = savedInstanceState.getString(CURRENT_ADDRESS);
        }
        else {
            final String androidId = Settings.Secure.getString(getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            //first time, creating default crawler;
            currentCrawler = new Crawler(androidId, Crawler.Gender.UNDEFINED);
        }

        if(PlayServicesHelper.isGooglePlayInstalled(getActivity())){
            //setting Firebase
            Firebase.setAndroidContext(getActivity());

            userInfo = new Firebase("https://boiling-fire-4188.firebaseio.com/crawlers/" + currentCrawler.getUserId());

            userInfo.addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    private SimplifiedLocation lastLocation;
                    if(dataSnapshot.getValue()!= null){
                        Map<String, Object> crawlerInfo = (Map<String, Object>) dataSnapshot.getValue();
                        currentCrawler.setLastAddress((String)crawlerInfo.get("lastAddress"));
                        currentCrawler.setFacebookId((String) crawlerInfo.get("facebookId"));
                        if(crawlerInfo.containsKey("checkInTimeStamp")){
                           currentCrawler.setCheckInTimeStamp(Long.parseLong(crawlerInfo.get("checkInTimeStamp").toString()));
                        }
                        if(crawlerInfo.containsKey("lastLocation")){
                            Map<String, Object> locationInfo = (Map<String, Object>) crawlerInfo.get("lastLocation");
                            currentCrawler.setLastLocation(new SimplifiedLocation((double)locationInfo.get("longitude"), (double) locationInfo.get("latitude")));
                        }
                        currentCrawler.setGender(Crawler.Gender.valueOf((String) crawlerInfo.get("gender")));

                        updateChecked(getView(), currentCrawler.isCheckedIn(currentAddress));
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(LOG_CAT, firebaseError.getMessage());
                }
            });

            //creating the service addressReceiver.
            addressReceiver = new AddressResultReceiver(new Handler());

            //creating Google Api for location
            buildGoogleApiClient();

            //creating a location request
            locationRequest = PlayServicesHelper.createLocationRequest(10000, 50000, LocationRequest.PRIORITY_HIGH_ACCURACY);

            //connecting to google api
            googleApiClient.connect();

            checkInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!currentCrawler.isCheckedIn(currentAddress)){
                        currentCrawler.checkIn(new SimplifiedLocation(lastLocation.getLongitude(),lastLocation.getLatitude()), currentAddress);
                        userInfo.setValue(currentCrawler);
                        updateChecked(rootView, true);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                openDetails();
                            }
                        }, DETAIL_DELAY);
                    }
                }
            });

            final TextView viewDetails = (TextView)rootView.findViewById(R.id.open_details);
            viewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetails();
                }
            });
        }
        else {
           checkInButton.setEnabled(false);
        }

        return rootView;
    }

    private void openDetails() {
        Intent intent = new Intent(getActivity(), PubDetailActivity.class);
        intent.putExtra(PUB_ADDRESS, currentCrawler.getLastAddress());
        //current and pub location are the same
        intent.putExtra(PubDetailFragment.PUB_LOCATION_LATITUDE, currentCrawler.getLastLocation().getLatitude());
        intent.putExtra(PubDetailFragment.PUB_LOCATION_LONGITUDE, currentCrawler.getLastLocation().getLongitude());
        intent.putExtra(PubDetailFragment.CURRENT_LOCATION_LATITUDE, currentCrawler.getLastLocation().getLatitude());
        intent.putExtra(PubDetailFragment.CURRENT_LOCATION_LONGITUDE, currentCrawler.getLastLocation().getLongitude());
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CRAWLER,currentCrawler);
        outState.putString(CURRENT_ADDRESS, currentAddress);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnected(Bundle bundle) {
        PlayServicesHelper.startLocationUpdates(googleApiClient, locationRequest, this);
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
        lastLocation = location;
        startIntentService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(googleApiClient != null){
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

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void updateChecked(@NonNull final View view, final boolean checkedIn) {
        TextView checkInText = (TextView)view.findViewById(R.id.checkInText);
        final ImageButton checkInButton = (ImageButton)view.findViewById(R.id.checkinButton);
        if(checkedIn){
            checkInText.setText(R.string.checked_in);
            checkInButton.setBackground(view.getResources().getDrawable(R.drawable.checked_in_button));

            final TextView viewDetails = (TextView)view.findViewById(R.id.open_details);
            viewDetails.setVisibility(View.VISIBLE);
        }
        else {
            checkInText.setText(R.string.checkIn);
            checkInButton.setBackground(view.getResources().getDrawable(R.drawable.checkin_button));
        }
    }

    private void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, addressReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, lastLocation);
        getActivity().startService(intent);
    }



    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {

            currentAddress = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);

            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                updateChecked(getView(),currentAddress.equals(currentCrawler.getLastAddress()));
            }
        }
    }

    private final static String LOG_CAT = WelcomeFragment.class.getName();
    private static final String CRAWLER = "CRAWLER";
    public static final String PUB_ADDRESS = "PUB_ADDRESS";
    private static final int DETAIL_DELAY = 1000;
    private static final String CURRENT_ADDRESS = "CURRENT_ADDRESS" ;

}
