package com.example.albert.mediaunderstanding;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Albert Mwanjesa 6/12/2016
 * This HttpRequestHelper requests data from the given API.
 * The data is processed further by the Asynctask.
 */
public class HttpRequestHelper {

    protected static synchronized String downloadFromServer(String urlString) {
        final String basicAuth = "Basic " + Base64.encodeToString("root:c199c52b8209ad0980fd861072fe8fa5".getBytes(), Base64.NO_WRAP);
        // declare return string result
        String result = "";
        // turn string into url
        Log.d("urlString", urlString);

        URL url;
        try {
            url = new URL(urlString);
        } catch (java.net.MalformedURLException e) {
            url = null;
            e.printStackTrace();
        }
        // make the connection
        HttpURLConnection connection;
        if (url != null) {
            try {
                // Open connection, set request method
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.addRequestProperty("Authorization", basicAuth);

                int responseCode = connection.getResponseCode();
                Log.d("response", Integer.toString(responseCode));
                if (200 <= responseCode && responseCode <= 299) {
                    if (responseCode == HttpURLConnection.HTTP_OK){
                        Log.d("success", "File uploaded!");
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        result = result + line;
                    }

                    Log.d("resulting tag", result);
                }
                else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    // communicate correct error
                }
            }catch (java.io.IOException e){
                e.printStackTrace();
            }

            return result;

        }
        return result;
    }

}
