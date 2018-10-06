package com.teamcity.enums;

public enum TCMetric {
    Total_Runs("Total runs"),
    Pass_Percentage("Pass %"),
    ;

    private final String header;


    TCMetric(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
