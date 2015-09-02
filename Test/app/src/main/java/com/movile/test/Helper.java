package com.movile.test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by jgutierrez on 9/1/15.
 */
public class Helper {
    private static int MAX_DIGITS = 3;

    public static String trimString(String str) {
        if (str.length() >= MAX_DIGITS)
            return str.substring(0, MAX_DIGITS);
        else
            return str;
    }

    public static boolean hasInternetAccess(Context context) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        return connected;
    }
}
