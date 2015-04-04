package com.jalloro.android.pubcrawler.model;


public class Place {

    private final SimplifiedLocation location;
    private final String address;
    private String name;
    private PriceRange priceRange;
    private int realAmountOfMen;
    private int realAmountOfWomen;
    private int plannedAmountOfMen;
    private int plannedAmountOfWomen;

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

    public int getRealAmountOfMen() {
        return realAmountOfMen;
    }

    public void setRealAmountOfMen(int realAmountOfMen) {
        this.realAmountOfMen = realAmountOfMen;
    }

    public int getRealAmountOfWomen() {
        return realAmountOfWomen;
    }

    public void setRealAmountOfWomen(int realAmountOfWomen) {
        this.realAmountOfWomen = realAmountOfWomen;
    }

    public int getPlannedAmountOfMen() {
        return plannedAmountOfMen;
    }

    public void setPlannedAmountOfMen(int plannedAmountOfMen) {
        this.plannedAmountOfMen = plannedAmountOfMen;
    }

    public int getPlannedAmountOfWomen() {
        return plannedAmountOfWomen;
    }

    public void setPlannedAmountOfWomen(int plannedAmountOfWomen) {
        this.plannedAmountOfWomen = plannedAmountOfWomen;
    }

    public enum PriceRange{
        $(1, "$"),
        $$(2, "$$"),
        $$$(3, "$$$"),
        $$$$(4, "$$$$"),
        UNKNOWN(5, "??");

        private final String label;
        private int value;

        PriceRange(int value, String label) {
           this.value = value;
           this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static PriceRange fromValue(int value){
            PriceRange resolvedPrice = UNKNOWN;
            for (PriceRange p : PriceRange.values()){
               if(p.value == value){
                   resolvedPrice = p;
                   break;
               }
            }
            return resolvedPrice;
        }
    }
}
