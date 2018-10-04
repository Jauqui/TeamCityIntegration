package com.teamcity;

public class TeamCityAPI {
    public static final String authToken = System.getenv().get("TeamCity_AuthToken");
    public static final String baseUrl = "https://teamcity.sapphirepri.com/";
}
