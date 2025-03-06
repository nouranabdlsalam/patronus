package com.example.patronus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView ssidText, securityText, bssidText, encryptionText, vpnStatus, monitoringStatus;
    private ImageView threatStatusIcon;
    private TextView threatStatusText;
    private Button vpnToggleButton, monitoringToggleButton;

    private boolean isVpnActive = false;
    private boolean isMonitoringActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Initialize UI components
        ssidText = findViewById(R.id.ssidText);
        securityText = findViewById(R.id.securityText);
        bssidText = findViewById(R.id.bssidText);
        encryptionText = findViewById(R.id.encryptionText);
        vpnStatus = findViewById(R.id.vpnStatus);
        monitoringStatus = findViewById(R.id.monitoringStatus);
        threatStatusIcon = findViewById(R.id.threatStatusIcon);
        threatStatusText = findViewById(R.id.threatStatusText);


        checkNetworkDetails();
        setupListeners();
    }

    private void checkNetworkDetails() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return; // Exit method until permission is granted
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            Toast.makeText(this, "Wi-Fi Manager unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled. Please enable it.", Toast.LENGTH_LONG).show();
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo == null) {
            Toast.makeText(this, "Unable to retrieve Wi-Fi info", Toast.LENGTH_SHORT).show();
            return;
        }

        String ssid = wifiInfo.getSSID().replace("\"", "");
        String bssid = wifiInfo.getBSSID();
        ssidText.setText(ssid);
        bssidText.setText(bssid);

        String securityType = getSecurityType(wifiManager);
        securityText.setText(securityType);

        boolean isThreat = isMaliciousSSID(ssid);
        threatStatusIcon.setImageResource(isThreat ? R.drawable.with_threats_warning : R.drawable.without_threats);
        threatStatusText.setText(isThreat ? "6 Threats Found" : "No Threats Found");
    }

    private String getSecurityType(WifiManager wifiManager) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Permission Required";
        }
        List<ScanResult> scanResults = wifiManager.getScanResults();

        if (scanResults != null) {
            for (ScanResult result : scanResults) {
                if (result.SSID.equals(ssidText.getText().toString())) {
                    if (result.capabilities.contains("WPA3")) return "WPA3";
                    if (result.capabilities.contains("WPA2")) return "WPA2";
                    if (result.capabilities.contains("WPA")) return "WPA";
                    if (result.capabilities.contains("WEP")) return "WEP";
                    return "Open Network";
                }
            }
        }
        return "Unknown";
    }

    private boolean isMaliciousSSID(String ssid) {
        String[] maliciousSSIDs = {"Malicious_Wifi", "Free_Hacked_Hotspot", "Fake_AP"};

        for (String malicious : maliciousSSIDs) {
            if (ssid.equalsIgnoreCase(malicious)) {
                return true;
            }
        }

        return false;
    }

    private void setupListeners() {
        vpnToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVpnActive = !isVpnActive;
                vpnStatus.setText(isVpnActive ? "VPN: Connected" : "VPN: Disconnected");
                vpnToggleButton.setBackgroundColor(ContextCompat.getColor(HomeScreenActivity.this,
                        isVpnActive ? R.color.green : R.color.red));
            }
        });

        monitoringToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMonitoringActive = !isMonitoringActive;
                monitoringStatus.setText(isMonitoringActive ? "Monitoring: Enabled" : "Monitoring: Disabled");
                monitoringToggleButton.setBackgroundColor(ContextCompat.getColor(HomeScreenActivity.this,
                        isMonitoringActive ? R.color.green : R.color.red));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkNetworkDetails();
            } else {
                Toast.makeText(this, "Location permission is required for Wi-Fi scanning", Toast.LENGTH_LONG).show();
            }
        }
    }
}
