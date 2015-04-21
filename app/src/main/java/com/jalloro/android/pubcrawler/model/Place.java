package com.jalloro.android.pubcrawler.model;


public class Place {

    private final SimplifiedLocation location;
    private final String address;
    private String name;
    private PriceRange priceRange;
    private long now;
    private long historic;

    public Place(SimplifiedLocation location, String address) {
        this.location = location;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public void setPriceRangeFromValue(int value) {
        this.priceRange = PriceRange.fromValue(value);
    }

    public String getAddress() {
        return address;
    }

    public SimplifiedLocation getLocation() {
        return location;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public long getHistoric() {
        return historic;
    }

    public void setHistoric(long historic) {
        this.historic = historic;
    }

    public void setPriceRange(PriceRange priceRange) {
        this.priceRange = priceRange;
    }

    public void addCrawler(){
        this.now += 1;
    }
}
