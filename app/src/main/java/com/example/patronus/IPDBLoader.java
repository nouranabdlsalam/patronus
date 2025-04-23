package com.example.patronus;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.room.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class IPDBLoader {
    // Method to read and load IPs if not already populated
    public static void loadIPsIfNeeded(Context context, IPDao ipDao) {
        SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);

        if (!prefsManager.isIPDBPopulated()) {
            Log.d("DATABASE LOADING", "loadIPsIfNeeded: LOADING IPs ");
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AssetManager assetManager = context.getAssets();
                    InputStream inputStream = assetManager.open("malicious_ips.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            MaliciousIP ip = new MaliciousIP(line);
                            ipDao.insertIP(ip);
                            Log.d("DATABASE LOADING", "loadIPsIfNeeded: LOADED " + ip);
                        }
                    }
                    reader.close();
                    inputStream.close();

                    prefsManager.setIPDBPopulated(true);
                    Log.d("DATABASE LOADING", "loaded " + ipDao.getTotalIPsCount() + " IPs into the database");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

}
