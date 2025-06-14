package com.example.patronus;

import android.os.Environment;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class ThreatLogger {
    private static final String logPath = Environment.getExternalStorageDirectory() + "/vpn_threat_log.txt";

    public static void log(String message) {
        try {
            FileWriter writer = new FileWriter(logPath, true);
            writer.append(new Date().toString()).append(" - ").append(message).append("\n");
            writer.close();
        } catch (IOException e) {
            Log.e("ThreatLogger", "Failed to write log", e);
        }
    }
}
