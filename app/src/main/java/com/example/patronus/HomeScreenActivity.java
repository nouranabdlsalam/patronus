package com.example.patronus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class HomeScreenActivity extends AppCompatActivity implements Wifi_preconnec.WifiScanListener {

    private Wifi_preconnec wifiScanner;
    private WifiManager wifiManager;

    private ListView wifiListView;
    private ArrayAdapter<String> adapter;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView ssidText, bssidText, encryptionText, securityText, threatStatusText;
    private ImageView threatStatusIcon;
    private Button network_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // UI references
        ssidText = findViewById(R.id.ssidText);
        bssidText = findViewById(R.id.bssidText);
        encryptionText = findViewById(R.id.encryptionText);
        securityText = findViewById(R.id.securityText);
        threatStatusText = findViewById(R.id.threatStatusText);
        threatStatusIcon = findViewById(R.id.threatStatusIcon);
        network_btn = findViewById(R.id.networkCenter_btn);

        // Network center button click
        network_btn.setOnClickListener(v -> {
            Intent i = new Intent(HomeScreenActivity.this, Network_center.class);
            startActivity(i);
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiScanner = new Wifi_preconnec(this, this); // Fix: this class now implements WifiScanListener

        // Register receiver once (optional – can be removed if not used in your Wifi_preconnec)
        // registerReceiver(wifiScanner.getReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Permission check
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
    public void onWifiScanCompleted(List<String> wifiList) {
        for (String wifi : wifiList) {
            Log.d("ScanResult", wifi);
        }
    }

    public void onThreatDetected(String ssid, String bssid, String encryption, int score) {
        runOnUiThread(() -> {
            ssidText.setText("SSID: " + ssid);
            bssidText.setText("BSSID: " + bssid);
            encryptionText.setText("Encryption: " + encryption);

            String threatLevel;
            String suggestion;

            if (score >= 7) {
                threatLevel = "⚠️ High Risk";
                suggestion = "Use a trusted VPN, avoid entering sensitive data, and consider switching networks.";
                threatStatusText.setText("Threat Detected!");
                threatStatusIcon.setImageResource(R.drawable.with_threats_warning);
            } else if (score >= 4) {
                threatLevel = "⚠️ Moderate Risk";
                suggestion = "Caution advised. Do not use apps requiring passwords or personal info.";
                threatStatusText.setText("Potential Risk");
                threatStatusIcon.setImageResource(R.drawable.with_threats_warning);
            } else {
                threatLevel = "✅ Low Risk";
                suggestion = "Network appears safe.";
                threatStatusText.setText("Safe Network");
                threatStatusIcon.setImageResource(R.drawable.without_threats);
            }

            new androidx.appcompat.app.AlertDialog.Builder(HomeScreenActivity.this)
                    .setTitle("Network Risk Analysis")
                    .setMessage(
                            "📶 SSID: " + ssid + "\n" +
                                    "🔐 Encryption: " + encryption + "\n" +
                                    "🛡️ Risk Score: " + score + " / 10\n" +
                                    "⚠️ Threat Level: " + threatLevel + "\n\n" +
                                    "💡 Suggestion: " + suggestion
                    )
                    .setPositiveButton("Remediate", (dialog, which) -> {
                        Intent i = new Intent(HomeScreenActivity.this, Network_center.class);
                        startActivity(i);
                    })
                    .setNegativeButton("Close", null)
                    .show();
        });
    }


    private void promptUserForVPN() {
        Intent vpnIntent = VpnService.prepare(HomeScreenActivity.this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, 123);
        } else {
            startService(new Intent(this, SecureVpnService.class));
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
