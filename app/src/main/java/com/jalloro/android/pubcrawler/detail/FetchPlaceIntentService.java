package com.jalloro.android.pubcrawler.detail;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.jalloro.android.pubcrawler.model.PriceRange;
import com.jalloro.android.pubcrawler.welcome.FetchAddressIntentService;

public class FetchPlaceIntentService extends IntentService {

    private ResultReceiver resultReceiver;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public FetchPlaceIntentService() {
        super(FetchAddressIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //get address info from intent
        String address = intent.getStringExtra(Constants.ADDRESS_DATA_EXTRA);
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        //connect to foursquare and get info


        //send info to receiver.
        AddressInfo addressInfo = new AddressInfo(address,"La Colmena", PriceRange.$$);
        deliverResultToReceiver(Constants.SUCCESS_RESULT, addressInfo);
    }

    private void deliverResultToReceiver(int resultCode, AddressInfo addressInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY, addressInfo);
        resultReceiver.send(resultCode, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationplace";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String ADDRESS_DATA_EXTRA = PACKAGE_NAME +
                ".ADDRESS_DATA_EXTRA";
    }
}
