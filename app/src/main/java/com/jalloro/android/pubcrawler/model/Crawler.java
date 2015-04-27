package com.jalloro.android.pubcrawler.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.jalloro.android.pubcrawler.helpers.PlayServicesHelper;
import com.jalloro.android.pubcrawler.helpers.SessionHelper;

public class Crawler implements Parcelable {

    private final String userId;
    private SimplifiedLocation lastLocation;
    private String lastAddress;
    private long checkInTimeStamp;
    private String facebookId;
    private Gender gender;


    public Crawler(@NonNull final String userId, @NonNull final Gender gender){
        this.userId = userId;
        this.gender = gender;
    }

    public Crawler(@NonNull final String userId,
                   @NonNull final SimplifiedLocation lastLocation,
                   @NonNull final String lastLocationAddress,
                   final long checkInTimeStamp,
                   @NonNull final String facebookId,
                   @NonNull final Gender gender) {
        this.userId = userId;
        this.lastLocation = lastLocation;
        this.lastAddress = lastLocationAddress;
        this.checkInTimeStamp = checkInTimeStamp;
        this.facebookId = facebookId;
        this.gender = gender;
    }

    public Crawler(Parcel in) {
       this.lastLocation = in.readParcelable(SimplifiedLocation.class.getClassLoader());
        userId = in.readString();
        lastAddress = in.readString();
        checkInTimeStamp = in.readLong();
        facebookId = in.readString();
        gender = Gender.valueOf(in.readString());
    }

    public String getUserId() {
        return userId;
    }

    public SimplifiedLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(SimplifiedLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getLastAddress() {
        return lastAddress;
    }

    public void setLastAddress(String lastAddress) {
        this.lastAddress = lastAddress;
    }

    public long getCheckInTimeStamp() {
        return checkInTimeStamp;
    }

    public void setCheckInTimeStamp(long checkInTimeStamp) {
        this.checkInTimeStamp = checkInTimeStamp;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public boolean isCheckedIn(@NonNull final Location location){
        final SimplifiedLocation to = new SimplifiedLocation(location.getLatitude(), location.getLongitude());
        return lastLocation != null && PlayServicesHelper.distance(lastLocation, to) < GEO_DISTANCE && SessionHelper.isInSession(checkInTimeStamp);
    }

    public void checkIn(@NonNull final SimplifiedLocation location, @NonNull final String address){
        setLastLocation(location);
        setLastAddress(address);
        setCheckInTimeStamp(System.currentTimeMillis());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.lastLocation,flags);
        dest.writeString(this.userId);
        dest.writeString(lastAddress);
        dest.writeLong(checkInTimeStamp);
        dest.writeString(facebookId);
        dest.writeString(gender.name());
    }

    public enum Gender {
       MALE,
       FEMALE,
       UNDEFINED
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Crawler createFromParcel(Parcel in) {
            return new Crawler(in);
        }

        public Crawler[] newArray(int size) {
            return new Crawler[size];
        }
    };

    public static final double GEO_DISTANCE = 0.012;

}
