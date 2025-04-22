package com.example.patronus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class SystemMonitoringService extends Service {
    PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver();
    Handler handler;
    Runnable periodicNetworkTask, periodicBalancedMode;
    SharedPreferencesManager sharedPreferencesManager;
    boolean isPackageReceiverRegistered = false;
    boolean isAutoModeRunning, isMalwareMonRunning, isNetMonRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());
        periodicNetworkTask = new Runnable() {
            @Override
            public void run() {
                Log.d("AppMonitorService", "Starting network scan");

                Intent intent = new Intent(SystemMonitoringService.this, NetworkMonitorService.class);
                startService(intent); // it will stop itself after 30 sec

                // Schedule next run
                handler.postDelayed(this, 15 * 60 * 1000); // 15 minutes
            }
        };

        periodicBalancedMode = new Runnable() {
            @Override
            public void run() {
                int batteryLevel = getBatteryLevel();

                if (isPowerSaveMode()) {
                    turnMalwareMonitoringOff();
//                    Toast.makeText(SystemMonitoringService.this, "Power Save Mode detected", Toast.LENGTH_SHORT).show();
                } else if (isDeviceCharging()) {
                    turnMalwareMonitoringOn();
//                    Toast.makeText(SystemMonitoringService.this, "Charging: High Security Mode", Toast.LENGTH_SHORT).show();
                } else if (isDeviceOverheating() && !isDeviceCharging() && isDeviceIdle()) {
                    turnMalwareMonitoringOn();
//                    Toast.makeText(SystemMonitoringService.this, "Overheating while idle: High Security", Toast.LENGTH_SHORT).show();
                } else if (isDeviceIdle() && isCpuLoadHigh()) {
                    turnMalwareMonitoringOn();
//                    Toast.makeText(SystemMonitoringService.this, "Idle + High CPU: High Security", Toast.LENGTH_SHORT).show();
                } else if (isCpuLoadHigh() && !isDeviceIdle()) {
                    turnMalwareMonitoringOff();
//                    Toast.makeText(SystemMonitoringService.this, "Active + High CPU: Low Security (Gaming Mode)", Toast.LENGTH_SHORT).show();
                } else if (isDeviceIdle()) {
                    turnMalwareMonitoringOn();
//                    Toast.makeText(SystemMonitoringService.this, "Idle: High Security Mode", Toast.LENGTH_SHORT).show();
                } else if (batteryLevel > 50) {
                    turnMalwareMonitoringOn();
//                    Toast.makeText(SystemMonitoringService.this, "Battery > 50%: High Security Mode", Toast.LENGTH_SHORT).show();
                } else {
                    turnMalwareMonitoringOff();
//                    Toast.makeText(SystemMonitoringService.this, "Battery <= 50%: Low Security Mode", Toast.LENGTH_SHORT).show();
                }
                handler.postDelayed(this, 10000); // Re-evaluate every 10 seconds
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

        if (sharedPreferencesManager.isBalancedModeOn()){
            if (!isAutoModeRunning){
                turnAutoModeOn();
                isAutoModeRunning = true;
            }
        }
        else {
            if (isAutoModeRunning){
                turnAutoModeOff();
                isAutoModeRunning = false;
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

        if (!sharedPreferencesManager.isBalancedModeOn() &&
            !sharedPreferencesManager.isMalwareMonitoringOn() &&
            !sharedPreferencesManager.isNetworkMonitoringOn()){
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        turnMalwareMonitoringOff();
        turnNetworkMonitoringOff();
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
        handler.post(periodicNetworkTask);
        Log.d("SETTINGS", "Network Monitoring is on");
    }

    public void turnAutoModeOn(){
        handler.post(periodicBalancedMode);
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

    public void turnAutoModeOff(){
        handler.removeCallbacks(periodicBalancedMode);
    }

    private int getBatteryLevel() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return -1; // Unsupported on very old devices
    }

    private boolean isPowerSaveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            return pm.isPowerSaveMode();
        }
        return false;
    }

    private boolean isDeviceCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    private boolean isDeviceIdle() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isDeviceIdleMode();
        }
        return false;
    }

    private boolean isCpuLoadHigh() {
        try {
            // Read CPU stats from /proc/stat
            java.io.RandomAccessFile reader = new java.io.RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" +"); // Regex for multiple spaces
            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);

            Thread.sleep(360); // Wait for 360ms before second read

            reader = new java.io.RandomAccessFile("/proc/stat", "r");
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");
            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);

            float cpuUsage = (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
            return cpuUsage > 0.7f; // Above 70% usage
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isDeviceOverheating() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int temp = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) : 0;
        float temperature = temp / 10f; // Temperature in °C
        return temperature >= 40.0f;
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
