package com.jalloro.android.pubcrawler.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SimplifiedLocation implements Parcelable {
    private double longitude;
    private double latitude;

    public SimplifiedLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SimplifiedLocation(Parcel in) {
        String[] data = new String[2];

        in.readStringArray(data);
        this.longitude = Double.parseDouble(data[0]);
        this.latitude = Double.parseDouble(data[1]);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final String[] val = {
                Double.toString(this.longitude),
                Double.toString(this.latitude)
        };
        dest.writeArray(val);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SimplifiedLocation createFromParcel(Parcel in) {
            return new SimplifiedLocation(in);
        }

        public SimplifiedLocation[] newArray(int size) {
            return new SimplifiedLocation[size];
        }
    };
}
