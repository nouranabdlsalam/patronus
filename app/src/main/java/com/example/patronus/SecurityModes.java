package com.example.patronus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecurityModes extends AppCompatActivity {

    private Switch autoModeSwitch, highSecuritySwitch, lowSecuritySwitch;
    private Handler handler = new Handler();
    private Runnable highSecurityRunnable;
    private boolean isAutoModeEnabled = false;
    private String lastMode = ""; // "HIGH", "LOW", "NONE"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_mode); // Your layout file name

        // Initialize switches
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        highSecuritySwitch = findViewById(R.id.highSecuritySwitch);
        lowSecuritySwitch = findViewById(R.id.LowSecuritySwitch);

        // Setup listeners
        autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAutoModeEnabled = isChecked;
            if (isChecked) {
                disableOtherSwitchesExcept(autoModeSwitch);
                startAutoMode();
            } else {
                stopAllMonitoring();
                lastMode = "";
            }
        });

        highSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(highSecuritySwitch);
                startHighSecurityMode();
            } else {
                stopAllMonitoring();
            }
        });

        lowSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(lowSecuritySwitch);
                stopAllMonitoring();
                stopMonitoringService();
                showToast("Low Security Mode: Monitoring disabled");
            }
        });

        highSecurityRunnable = new Runnable() {
            @Override
            public void run() {
                // simulateScan("High Security Mode: Scanning in real-time...");
                handler.postDelayed(this, 5000); // every 5 seconds
            }
        };
    }

    private void disableOtherSwitchesExcept(Switch activeSwitch) {
        if (activeSwitch != autoModeSwitch) autoModeSwitch.setChecked(false);
        if (activeSwitch != highSecuritySwitch) highSecuritySwitch.setChecked(false);
        if (activeSwitch != lowSecuritySwitch) lowSecuritySwitch.setChecked(false);
    }

    private void startHighSecurityMode() {
        stopAllMonitoring();  // Ensure all other monitoring is stopped before starting high security mode
        showToast("High Security Mode Activated");
        handler.post(highSecurityRunnable);  // Start continuous monitoring (simulate scanning)

        // Start the monitoring service to keep running in the background
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);
    }

    private void evaluateAutoMode() {
        if (!isAutoModeEnabled) return;

        String newMode = "LOW";  // Default
        int batteryLevel = getBatteryLevel();

        if (isPowerSaveMode()) {
            newMode = "LOW";
            showToast("Power Save Mode detected");
        } else if (isDeviceCharging()) {
            newMode = "HIGH";
            showToast("Charging: High Security Mode");
        } else if (isDeviceOverheating() && !isDeviceCharging() && isDeviceIdle()) {
            newMode = "HIGH";
            showToast("Overheating while idle: High Security");
        } else if (isDeviceIdle() && isCpuLoadHigh()) {
            newMode = "HIGH";
            showToast("Idle + High CPU: High Security");
        } else if (isCpuLoadHigh() && !isDeviceIdle()) {
            newMode = "LOW";
            showToast("Active + High CPU: Low Security (Gaming Mode)");
        } else if (isDeviceIdle()) {
            newMode = "HIGH";
            showToast("Idle: High Security Mode");
        } else if (batteryLevel > 50) {
            newMode = "HIGH";
            showToast("Battery > 50%: High Security Mode");
        } else {
            newMode = "LOW";
            showToast("Battery <= 50%: Low Security Mode");
        }

        // Only switch if mode changed
        if (!newMode.equals(lastMode)) {
            lastMode = newMode;

            // Temporarily remove listeners to prevent loops
            autoModeSwitch.setOnCheckedChangeListener(null);
            highSecuritySwitch.setOnCheckedChangeListener(null);
            lowSecuritySwitch.setOnCheckedChangeListener(null);

            if (newMode.equals("HIGH")) {
                highSecuritySwitch.setChecked(true);
                lowSecuritySwitch.setChecked(false);
            } else {
                lowSecuritySwitch.setChecked(true);
                highSecuritySwitch.setChecked(false);
            }

            // Restore listeners
            restoreSwitchListeners();
        } else {
            Log.d("AutoMode", "Mode unchanged: " + lastMode);
        }
    }

    private void startAutoMode() {
        stopAllMonitoring();
        showToast("Auto Mode: Monitoring...");

        // Define a new Runnable for delayed execution
        Runnable autoModeRunnable = new Runnable() {
            @Override
            public void run() {
                evaluateAutoMode();
                handler.postDelayed(this, 10000); // Re-evaluate every 10 seconds
            }
        };

        handler.postDelayed(autoModeRunnable, 10000); // Start the first evaluation after 10 seconds

        // Start the monitoring service to keep running in the background
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);
    }


    private void stopAllMonitoring() {
        handler.removeCallbacks(highSecurityRunnable);
        showToast("Monitoring stopped");
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

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void stopMonitoringService() {
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        stopService(serviceIntent);
    }

    private void restoreSwitchListeners() {
        autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAutoModeEnabled = isChecked;
            if (isChecked) {
                startAutoMode();
            } else {
                stopAllMonitoring();
                lastMode = "";
            }
        });

        highSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(highSecuritySwitch);
                startHighSecurityMode();
            } else {
                stopAllMonitoring();
            }
        });

        lowSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(lowSecuritySwitch);
                stopAllMonitoring();
                stopMonitoringService();
                showToast("Low Security Mode: Monitoring disabled");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllMonitoring();  // Ensure everything is cleaned up
    }
}
