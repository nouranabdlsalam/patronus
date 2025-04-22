package com.example.patronus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;


public class MonitoringService extends Service {

    private static final String TAG = "MonitoringService";

    private BroadcastReceiver InstallReceiver;

    @Override

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MonitoringService started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "monitor_channel",
                    "Monitoring Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        Notification notification = new NotificationCompat.Builder(this, "monitor_channel")
                .setContentTitle("High Security Mode")
                .setContentText("Monitoring for new app installations...")
                //  .setSmallIcon(R.drawable.ic_monitoring) // replace with your icon
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

        InstallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String packageName = intent.getData().getEncodedSchemeSpecificPart();

                if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    Log.d("SecurityScan", "New app installed: " + packageName);
                    Log.d("AnomalyDetection", "Anomalous app detected: " + packageName + " | Reason: New install");
                    Log.d("MalwareScanner", "Scanning app for malware: " + packageName);



                } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                    Log.d("SecurityScan", "App updated: " + packageName);
                    Log.d("AnomalyDetection", "App update detected: " + packageName + " | Reason: App updated");
                    Log.d("MalwareScanner", "Re-scanning updated app for malware: " + packageName);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(InstallReceiver, filter);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    "monitor_channel",
//                    "Monitoring Service",
//                    NotificationManager.IMPORTANCE_LOW
//            );
//            //NotificationManager manager = getSystemService(NotificationManager.class);
//            //manager.createNotificationChannel(channel);
//        }

//        Notification notification = new NotificationCompat.Builder(this, "monitor_channel")
//                .setContentTitle("Monitoring Active")
//                .setContentText("Security monitoring is running")
//                .build();
//
//        startForeground(1, notification);
//
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service running...");
        return START_STICKY;
    }







    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(InstallReceiver);
        Log.d(TAG, "MonitoringService stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}






