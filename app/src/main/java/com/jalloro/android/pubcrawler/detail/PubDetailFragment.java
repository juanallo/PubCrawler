package com.jalloro.android.pubcrawler.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    public PubDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pub_detail, container, false);


        Intent intent = getActivity().getIntent();

        final String address = intent.getStringExtra(WelcomeFragment.PUB_ADDRESS);
        final double longitude = intent.getDoubleExtra(WelcomeFragment.PUB_LOCATION_LONGITUDE, 0);
        final double latitude = intent.getDoubleExtra(WelcomeFragment.PUB_LOCATION_LATITUDE, 0);

        currentPlace = new Place(new SimplifiedLocation(latitude,longitude), address);

        //TODO fetch from forsquare
        currentPlace.setName("La colmena");
        currentPlace.setPriceRangeFromValue(2);

        //TODO fetch amount of users from firebase
        currentPlace.setRealAmountOfMen(3000);
        currentPlace.setRealAmountOfWomen(2000);

        currentPlace.setPlannedAmountOfMen(3200);
        currentPlace.setPlannedAmountOfWomen(6000);

        updateUi(rootView, currentPlace);

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
                //TODO get real destination
                Uri mapUri = Uri.parse(MAPS_BASE_URL).buildUpon()
                        .appendQueryParameter(FROM_LOCATION, "20.344,34.34")
                        .appendQueryParameter(TO_LOCATION, currentPlace.getLocation().toString())
                        .build();

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,mapUri);
                startActivity(intent);
            }
        });

        List<Bar> values = new ArrayList<>();
        values.add(new Bar("Now\nMen", currentPlace.getRealAmountOfMen(), R.color.light_blue));
        values.add(new Bar("Now\nWomen", currentPlace.getPlannedAmountOfWomen(), R.color.pink));
        values.add(new Bar("Planned\nMen", currentPlace.getPlannedAmountOfMen(), R.color.light_blue) );
        values.add(new Bar("Planned\nWomen", currentPlace.getPlannedAmountOfWomen(), R.color.pink));
        BarChart chart = (BarChart) rootView.findViewById(R.id.hot_chart);
        chart.setData(values);
    }

    private static final String MAPS_BASE_URL = "http://maps.google.com/maps";
    private static final String FROM_LOCATION = "saddr";
    private static final String TO_LOCATION = "daddr";
}
