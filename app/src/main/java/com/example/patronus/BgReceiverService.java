package com.example.patronus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class BgReceiverService extends Service {
    WifiConAttemptReceiver wifiConAttemptReceiver = new WifiConAttemptReceiver();
    PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        registerReceiver(wifiConAttemptReceiver, intentFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");

        registerReceiver(packageInstallReceiver, filter);
        Toast.makeText(this, "Registered Receivers", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "WiFiMonitorChannel")
                .setContentTitle("System Monitoring is On")
                .setContentText("We've got your back.")
                .setOngoing(true)  // Makes it persistent (user can't swipe it away)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification); // Start the service in foreground mode
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wifiConAttemptReceiver != null) {
            unregisterReceiver(wifiConAttemptReceiver);
            Toast.makeText(this, "Unregistered Receiver", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "WiFiMonitorChannel",
                    "WiFi Monitoring",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notification channel for foreground service");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
