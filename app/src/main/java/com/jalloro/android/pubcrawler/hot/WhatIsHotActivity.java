package com.jalloro.android.pubcrawler.hot;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.data.PubContract;
import com.jalloro.android.pubcrawler.detail.PubDetailActivity;
import com.jalloro.android.pubcrawler.detail.PubDetailFragment;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.welcome.CheckInFragment;

import java.util.HashMap;
import java.util.Map;

public class WhatIsHotActivity
        extends ActionBarActivity
        implements GoogleMap.OnInfoWindowClickListener, LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private SimplifiedLocation currentLocation;
    private SimplifiedLocation crawlerLocation;
    private String crawlerAddress;
    private Map<String, Marker> places;
    private boolean tablet;
    private Marker selectedMarker;
    private String selectedMarkerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentLocation = getIntent().getParcelableExtra(PubDetailFragment.CURRENT_LOCATION);
        crawlerLocation = getIntent().getParcelableExtra(PubDetailFragment.PUB_LOCATION);
        crawlerAddress = getIntent().getStringExtra(CheckInFragment.PUB_ADDRESS);

        if(savedInstanceState!= null){
            selectedMarkerTitle = savedInstanceState.getString(SELECTED, null);
        }

        places = new HashMap<>();
        Loader<Object> loader = getLoaderManager().getLoader(0);
        if (loader != null && ! loader.isReset()) {
            getLoaderManager().restartLoader(0, null, this);
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
        setContentView(R.layout.activity_near);

        final View selectedView = findViewById(R.id.pub_detail);
        tablet = selectedView != null;
        if(tablet){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.pub_detail, new PubDetailFragment(), DETAILFRAGMENT_TAG)
                    .commit();
            if(selectedMarkerTitle != null){
                selectedView.setVisibility(View.VISIBLE);
            }
        }
        setUpMapIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(selectedMarker != null){
            outState.putString(SELECTED, selectedMarker.getTitle());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
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
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        final LatLng position = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        final Marker you = map.addMarker(new MarkerOptions().position(position).title(YOU).icon(BitmapDescriptorFactory.fromResource(R.drawable.you)));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

        if(tablet){
            map.setOnMarkerClickListener(this);
            map.setOnMapClickListener(this);
            if(selectedMarkerTitle  != null && selectedMarkerTitle.equals(YOU)){
                onMarkerClick(you);
            }
        }
        else {
            map.setOnInfoWindowClickListener(this);
        }
    }

    private void openDetails(SimplifiedLocation currentLocation, SimplifiedLocation pubLocation, String pubAddress) {
        Intent intent = new Intent(this, PubDetailActivity.class);
        intent.putExtra(CheckInFragment.PUB_ADDRESS, pubAddress);
        //current and pub location are the same
        intent.putExtra(PubDetailFragment.PUB_LOCATION, pubLocation);
        intent.putExtra(PubDetailFragment.CURRENT_LOCATION, currentLocation);
        startActivity(intent);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.getTitle().equals(YOU)){
            openDetails(currentLocation, currentLocation, crawlerAddress);
        }
        else {
            final LatLng position = marker.getPosition();
            openDetails(currentLocation, new SimplifiedLocation(position.latitude, position.longitude), marker.getSnippet());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                PubContract.WhatIsHot.CONTENT_URI,
                PubContract.WhatIsHot.COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        while (data.moveToNext())
        {
            final String address = data.getString(data.getColumnIndex(PubContract.WhatIsHot._ID));
            if(!places.containsKey(address)){
                final long actualCrawlers = data.getLong(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_NOW));
                final double latitude = data.getDouble(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_COORD_LAT));
                final double longitude = data.getDouble(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_COORD_LONG));
                final LatLng position = new LatLng(latitude,longitude);
                final String name = data.getString(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_NAME));
                final float alpha;
                if(actualCrawlers > 0){
                    alpha = 1;
                }
                else {
                    alpha = 0.4f;
                }
                final Marker marker = map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(name)
                        .snippet(address).alpha(alpha));
                places.put(address, marker);
            }
        }
        if(selectedMarkerTitle != null && !selectedMarkerTitle.equals(YOU)){
            for (Marker marker : places.values()){
                if(marker.getTitle().equals(selectedMarkerTitle)){
                    onMarkerClick(marker);
                    break;
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //nothing to do!
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(selectedMarker != null){
            if(selectedMarker.getTitle().equals(YOU)){
               selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.you));
            }
            else {
                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        }
        selectedMarker = marker;
        if(marker.getTitle().equals(YOU)){
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.you_selected));
        }
        else {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }

        PubDetailFragment df = (PubDetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);

        if(df != null){
            Bundle args = new Bundle();
            if(marker.getTitle().equals(YOU)){
                args.putString(CheckInFragment.PUB_ADDRESS,crawlerAddress);
            }
            else {
                args.putString(CheckInFragment.PUB_ADDRESS, marker.getSnippet());
            }
            args.putParcelable(PubDetailFragment.CURRENT_LOCATION, currentLocation);
            final SimplifiedLocation pubLocation = new SimplifiedLocation(marker.getPosition().latitude, marker.getPosition().longitude);
            args.putParcelable(PubDetailFragment.PUB_LOCATION, pubLocation);
            args.putBoolean(PubDetailFragment.VERTICAL, true);

            final View details = findViewById(R.id.pub_detail);

            df.locationChanged(args);
            details.setVisibility(View.VISIBLE);
        }
        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(selectedMarker != null){
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        selectedMarker = null;
        final View details = findViewById(R.id.pub_detail);
        details.setVisibility(View.GONE);
    }

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String YOU = "You";
    private static final String LOG_CAT = WhatIsHotActivity.class.getName();
    private static final String SELECTED = "Selected";

}
