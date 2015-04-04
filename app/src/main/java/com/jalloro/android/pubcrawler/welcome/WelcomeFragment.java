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
                            currentCrawler.setLastLocation(new SimplifiedLocation((double) locationInfo.get("latitude"),(double)locationInfo.get("longitude")));
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
                        if(lastLocation != null){
                            currentCrawler.checkIn(new SimplifiedLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), currentAddress);
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
                        else {
                            //TODO add toast indicating that the location is not there.
                        }

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
        intent.putExtra(PubDetailFragment.PUB_LOCATION, currentCrawler.getLastLocation());
        intent.putExtra(PubDetailFragment.CURRENT_LOCATION, currentCrawler.getLastLocation());
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

//    public void createTestUsers(){
//        final Firebase firebase = new Firebase("https://boiling-fire-4188.firebaseio.com/crawlers");
//        String address = "760 Mission Court \nFremont, California 94539";
//        Map<String, Crawler> crawlers = new HashMap<String, Crawler>();
//        crawlers.put(currentCrawler.getUserId(), currentCrawler);
//        for(int i = 0 ; i <100; i++){
//            Crawler crawler = new Crawler(UUID.randomUUID().toString(), Crawler.Gender.UNDEFINED);
//            crawler.checkIn(new SimplifiedLocation(37.489933, -121.930999), address);
//            crawlers.put(crawler.getUserId(),crawler);
//        }
//        address = "722 Edgewater Blvd \nFoster City, California 94404";
//        for(int i = 0 ; i <100; i++){
//            Crawler crawler = new Crawler(UUID.randomUUID().toString(), Crawler.Gender.UNDEFINED);
//            crawler.checkIn(new SimplifiedLocation(37.5535048, -122.2743742), address);
//            crawlers.put(crawler.getUserId(),crawler);
//        }
//        address = "744 Edgewater Blvd \nFoster City, California 94404";
//        for(int i = 0 ; i <100; i++){
//            Crawler crawler = new Crawler(UUID.randomUUID().toString(), Crawler.Gender.UNDEFINED);
//            crawler.checkIn(new SimplifiedLocation(37.552946, -122.274577), address);
//            crawlers.put(crawler.getUserId(),crawler);
//        }
//        firebase.setValue(crawlers);
//    }

    private final static String LOG_CAT = WelcomeFragment.class.getName();
    private static final String CRAWLER = "CRAWLER";
    public static final String PUB_ADDRESS = "PUB_ADDRESS";
    private static final int DETAIL_DELAY = 1000;
    private static final String CURRENT_ADDRESS = "CURRENT_ADDRESS" ;

}
