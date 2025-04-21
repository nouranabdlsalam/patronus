package com.example.patronus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpeedChecker {

    private Context context;


    public SpeedChecker(Context context) {
        this.context = context;
    }

    public void measureDownloadSpeed(String testUrl, SpeedTestCallback callback) {
        new AsyncTask<String, Void, Double>() {
            @Override
            protected Double doInBackground(String... params) {
                String url = params.length > 0 ? params[0] : "https://www.google.com";
                try {
                    long startTime = System.currentTimeMillis();
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;

                        if (System.currentTimeMillis() - startTime > 10000) {
                            break;
                        }
                    }

                    long endTime = System.currentTimeMillis();
                    double duration = (endTime - startTime) / 1000.0;

                    inputStream.close();
                    connection.disconnect();

                    double speedMbps = ((totalBytesRead * 8) / 1000000.0) / duration;
                    return Math.round(speedMbps * 100) / 100.0; // Round to 2 decimal places
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0.0;
                }
            }

            @Override
            protected void onPostExecute(Double speed) {
                if (callback != null) {
                    callback.onSpeedMeasured(speed);
                }
            }
        }.execute(testUrl);
    }

    public void measureUploadSpeed(String testUrl, SpeedTestCallback callback) {
        new AsyncTask<String, Void, Double>() {
            @Override
            protected Double doInBackground(String... params) {
                String url = params.length > 0 ? params[0] : "https://httpbin.org/post";
                try {
                    int dataSize = 1000000; // 1MB
                    byte[] data = new byte[dataSize];
                    for (int i = 0; i < dataSize; i++) {
                        data[i] = 0x61; // Fill with 'a'
                    }

                    long startTime = System.currentTimeMillis();
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.connect();

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
                    outputStream.close();

                    InputStream inputStream = connection.getInputStream();
                    byte[] buffer = new byte[8192];
                    while (inputStream.read(buffer) != -1) {
                    }

                    long endTime = System.currentTimeMillis();
                    double duration = (endTime - startTime) / 1000.0;

                    inputStream.close();
                    connection.disconnect();

                    double speedMbps = ((dataSize * 8) / 1000000.0) / duration;
                    return Math.round(speedMbps * 100) / 100.0; // Round to 2 decimal places
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0.0;
                }
            }

            @Override
            protected void onPostExecute(Double speed) {
                if (callback != null) {
                    callback.onSpeedMeasured(speed);
                }
            }
        }.execute(testUrl);
    }

    public String getNetworkQuality(double downloadSpeedMbps) {
        if (downloadSpeedMbps <= 0) {
            return "No Connection";
        } else if (downloadSpeedMbps < 1) {
            return "Poor";
        } else if (downloadSpeedMbps < 5) {
            return "Fair";
        } else if (downloadSpeedMbps < 10) {
            return "Good";
        } else if (downloadSpeedMbps < 30) {
            return "Very Good";
        } else {
            return "Excellent";
        }
    }

    public interface SpeedTestCallback {
        void onSpeedMeasured(Double speed);
    }
}
