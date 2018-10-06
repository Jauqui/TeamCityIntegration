package com.teamcity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;


public class RESTInvoker {
    private final String baseUrl;
    private List<String> cookies;


    public RESTInvoker(String domain, String authToken) {
        this.baseUrl = domain;

        try {
            this.cookies = getToken(domain, authToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataFromServer(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(baseUrl + path);
            URLConnection urlConnection = getURLConnection(url);
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

    private URLConnection getURLConnection(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        for (String cookie : cookies)
            urlConnection.addRequestProperty("Cookie", cookie);

        return urlConnection;
    }

    private List<String> getToken(String domain, String authToken) throws IOException {
        URL url = new URL(domain + "/app/rest");
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", authToken);
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            return headerFields.get("Set-Cookie");
        }
        return null;
    }
}