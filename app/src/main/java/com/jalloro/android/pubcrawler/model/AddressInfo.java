package com.jalloro.android.pubcrawler.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressInfo implements Parcelable {

    private final String address;
    private final String name;
    private final PriceRange priceRange;
    private final SimplifiedLocation location;
    private boolean verified;

    public AddressInfo(String address, String name, PriceRange priceRange, SimplifiedLocation location) {
        this.address = address;
        this.name = name;
        this.priceRange = priceRange;
        this.location = location;
    }

    public AddressInfo(Parcel in) {
        this.location = in.readParcelable(this.getClass().getClassLoader());

        String[] data = new String[4];
        in.readStringArray(data);
        this.address = data[0];
        this.name = data[1];
        this.priceRange = PriceRange.valueOf(data[2]);
        this.verified = Boolean.parseBoolean(data[3]);
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.location,flags);
        dest.writeArray(new String[]{
                this.address,
                this.name,
                this.priceRange.name(),
                Boolean.toString(this.verified)
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AddressInfo createFromParcel(Parcel in) {
            return new AddressInfo(in);
        }

        public AddressInfo[] newArray(int size) {
            return new AddressInfo[size];
        }
    };

    public SimplifiedLocation getLocation() {
        return location;
    }
}
