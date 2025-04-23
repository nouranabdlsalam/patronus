package com.example.patronus;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.Executors;

public class HomeScreenActivity extends AppCompatActivity implements Wifi_preconnec.WifiScanListener {

    private Wifi_preconnec wifiScanner;
    private WifiManager wifiManager;

    private ListView wifiListView;
    private ArrayAdapter<String> adapter;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView ssidText, bssidText, encryptionText, ThreatLevel, threatStatusText, monitoringStatus;
    private ImageView threatStatusIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);


        // Initialize the Room database and the DAO
        IPDatabase db = Room.databaseBuilder(getApplicationContext(),
                IPDatabase.class, "ip_database").build();
        IPDao ipDao = db.ipDao();
        IPDBLoader.loadIPsIfNeeded(getApplicationContext(), ipDao);
        SharedPreferencesManager sp = new SharedPreferencesManager(getApplicationContext());
        Log.d("TAG", "onCreate: " + sp.isIPDBPopulated());


        Intent sysmonIntent = new Intent(this, SystemMonitoringService.class);
        startService(sysmonIntent);

        ssidText = findViewById(R.id.ssidText);
        bssidText = findViewById(R.id.bssidText);
        encryptionText = findViewById(R.id.encryptionText);
        threatStatusText = findViewById(R.id.threatStatusText);
        threatStatusIcon = findViewById(R.id.threatStatusIcon);
        monitoringStatus = findViewById(R.id.monitoringStatus);
        ThreatLevel = findViewById(R.id.ThreatLevel);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiScanner = new Wifi_preconnec(this, this); // Fix: this class now implements WifiScanListener

        LinearLayout networkCenterBlock = findViewById(R.id.networkCenterBlock);
        networkCenterBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this, Network_center.class);
                startActivity(intent);
            }
        });
        LinearLayout home_security_modes = findViewById(R.id.home_security_modes);
        home_security_modes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout nav_scan= findViewById(R.id.nav_scan);
        nav_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this, ScanScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_settings= findViewById(R.id.nav_settings);
        nav_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_help= findViewById(R.id.nav_help);
        nav_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this, Help.class);
                startActivity(intent);
            }
        });
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
         if (sharedPreferencesManager.isBalancedModeOn()){
             monitoringStatus.setText("Balanced Security");
         }
         else {
             if (sharedPreferencesManager.isMalwareMonitoringOn()){
                 monitoringStatus.setText("On");
             }
             else {
                 monitoringStatus.setText("Off");
             }
         }


        // registerReceiver(wifiScanner.getReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            performWifiScan();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        // Only unregister if receiver is registered
        try {
            if (wifiScanner.getReceiver() != null)
                unregisterReceiver(wifiScanner.getReceiver());
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performWifiScan();
            } else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void performWifiScan() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Please enable Wi-Fi to scan networks.", Toast.LENGTH_LONG).show();
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String ssid = wifiInfo.getSSID().replace("\"", "");
            String bssid = wifiInfo.getBSSID();
            int rssi = wifiInfo.getRssi();
            String capabilities = wifiScanner.getSecurityLevelFromCapabilities(ssid);

            ssidText.setText("" + ssid);
            bssidText.setText("" + bssid);
            encryptionText.setText("" + rssi + " dBm");
            ThreatLevel.setText("" + capabilities);

            wifiScanner.startScan();
        } else {
            Toast.makeText(this, "No Wi-Fi info available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWifiScanCompleted(List<String> wifiList) {
        for (String wifi : wifiList) {
            Log.d("ScanResult", wifi);
        }
    }
    private void showThreatNotification(String suggestion) {
        String channelId = "wifi_threat_channel";
        String channelName = "Wi-Fi Threat Notifications";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when unsafe Wi-Fi is detected.");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle("Wi-Fi Threat Detected")
                .setContentText(suggestion)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }


    public void onThreatDetected(String ssid, String bssid, String encryption, int score) {

            ssidText.setText("" + ssid);
            bssidText.setText("" + bssid);
            encryptionText.setText("" + encryption);

            String threatLevel;
            String suggestion;


            if (score >= 7) {
                threatLevel = "⚠️ High Risk";
                threatStatusIcon.setImageResource(R.drawable.with_threats_warning);
                threatStatusText.setText("Wi-Fi threat detected!");
                suggestion = "Use a trusted VPN, avoid entering sensitive data, and consider switching networks.";
            } else if (score >= 4) {
                threatLevel = "⚠️ Moderate Risk";
                threatStatusText.setText("Wi-Fi threat detected!");
                threatStatusIcon.setImageResource(R.drawable.with_threats_warning);
                suggestion = "Caution advised. Do not use apps requiring passwords or personal info.";
            } else {
                threatLevel = "✅ Low Risk";
                threatStatusText.setText("No Wi-Fi threats detected.");
                threatStatusIcon.setImageResource(R.drawable.without_threats);
                suggestion = "Network appears safe.";
            }
        ThreatLevel.setText(threatLevel);
        showThreatNotification(suggestion);

    }


}