package com.jalloro.android.pubcrawler.model;

public enum PriceRange{
    $(1, "$"),
    $$(2, "$$"),
    $$$(3, "$$$"),
    $$$$(4, "$$$$"),
    UNKNOWN(5, "");

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