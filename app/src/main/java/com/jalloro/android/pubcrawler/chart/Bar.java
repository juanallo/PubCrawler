package com.jalloro.android.pubcrawler.chart;

public class Bar {

    private final String label;
    private final int value;
    private final int colorId;

    public Bar(String label, int value, int colorId) {
        this.label = label;
        this.value = value;
        this.colorId = colorId;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    public int getColorId() {
        return colorId;
    }
}
