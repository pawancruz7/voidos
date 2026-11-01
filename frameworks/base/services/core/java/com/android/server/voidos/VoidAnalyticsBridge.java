package com.android.server.voidos;

import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;

public class VoidAnalyticsBridge {
    private static final String TAG = "VoidOS_Metrics";
    private static final String COUNTER_URL = "https://api.voidos.org/metrics/active_ping";

    public static void performAnonymousBootPing() {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(COUNTER_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.setRequestProperty("X-VoidOS-Status", "ALIVE");

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "Device metrics registered anonymously.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Metrics server unreachable.");
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
}
