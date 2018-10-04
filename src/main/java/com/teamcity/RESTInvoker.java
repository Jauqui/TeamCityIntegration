package com.teamcity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class RESTInvoker {
    private final String baseUrl;
    private final String authToken;


    public RESTInvoker(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    public String getDataFromServer(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(baseUrl + path);
            URLConnection urlConnection = getURLConnection(url, authToken);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URLConnection getURLConnection(URL url, String authToken) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", authToken);

        return urlConnection;
    }
}