package com.jalloro.android.pubcrawler.welcome;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.helpers.PlayServicesHelper;

public class WelcomeFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    private boolean checkedIn;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    LocationRequest locationRequest;
    private ResultReceiver resultReceiver;
    private String lastAddress;

    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_welcome_checkin, container, false);
        final ImageButton checkInButton = (ImageButton)rootView.findViewById(R.id.checkinButton);

        if(PlayServicesHelper.isGooglePlayInstalled(getActivity())){
            resultReceiver = new AddressResultReceiver(new Handler());
            buildGoogleApiClient();
            locationRequest = PlayServicesHelper.createLocationRequest(10000, 50000, LocationRequest.PRIORITY_HIGH_ACCURACY);
            googleApiClient.connect();

            checkedIn = savedInstanceState != null && Boolean.parseBoolean(savedInstanceState.get(CHECKED_IN).toString());
            updateChecked(rootView, checkedIn);

            checkInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!checkedIn){
                        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        startIntentService();
                        checkedIn = true;
                        updateChecked(rootView, true);
                    }
                }
            });
        }
        else {
           checkInButton.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CHECKED_IN,Boolean.toString(checkedIn));
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
       // checkedIn = false;
       // updateChecked(getView(),false);
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
        }
        else {
            checkInText.setText(R.string.checkIn);
            checkInButton.setBackground(view.getResources().getDrawable(R.drawable.checkin_button));
        }
    }

    private void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, lastLocation);
        getActivity().startService(intent);
    }



    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                //showToast(getString(R.string.address_found));
                if(!addressOutput.equals(lastAddress)){
                    checkedIn = false;
                    updateChecked(getView(),false);
                    lastAddress = addressOutput;
                }
            }

        }
    }

    private final static String LOG_CAT = WelcomeFragment.class.getName();
    private static final String CHECKED_IN = "CHECKED_IN";

}
