package com.jalloro.android.pubcrawler.detail;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.chart.Bar;
import com.jalloro.android.pubcrawler.chart.BarChart;
import com.jalloro.android.pubcrawler.data.PubContract;
import com.jalloro.android.pubcrawler.model.AddressInfo;
import com.jalloro.android.pubcrawler.model.Place;
import com.jalloro.android.pubcrawler.model.PriceRange;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.welcome.CheckInFragment;

import java.util.ArrayList;
import java.util.List;

public class PubDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String VERTICAL = "VERTICAL";
    private Place currentPlace;
    private PlaceResultReceiver placeReceiver;
    private SimplifiedLocation currentLocation;
    private ShareActionProvider shareActionProvider;


    public PubDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pub_detail, container, false);

        Bundle intent = getActivity().getIntent().getExtras();

        initUi(rootView, intent);

        return rootView;
    }

    private void initUi(View rootView, Bundle intent) {
        final String address = intent.getString(CheckInFragment.PUB_ADDRESS);

        currentLocation = intent.getParcelable(CURRENT_LOCATION);

        final SimplifiedLocation pubLocation = intent.getParcelable(PUB_LOCATION);

        final boolean verticalOrientation = intent.getBoolean(PubDetailFragment.VERTICAL, false);

        currentPlace = new Place(pubLocation, address);

        placeReceiver = new PlaceResultReceiver(new Handler());

        //change layout orientation based on phone orientation.
        final int phoneOrientation = getResources().getConfiguration().orientation;
        LinearLayout mainLayout = (LinearLayout) rootView.findViewById(R.id.main_pub_detail);
        if(phoneOrientation == Configuration.ORIENTATION_LANDSCAPE && !verticalOrientation){
            mainLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout header = (LinearLayout) rootView.findViewById(R.id.detail_header);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            header.setLayoutParams(params);
        }
        else {
            mainLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_pub_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(currentPlace.getName() != null){
            shareActionProvider.setShareIntent(createSharePlace());
        }
    }

    private Intent createSharePlace() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Meet me in " + currentPlace.getName() + ", address: " + currentPlace.getAddress() + " #pubCrawler");
        return shareIntent;
    }

    private void updateUi(View rootView, final Place currentPlace) {
        TextView pubName = (TextView) rootView.findViewById(R.id.pub_name);
        TextView pubPrice = (TextView) rootView.findViewById(R.id.pub_price);
        TextView pubAddress = (TextView) rootView.findViewById(R.id.pub_address);
        pubName.setText(currentPlace.getName());
        pubPrice.setText(currentPlace.getPriceRange().getLabel());
        pubAddress.setText(currentPlace.getAddress());
        pubAddress.setLinksClickable(true);
        pubAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri mapUri = Uri.parse(MAPS_BASE_URL).buildUpon()
                        .appendQueryParameter(FROM_LOCATION, currentLocation.toString())
                        .appendQueryParameter(TO_LOCATION, currentPlace.getLocation().toString())
                        .build();

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,mapUri);
                startActivity(intent);
            }
        });

        updateStatusChart(rootView, currentPlace);

        if(shareActionProvider != null){
            shareActionProvider.setShareIntent(createSharePlace());
        }
    }

    private void updateStatusChart(View rootView, Place currentPlace) {
        List<Bar> values = new ArrayList<>();
        values.add(new Bar(getString(R.string.now), currentPlace.getNow(), R.color.light_blue));
        values.add(new Bar(getString(R.string.historic), currentPlace.getHistoric(), R.color.pink));
        BarChart chart = (BarChart) rootView.findViewById(R.id.hot_chart);
        chart.setData(values);
    }

    private void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchPlaceIntentService.class);
        intent.putExtra(FetchPlaceIntentService.Constants.RECEIVER, placeReceiver);
        intent.putExtra(FetchPlaceIntentService.Constants.ADDRESS_DATA_EXTRA, currentPlace.getAddress());
        intent.putExtra(FetchPlaceIntentService.Constants.LOCATION_DATA_EXTRA, currentPlace.getLocation());

        String foursquareId = getResources().getString(R.string.foursquare_id);
        String foursquareSecret = getResources().getString(R.string.foursquare_secret);

        intent.putExtra(FetchPlaceIntentService.Constants.FOURSQUARE_ID, foursquareId);
        intent.putExtra(FetchPlaceIntentService.Constants.FOURSQUARE_SECRET, foursquareSecret);
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String condition = PubContract.WhatIsHot.HOT_ID + " = " + DatabaseUtils.sqlEscapeString(currentPlace.getAddress());

        return new CursorLoader(getActivity(),
                PubContract.WhatIsHot.CONTENT_URI,
                PubContract.WhatIsHot.COLUMNS,
                condition,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            final int nameIndex = data.getColumnIndex(PubContract.WhatIsHot.COLUMN_NAME);
            if(!data.isNull(nameIndex)){
                final String placeName = data.getString(nameIndex);
                currentPlace.setName(placeName);
                currentPlace.getLocation().setLatitude(data.getLong(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_COORD_LAT)));
                currentPlace.getLocation().setLongitude(data.getLong(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_COORD_LONG)));
                final String price = data.getString(data.getColumnIndex(PubContract.WhatIsHot.COLUMN_PRICE));
                currentPlace.setPriceRange(PriceRange.valueOf(price));

                //let's look for checkIn data
                final int actualIndex = data.getColumnIndex(PubContract.WhatIsHot.COLUMN_NOW);
                //adding db data and current checkIn
                currentPlace.setNow(data.getLong(actualIndex));
                final int plannedIndex = data.getColumnIndex(PubContract.WhatIsHot.COLUMN_HISTORIC);
                currentPlace.setHistoric(data.getLong(plannedIndex));
                updateUi(getView(), currentPlace);

            }
            else {
                //no info on place so we need to fetch it.
                startIntentService();
            }

        }
        else {
            //no info on place so we need to fetch it
            startIntentService();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //nothing to do!
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PLACE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void locationChanged(Bundle args) {

        initUi(getView(),args);
        getLoaderManager().restartLoader(PLACE_LOADER, null, this);
    }

    class PlaceResultReceiver extends ResultReceiver {
        public PlaceResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {

            AddressInfo addressInfo = resultData.getParcelable(FetchPlaceIntentService.Constants.RESULT_DATA_KEY);
            if (resultCode == FetchPlaceIntentService.Constants.FAILURE_RESULT) {
                Place place = new Place(addressInfo.getLocation(),addressInfo.getAddress());
                place.setName(addressInfo.getName());
                place.setPriceRange(addressInfo.getPriceRange());
                place.setNow(1);
                //On all the apollo missions only 12 guys walked on the moon.
                place.setHistoric(12 + 1);
                updateUi(getView(), place);
                Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, getString(R.string.place_not_found), duration);
                toast.show();
            }
        }
    }

    private static final String MAPS_BASE_URL = "http://maps.google.com/maps";
    private static final String FROM_LOCATION = "saddr";
    private static final String TO_LOCATION = "daddr";
    public static final String PUB_LOCATION = "PUB_LOCATION";
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    private static final int PLACE_LOADER = 1;
}
