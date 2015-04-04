package com.jalloro.android.pubcrawler.chart;

public class Bar {

    private final String label;
    private final long value;
    private final long colorId;

    public Bar(String label, long value, long colorId) {
        this.label = label;
        this.value = value;
        this.colorId = colorId;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }

    public long getColorId() {
        return colorId;
    }
}
