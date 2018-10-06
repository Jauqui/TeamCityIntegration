package com.teamcity;

public abstract class TeamCityAPI {
    public static final String baseUrl = "https://teamcity.sapphirepri.com/";

    protected static String getAuthToken() {
        return System.getenv().get("TeamCity_AuthToken");
    }
}
