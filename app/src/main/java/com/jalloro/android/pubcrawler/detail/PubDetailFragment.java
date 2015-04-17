package com.jalloro.android.pubcrawler.detail;

import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private Place currentPlace;
    private PlaceResultReceiver placeReceiver;
    private SimplifiedLocation currentLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pub_detail, container, false);

        Intent intent = getActivity().getIntent();

        final String address = intent.getStringExtra(CheckInFragment.PUB_ADDRESS);

        currentLocation = intent.getParcelableExtra(CURRENT_LOCATION);

        final SimplifiedLocation pubLocation = intent.getParcelableExtra(PUB_LOCATION);

        currentPlace = new Place(pubLocation, address);

        placeReceiver = new PlaceResultReceiver(new Handler());

        return rootView;
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
    }

    private void updateStatusChart(View rootView, Place currentPlace) {
        List<Bar> values = new ArrayList<>();
        values.add(new Bar("Now", currentPlace.getRealAmountOfUndefined(), R.color.light_blue));
        values.add(new Bar("Planned", currentPlace.getPlannedAmountOfUndefined(), R.color.pink));
        BarChart chart = (BarChart) rootView.findViewById(R.id.hot_chart);
        chart.setData(values);
    }

    private void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchPlaceIntentService.class);
        intent.putExtra(FetchPlaceIntentService.Constants.RECEIVER, placeReceiver);
        intent.putExtra(FetchPlaceIntentService.Constants.ADDRESS_DATA_EXTRA, currentPlace.getAddress());
        intent.putExtra(FetchPlaceIntentService.Constants.LOCATION_DATA_EXTRA, currentPlace.getLocation());
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Uri crawlerUri = PubContract.WhatIsHot.buildCurrentPubUri(currentPlace.getAddress());
//
//        return new CursorLoader(getActivity(),
//                crawlerUri,
//                PubContract.WhatIsHot.COLUMNS,
//                null,
//                null,
//                null);
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
            }
            else {
                //no info on place so we need to fetch it.
                startIntentService();
            }
            //let's look for checkIn data
            final int actualIndex = data.getColumnIndex(PubContract.WhatIsHot.COLUMN_ACTUAL_UNDEFINED);
            //adding db data and current checkIn
            currentPlace.setRealAmountOfUndefined(data.getLong(actualIndex));
            final int plannedIndex = data.getColumnIndex(PubContract.WhatIsHot.COLUMN_PLANNED_UNDEFINED);
            currentPlace.setPlannedAmountOfUndefined(data.getLong(plannedIndex));

            updateUi(getView(), currentPlace);
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

    class PlaceResultReceiver extends ResultReceiver {
        public PlaceResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {

            AddressInfo addressInfo = resultData.getParcelable(FetchPlaceIntentService.Constants.RESULT_DATA_KEY);
            if (resultCode == FetchPlaceIntentService.Constants.FAILURE_RESULT) {
                //TODO update UI with moon details!
                // updateUi(getView(), addressInfo);
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
