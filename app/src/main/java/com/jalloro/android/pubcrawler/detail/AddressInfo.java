package com.jalloro.android.pubcrawler.detail;

import android.os.Parcel;
import android.os.Parcelable;

import com.jalloro.android.pubcrawler.model.PriceRange;

public class AddressInfo implements Parcelable {

    private final String address;
    final String name;
    final PriceRange priceRange;

    public AddressInfo(String address, String name, PriceRange priceRange) {
        this.address = address;
        this.name = name;
        this.priceRange = priceRange;
    }

    public AddressInfo(Parcel in) {
        String[] data = new String[3];
        in.readStringArray(data);
        this.address = data[0];
        this.name = data[1];
        this.priceRange = PriceRange.valueOf(data[2]);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(new String[]{
                this.address,
                this.name,
                this.priceRange.name()
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
}
