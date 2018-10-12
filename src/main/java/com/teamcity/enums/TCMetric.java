package com.teamcity.enums;

public enum TCMetric {
    Total_Runs("Total runs", false),
    Pass_Percentage("Pass %", true),
    Stability_Percentage("Stability", true),
    ;

    private final String header;
    private final boolean isNumeric;


    TCMetric(String header, boolean isNumeric) {
        this.header = header;
        this.isNumeric = isNumeric;
    }

    public String getHeader() {
        return header;
    }

    public boolean isNumeric() {
        return isNumeric;
    }
}
