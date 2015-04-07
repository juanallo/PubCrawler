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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.chart.Bar;
import com.jalloro.android.pubcrawler.chart.BarChart;
import com.jalloro.android.pubcrawler.model.Place;
import com.jalloro.android.pubcrawler.model.SimplifiedLocation;
import com.jalloro.android.pubcrawler.welcome.CheckInFragment;

import java.util.ArrayList;
import java.util.List;

public class PubDetailFragment extends Fragment {

    private Place currentPlace;
    private PlaceResultReceiver placeReceiver;
    private SimplifiedLocation currentLocation;
    private Firebase placeDetailsDataBase;

    public PubDetailFragment() {

    }

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

        startIntentService();

        final Firebase firebase = new Firebase("https://boiling-fire-4188.firebaseio.com/crawlers");
        firebase.orderByChild("lastAddress").equalTo(address).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO ADD WOMEN/MEN
                //TODO CHECK TIMESTAMP
                currentPlace.setRealAmountOfUndefined(dataSnapshot.getChildrenCount());
                //TODO GET PLANNED
                currentPlace.setPlannedAmountOfUndefined(dataSnapshot.getChildrenCount() + 30);
                updateStatusChart(getView(), currentPlace);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        placeDetailsDataBase = new Firebase("https://boiling-fire-4188.firebaseio.com/pubs/" + address.replace("\n", " "));

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
                placeDetailsDataBase.setValue(currentPlace);
                updateUi(getView(), currentPlace);
            }
            else {
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
}
