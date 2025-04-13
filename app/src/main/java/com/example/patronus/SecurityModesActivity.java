package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Switch;
import android.widget.Toast;

public class SecurityModesActivity extends AppCompatActivity {
    private Switch autoModeSwitch, highSecuritySwitch, balancedSecuritySwitch, lowSecuritySwitch;
    private Handler handler = new Handler();
    private Runnable highSecurityRunnable, balancedSecurityRunnable;
    private boolean isMonitoring = false;
    private final int BALANCED_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_mode);

        // Initialize switches
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        highSecuritySwitch = findViewById(R.id.highSecuritySwitch);
        balancedSecuritySwitch = findViewById(R.id.BalancedSecuritySwitch);
        lowSecuritySwitch = findViewById(R.id.LowSecuritySwitch);

        // Setup listeners
        autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(autoModeSwitch);
                startAutoMode();
            } else {
                stopAllMonitoring();
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

        balancedSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(balancedSecuritySwitch);
                startBalancedSecurityMode();
            } else {
                stopAllMonitoring();
            }
        });

        lowSecuritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                disableOtherSwitchesExcept(lowSecuritySwitch);
                stopAllMonitoring();
                showToast("Low Security Mode: Monitoring disabled");
            }
        });

        // Define monitoring tasks
        highSecurityRunnable = new Runnable() {
            @Override
            public void run() {
                simulateScan("High Security Mode: Scanning in real-time...");
                handler.postDelayed(this, 5000); // every 5 seconds
            }
        };

        balancedSecurityRunnable = new Runnable() {
            @Override
            public void run() {
                simulateScan("Balanced Security Mode: Scheduled scan");
                handler.postDelayed(this, BALANCED_INTERVAL_MS);
            }
        };
    }

    private void disableOtherSwitchesExcept(Switch activeSwitch) {
        if (activeSwitch != autoModeSwitch) autoModeSwitch.setChecked(false);
        if (activeSwitch != highSecuritySwitch) highSecuritySwitch.setChecked(false);
        if (activeSwitch != balancedSecuritySwitch) balancedSecuritySwitch.setChecked(false);
        if (activeSwitch != lowSecuritySwitch) lowSecuritySwitch.setChecked(false);
    }

    private void startHighSecurityMode() {
        stopAllMonitoring();
        showToast("High Security Mode Activated");
        isMonitoring = true;
        handler.post(highSecurityRunnable);
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);

    }

    private void startBalancedSecurityMode() {
        stopAllMonitoring();
        showToast("Balanced Security Mode Activated");
        isMonitoring = true;
        handler.post(balancedSecurityRunnable);
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);
    }

    private void startAutoMode() {
        stopAllMonitoring();
        showToast("Auto Mode: Choosing security level...");
        if (isPowerSaveMode()) {
            showToast("Power Save Mode detected: Switching to Low Security Mode");
            lowSecuritySwitch.setChecked(true);
            return;
        }

        if (isDeviceCharging()) {
            showToast("Charging: High Security Mode Enabled");
            highSecuritySwitch.setChecked(true);
            return;
        }
        // Simulate auto selection logic
        int batteryLevel = getBatteryLevel();
        if (batteryLevel > 50) {
            startHighSecurityMode();
        } else {
            lowSecuritySwitch.setChecked(true);
        }
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);

    }

    private void stopAllMonitoring() {
        isMonitoring = false;
        handler.removeCallbacks(highSecurityRunnable);
        handler.removeCallbacks(balancedSecurityRunnable);
        showToast("Monitoring stopped");
    }

    private void simulateScan(String message) {
        if (isMonitoring) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
// hena n7ot el mointoring  logic
        }   }

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

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
