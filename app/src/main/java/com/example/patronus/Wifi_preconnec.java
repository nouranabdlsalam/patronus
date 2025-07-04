package com.example.patronus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Wifi_preconnec {
    private final WifiManager wifiManager;
    private final Context context;
    private WifiScanListener listener;
    private BroadcastReceiver wifiScanReceiver;
    private boolean isReceiverRegistered = false;
    private final Set<String> notifiedSsids = new HashSet<>();
    private static final String TAG = "Wifi_preconnec";

    public Wifi_preconnec(Context context, WifiScanListener listener) {
        this.context = context;
        this.listener = listener;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void startScan() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "Please enable Wi-Fi to scan networks", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isReceiverRegistered) {
            wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    context.unregisterReceiver(this);
                    isReceiverRegistered = false;

                    if (scanResults == null || scanResults.isEmpty()) {
                        Toast.makeText(context, "No networks found. Try again later.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> wifiList = new ArrayList<>();

                    for (ScanResult scanResult : scanResults) {
                        int riskScore = calculateRiskScore(scanResult, scanResults);

                        String wifiInfo = String.format("SSID: %s\nBSSID: %s\nSignal Strength: %d dBm\nEncryption: %s\nRisk Score: %d",
                                scanResult.SSID, scanResult.BSSID, scanResult.level,
                                getSecurityType(scanResult.capabilities), riskScore);

                        wifiList.add(wifiInfo);

//                        if (riskScore >= 4 && !notifiedSsids.contains(scanResult.SSID)) {
                        if (!notifiedSsids.contains(scanResult.SSID)) {
                            if (listener instanceof HomeScreenActivity) {
                                ((HomeScreenActivity) listener).onThreatDetected(
                                        scanResult.SSID,
                                        scanResult.BSSID,
                                        getSecurityType(scanResult.capabilities),
                                        riskScore
                                );
                                notifiedSsids.add(scanResult.SSID);
                            }
                        }
                    }


                    if (listener != null) {
                        listener.onWifiScanCompleted(wifiList);
                    }
                }
            };

            context.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isReceiverRegistered = true;
        }

        boolean success = wifiManager.startScan();
        if (!success) {
            Toast.makeText(context, "Failed to initiate scan", Toast.LENGTH_SHORT).show();
        }
    }

    public BroadcastReceiver getReceiver() {
        return wifiScanReceiver;
    }

    public String getSecurityLevelFromCapabilities(String ssid) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Unknown"; // fallback if permission not granted
        }

        List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult result : results) {
            if (result.SSID.equals(ssid)) {
                return getSecurityType(result.capabilities);
            }
        }
        return "Unknown";
    }



    public int calculateRiskScore(ScanResult scanResult, List<ScanResult> allResults) {
        int score = 0;

        String capabilities = scanResult.capabilities;
        int rssi = scanResult.level;
        String ssid = scanResult.SSID;

        // Encryption / Authentication
        if (capabilities.contains("WEP")) {
            score += 4; // High risk
        } else if (capabilities.contains("WPA") && !capabilities.contains("WPA2")) {
            score += 3; // Moderate risk
        } else if (capabilities.contains("WPA2") && !capabilities.contains("WPA3")) {
            score += 2; // Lower risk
        } else if (capabilities.contains("WPA3")) {
            score += 1; // Low risk
        } else if (capabilities.contains("ESS")) {
            score += 5; // Open network, very high risk
        }

        // Signal Strength (Evil Twin / Rogue AP detection suspicion)
        if (rssi > -40 && capabilities.contains("ESS")) {
            score += 2; // suspiciously strong open AP
        }

        // Check for SSID confusion - same SSID but different security
        for (ScanResult other : allResults) {
            if (!other.BSSID.equals(scanResult.BSSID) && other.SSID.equals(ssid)) {
                if (!getSecurityType(other.capabilities).equals(getSecurityType(capabilities))) {
                    score += 2; // suspicious SSID confusion
                }
            }
        }

        // Final clamp for more meaningful range
        return Math.min(score, 10);
    }


    private String getSecurityType(String capabilities) {
        if (capabilities.contains("WPA3")) return "WPA3";
        else if (capabilities.contains("WPA2")) return "WPA2";
        else if (capabilities.contains("WPA")) return "WPA";
        else if (capabilities.contains("WEP")) return "WEP";
        else return "Open";
    }

    public interface WifiScanListener {
        void onWifiScanCompleted(List<String> wifiList);
    }
}
