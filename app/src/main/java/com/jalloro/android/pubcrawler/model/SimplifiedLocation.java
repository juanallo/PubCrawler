package com.jalloro.android.pubcrawler.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SimplifiedLocation implements Parcelable {
    private double latitude;
    private double longitude;

    public SimplifiedLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SimplifiedLocation(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
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
    public String toString() {
        return Double.toString(latitude) + "," + Double.toString(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
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
