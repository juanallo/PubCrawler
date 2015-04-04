package com.jalloro.android.pubcrawler.model;


public class Place {

    private final SimplifiedLocation location;
    private final String address;
    private String name;
    private PriceRange priceRange;
    private long realAmountOfMen;
    private long realAmountOfUndefined;
    private long realAmountOfWomen;
    private long plannedAmountOfMen;
    private long plannedAmountOfUndefined;
    private long plannedAmountOfWomen;

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

    public long getRealAmountOfMen() {
        return realAmountOfMen;
    }

    public void setRealAmountOfMen(long realAmountOfMen) {
        this.realAmountOfMen = realAmountOfMen;
    }

    public long getRealAmountOfWomen() {
        return realAmountOfWomen;
    }

    public void setRealAmountOfWomen(long realAmountOfWomen) {
        this.realAmountOfWomen = realAmountOfWomen;
    }

    public long getPlannedAmountOfMen() {
        return plannedAmountOfMen;
    }

    public void setPlannedAmountOfMen(long plannedAmountOfMen) {
        this.plannedAmountOfMen = plannedAmountOfMen;
    }

    public long getPlannedAmountOfWomen() {
        return plannedAmountOfWomen;
    }

    public void setPlannedAmountOfWomen(long plannedAmountOfWomen) {
        this.plannedAmountOfWomen = plannedAmountOfWomen;
    }

    public long getRealAmountOfUndefined() {
        return realAmountOfUndefined;
    }

    public void setRealAmountOfUndefined(long realAmountOfUndefined) {
        this.realAmountOfUndefined = realAmountOfUndefined;
    }

    public long getPlannedAmountOfUndefined() {
        return plannedAmountOfUndefined;
    }

    public void setPlannedAmountOfUndefined(long plannedAmountOfUndefined) {
        this.plannedAmountOfUndefined = plannedAmountOfUndefined;
    }

    public void setPriceRange(PriceRange priceRange) {
        this.priceRange = priceRange;
    }
}
