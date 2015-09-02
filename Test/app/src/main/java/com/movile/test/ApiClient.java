package com.movile.test;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by jgutierrez on 9/1/15.
 */
public class ApiClient {
    private static DefaultHttpClient httpClient = new DefaultHttpClient();
    private static final String TAG = "FETCHING_INFO";
    private static String rating;

    public static JSONArray getEpisodes() {
        InputStream inputStream = null;
        JSONArray jsonArray = null;
        String jsonString = "";
        httpClient = new DefaultHttpClient();

        try {
            HttpGet httpGet = new HttpGet(Constants.urlRequest);
            httpGet.setHeader(Constants.content_type, Constants.content_typeValue);
            httpGet.setHeader(Constants.apiKey, Constants.apiKeyValue);
            httpGet.setHeader(Constants.apiVersion, Constants.apiVersionValue);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }

            inputStream.close();
            jsonString = sb.toString();


        } catch (Exception e) {
            Log.e(TAG, "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }
        return jsonArray;
    }

    public static String getRating() {
        InputStream inputStream = null;
        JSONObject jsonObject = null;
        String jsonString = "";
        httpClient = new DefaultHttpClient();

        try {
            HttpGet httpGet = new HttpGet(Constants.ratingsRequest);
            httpGet.setHeader(Constants.content_type, Constants.content_typeValue);
            httpGet.setHeader(Constants.apiKey, Constants.apiKeyValue);
            httpGet.setHeader(Constants.apiVersion, Constants.apiVersionValue);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }

            inputStream.close();
            jsonString = sb.toString();


        } catch (Exception e) {
            Log.e(TAG, "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jsonObject = new JSONObject(jsonString);
            rating = jsonObject.get("rating").toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }

        return Helper.trimString(rating);
    }
}
