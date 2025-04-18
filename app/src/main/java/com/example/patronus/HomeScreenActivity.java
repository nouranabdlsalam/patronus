package com.example.patronus;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeScreenActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView ssidText, bssidText, encryptionText, securityText, threatStatusText;
    private ImageView threatStatusIcon;

    private Wifi_preconnec wifiScanner;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        ssidText = findViewById(R.id.ssidText);
        bssidText = findViewById(R.id.bssidText);
        encryptionText = findViewById(R.id.encryptionText);
        securityText = findViewById(R.id.securityText);
        threatStatusText = findViewById(R.id.threatStatusText);
        threatStatusIcon = findViewById(R.id.threatStatusIcon);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiScanner = new Wifi_preconnec(this, null); // ListView not needed

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
    protected void onResume() {
        super.onResume();
        registerReceiver(new Wifi_preconnec.WifiConnectionReceiver(),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(new Wifi_preconnec.WifiConnectionReceiver());
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
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getSSID() != null) {
            String ssid = wifiInfo.getSSID().replace("\"", "");
            String bssid = wifiInfo.getBSSID();
            int rssi = wifiInfo.getRssi();
            String capabilities = wifiScanner.getSecurityLevelFromCapabilities(wifiInfo.getSSID());

            ssidText.setText("SSID: " + ssid);
            bssidText.setText("BSSID: " + bssid);
            encryptionText.setText("RSSI: " + rssi + " dBm");
            securityText.setText("Encryption: " + capabilities);

            wifiScanner.startScan();
        } else {
            Toast.makeText(this, "No Wi-Fi info available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Log.i("VPN", "User accepted VPN permission");
            startService(new Intent(this, SecureVpnService.class));
        } else {
            Log.w("VPN", "VPN permission denied");
        }
    }
}
