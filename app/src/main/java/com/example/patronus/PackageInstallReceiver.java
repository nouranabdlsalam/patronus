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
        Uri data = intent.getData();
        if (data == null) return;

        String packageName = data.getSchemeSpecificPart();
        if (packageName == null) return;

        // Log action for debugging (optional)
        Log.d("AppChangeReceiver", "App installed or updated: " + packageName);

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
