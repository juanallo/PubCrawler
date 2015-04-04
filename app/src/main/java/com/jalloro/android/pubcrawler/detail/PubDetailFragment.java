package com.jalloro.android.pubcrawler.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.chart.Bar;
import com.jalloro.android.pubcrawler.chart.BarChart;
import com.jalloro.android.pubcrawler.model.Place;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.welcome.WelcomeFragment;

import java.util.ArrayList;
import java.util.List;

public class PubDetailFragment extends Fragment {

    private Place currentPlace;
    private PlaceResultReceiver placeReceiver;
    private SimplifiedLocation currentLocation;

    public PubDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pub_detail, container, false);

        Intent intent = getActivity().getIntent();

        final String address = intent.getStringExtra(WelcomeFragment.PUB_ADDRESS);

        final double pubLongitude = intent.getDoubleExtra(PUB_LOCATION_LONGITUDE, 0);
        final double pubLatitude = intent.getDoubleExtra(PUB_LOCATION_LATITUDE, 0);

        final double currentLongitude = intent.getDoubleExtra(CURRENT_LOCATION_LONGITUDE, 0);
        final double currentLatitude = intent.getDoubleExtra(CURRENT_LOCATION_LATITUDE, 0);

        currentLocation = new SimplifiedLocation(currentLatitude, currentLongitude);
        currentPlace = new Place(new SimplifiedLocation(pubLatitude,pubLongitude), address);

        placeReceiver = new PlaceResultReceiver(new Handler());

        startIntentService();

        //TODO fetch amount of users from firebase
        currentPlace.setRealAmountOfMen(3000);
        currentPlace.setRealAmountOfWomen(2000);

        currentPlace.setPlannedAmountOfMen(3200);
        currentPlace.setPlannedAmountOfWomen(6000);

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

        List<Bar> values = new ArrayList<>();
        values.add(new Bar("Now\nMen", currentPlace.getRealAmountOfMen(), R.color.light_blue));
        values.add(new Bar("Now\nWomen", currentPlace.getPlannedAmountOfWomen(), R.color.pink));
        values.add(new Bar("Planned\nMen", currentPlace.getPlannedAmountOfMen(), R.color.light_blue));
        values.add(new Bar("Planned\nWomen", currentPlace.getPlannedAmountOfWomen(), R.color.pink));
        BarChart chart = (BarChart) rootView.findViewById(R.id.hot_chart);
        chart.setData(values);
    }

    private void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchPlaceIntentService.class);
        intent.putExtra(FetchPlaceIntentService.Constants.RECEIVER, placeReceiver);
        intent.putExtra(FetchPlaceIntentService.Constants.ADDRESS_DATA_EXTRA, currentPlace.getAddress());
        getActivity().startService(intent);
    }

    class PlaceResultReceiver extends ResultReceiver {
        public PlaceResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {

            AddressInfo addressInfo = resultData.getParcelable(FetchPlaceIntentService.Constants.RESULT_DATA_KEY);

            if (resultCode == FetchPlaceIntentService.Constants.SUCCESS_RESULT) {
                currentPlace.setName(addressInfo.getName());
                currentPlace.setPriceRange(addressInfo.getPriceRange());
                updateUi(getView(), currentPlace);
            }
        }
    }

    private static final String MAPS_BASE_URL = "http://maps.google.com/maps";
    private static final String FROM_LOCATION = "saddr";
    private static final String TO_LOCATION = "daddr";
    public static final String PUB_LOCATION_LONGITUDE = "PUB_LOCATION_LONGITUDE";
    public static final String PUB_LOCATION_LATITUDE = "PUB_LOCATION_LATITUDE";
    public static final String CURRENT_LOCATION_LONGITUDE = "CURRENT_LOCATION_LONGITUDE";
    public static final String CURRENT_LOCATION_LATITUDE = "CURRENT_LOCATION_LATITUDE";
}
