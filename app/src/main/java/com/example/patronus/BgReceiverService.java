package com.example.patronus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class BgReceiverService extends Service {
    WifiConAttemptReceiver wifiConAttemptReceiver = new WifiConAttemptReceiver();
    PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver();
    Handler handler;
    Runnable periodicNetworkTask;
    SharedPreferencesManager sharedPreferencesManager;
    boolean isWifiReceiverRegistered = false;
    boolean isPackageReceiverRegistered = false;
    boolean isPreconRunning, isMalwareMonRunning, isNetMonRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(this, "Registered Receivers", Toast.LENGTH_SHORT).show();

        handler = new Handler(Looper.getMainLooper());
        periodicNetworkTask = new Runnable() {
            @Override
            public void run() {
                Log.d("AppMonitorService", "Starting network scan");

                Intent intent = new Intent(BgReceiverService.this, NetworkMonitorService.class);
                startService(intent); // it will stop itself after 30 sec

                // Schedule next run
                handler.postDelayed(this, 15 * 60 * 1000); // 15 minutes
            }
        };

        sharedPreferencesManager = new SharedPreferencesManager(this);
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
                .setSmallIcon(R.mipmap.logo)
                .build();
        startForeground(1, notification); // Start the service in foreground mode

        if (sharedPreferencesManager.isPreconnectionScanningOn()){
            if (!isPreconRunning){
                turnPreconnectionScanningOn();
                isPreconRunning = true;
            }
        }
        else {
            if (isPreconRunning){
                turnPreconnectionScanningOff();
                isPreconRunning = false;
            }
        }

        if (sharedPreferencesManager.isMalwareMonitoringOn()){
            if (!isMalwareMonRunning){
                turnMalwareMonitoringOn();
                isMalwareMonRunning = true;
            }
        }
        else {
            if (isMalwareMonRunning){
                turnMalwareMonitoringOff();
                isMalwareMonRunning = false;
            }
        }

        if (sharedPreferencesManager.isNetworkMonitoringOn()){
            if (!isNetMonRunning){
                turnNetworkMonitoringOn();
                isNetMonRunning = true;
            }
        }
        else {
            if (isNetMonRunning){
                turnNetworkMonitoringOff();
                isNetMonRunning = false;
            }
        }

        if (!sharedPreferencesManager.isPreconnectionScanningOn() &&
            !sharedPreferencesManager.isMalwareMonitoringOn() &&
            !sharedPreferencesManager.isNetworkMonitoringOn()){
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        turnPreconnectionScanningOff();
        turnMalwareMonitoringOff();
        turnNetworkMonitoringOff();
    }

    public void turnPreconnectionScanningOn(){
        if (!isWifiReceiverRegistered){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

            registerReceiver(wifiConAttemptReceiver, intentFilter);
            isWifiReceiverRegistered = true;
            Log.d("SETTINGS", "Preconnection Scanning is on");
        }
    }

    public void turnMalwareMonitoringOn(){
        if (!isPackageReceiverRegistered){
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            filter.addDataScheme("package");
            registerReceiver(packageInstallReceiver, filter);
            isPackageReceiverRegistered = true;
            Log.d("SETTINGS", "Malware Monitoring is on");
        }
    }

    public void turnNetworkMonitoringOn(){
//        handler.removeCallbacks(periodicNetworkTask);
        handler.post(periodicNetworkTask);
        Log.d("SETTINGS", "Network Monitoring is on");
    }

    public void turnPreconnectionScanningOff(){
        if (isWifiReceiverRegistered){
            if (wifiConAttemptReceiver != null) {
                unregisterReceiver(wifiConAttemptReceiver);
                isWifiReceiverRegistered = false;
                Toast.makeText(this, "Unregistered Receiver", Toast.LENGTH_SHORT).show();
                Log.d("SETTINGS", "Preconnection Scanning is Off");
            }
        }
    }

    public void turnMalwareMonitoringOff(){
        if (isPackageReceiverRegistered){
            if (packageInstallReceiver != null) {
                unregisterReceiver(packageInstallReceiver);
                isPackageReceiverRegistered = false;
                Toast.makeText(this, "Unregistered Receiver", Toast.LENGTH_SHORT).show();
                Log.d("SETTINGS", "Malware Monitoring is off");
            }
        }
    }

    public void turnNetworkMonitoringOff(){
        handler.removeCallbacks(periodicNetworkTask);
        Log.d("SETTINGS", "Network Monitoring is off");
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
