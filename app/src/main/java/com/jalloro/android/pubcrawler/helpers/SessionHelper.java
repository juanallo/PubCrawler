package com.jalloro.android.pubcrawler.helpers;

import java.sql.Timestamp;

public class SessionHelper {


    /**
     * compares a timeStamp against now.
     * @param timeStamp
     * @return
     */
    public static long getTimeStampDiff(long timeStamp, long now){
        Timestamp d1 = new Timestamp(now);
        Timestamp d2 = new Timestamp(timeStamp);
        return Math.abs(d1.getTime() - d2.getTime());
    }

    /**
     * true if the timeStamp is inside a range between now and now - sessionTime.
     * @param timeStamp timeStampToCheck
     * @param sessionTime period of a particular session.
     * @return
     */
    public static boolean isInSession(long timeStamp, long sessionTime){
        final long diff = getTimeStampDiff(timeStamp, System.currentTimeMillis());
        return diff < sessionTime;
    }

    /**
     * true if the timeStamp is inside a range between now and now - DEFAULT_SESSION_SIZE_MILIS
     * @param timestamp timeStampToCheck
     * @return
     */
    public static boolean isInSession(long timestamp){
        return isInSession(timestamp, DEFAULT_SESSION_SIZE_MILIS);
    }

    //default session size is 8 hours in millis.
    //((1 second * 60 seconds) * 60 min) * 8 hours
    public static final long DEFAULT_SESSION_SIZE_MILIS = 1000 * 60 * 60 * 8;
}

