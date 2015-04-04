package com.jalloro.android.pubcrawler.near;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.detail.PubDetailActivity;
import com.jalloro.android.pubcrawler.detail.PubDetailFragment;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.welcome.WelcomeFragment;

import java.util.Map;

public class WhatIsHotActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {

    private static final String YOU = "You";
    private static final String LOG_CAT = WhatIsHotActivity.class.getName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private SimplifiedLocation currentLocation;
    private SimplifiedLocation crawlerLocation;
    private String crawlerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentLocation = getIntent().getParcelableExtra(PubDetailFragment.CURRENT_LOCATION);
        crawlerLocation = getIntent().getParcelableExtra(PubDetailFragment.PUB_LOCATION);
        crawlerAddress = getIntent().getStringExtra(WelcomeFragment.PUB_ADDRESS);


        Firebase.setAndroidContext(this);

        final Firebase firebase = new Firebase("https://boiling-fire-4188.firebaseio.com/pubs/");


        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.getValue()!= null) {
                    Map<String, Object> crawlerInfo = (Map<String, Object>) snapshot.getValue();
                    for (String placeAddress : crawlerInfo.keySet()){
                        if(!placeAddress.equals(crawlerAddress.replace("\n", " "))){
                            Map<String, Object> place = (Map<String, Object>) crawlerInfo.get(placeAddress);
                            if(place.containsKey("location")){
                                Map<String, Object> locationInfo = (Map<String, Object>) place.get("location");
                                final LatLng position = new LatLng((double) locationInfo.get("latitude"),(double)locationInfo.get("longitude"));
                                final String name = (String) place.get("name");
                                mMap.addMarker(new MarkerOptions().position(position).title(name).snippet(placeAddress));
                            }
                        }
                    }
                };
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_CAT,"The read failed: " + firebaseError.getMessage());
            }
        });


        setContentView(R.layout.activity_near);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        final LatLng position = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        final Marker you = mMap.addMarker(new MarkerOptions().position(position).title(YOU).icon(BitmapDescriptorFactory.fromResource(R.drawable.you)));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

        mMap.setOnMarkerClickListener(this);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle().equals(YOU)){
            openDetails(currentLocation, currentLocation, crawlerAddress);
        }
        else {
            final LatLng position = marker.getPosition();
            openDetails(currentLocation, new SimplifiedLocation(position.latitude, position.longitude), marker.getSnippet());
        }

        return false;
    }

    private void openDetails(SimplifiedLocation currentLocation, SimplifiedLocation pubLocation, String pubAddress) {
        Intent intent = new Intent(this, PubDetailActivity.class);
        intent.putExtra(WelcomeFragment.PUB_ADDRESS, pubAddress);
        //current and pub location are the same
        intent.putExtra(PubDetailFragment.PUB_LOCATION, pubLocation);
        intent.putExtra(PubDetailFragment.CURRENT_LOCATION, currentLocation);
        startActivity(intent);
    }
}
