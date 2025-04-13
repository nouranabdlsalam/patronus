package com.example.patronus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public class PackageInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Uri data = intent.getData();
        if (data == null) return;

        String packageName = data.getSchemeSpecificPart();
        if (packageName == null) return;

        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            Log.d("SecurityScan", "New app installed: " + packageName);
            Log.d("AnomalyDetection", "Anomalous app detected: " + packageName + " | Reason: New install");
            Log.d("MalwareScanner", "Scanning app for malware: " + packageName);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            Log.d("SecurityScan", "App updated: " + packageName);
            Log.d("AnomalyDetection", "App update detected: " + packageName + " | Reason: App updated");
            Log.d("MalwareScanner", "Re-scanning updated app for malware: " + packageName);
        }

        AppScanner scanner = new AppScanner(context);
        int classification = -1;
        try {
            classification = scanner.scanApp(packageName);
            if (classification==1){
                Log.d("BGSCAN", "App " + packageName + " is Malicious");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
