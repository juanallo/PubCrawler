package com.jalloro.android.pubcrawler.data;

import android.content.res.Resources;

import com.jalloro.android.pubcrawler.R;
import com.jalloro.android.pubcrawler.model.Crawler;

public class FirebaseContract {

    public static final String CRAWLERS = "crawlers";
    public static final String LAST_ADDRESS = "lastAddress";
    public static final String PUBS = "pubs";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String LAST_LOCATION = "lastLocation";
    public static final String URL_SEPARATOR = "/";

    public static String getPubUrl(Resources resources, String currentAddress){
       return getBaseUrl(resources) + URL_SEPARATOR + PUBS + URL_SEPARATOR + currentAddress.replace("\n", "");
   }

   public static String getBaseUrl(Resources resources){
       return resources.getString(R.string.firebase_base_url);
   }

    public static String getCurrentCrawlerUrl(Resources resources, Crawler crawler){
        return getBaseUrl(resources) + URL_SEPARATOR + CRAWLERS + URL_SEPARATOR + crawler.getUserId();
    }
}
