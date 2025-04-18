package com.example.patronus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Wifi_preconnec {

    private static final String TAG = "WiFiScan";
    private Context context;
    private WifiManager wifiManager;
    private ListView wifiListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> wifiInfoList = new ArrayList<>();

    public Wifi_preconnec(Context ctx, ListView listView) {
        this.context = ctx;
        this.wifiListView = listView;
        this.wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void startScan() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "Wi-Fi is off, enabling...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission is required for Wi-Fi scanning.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = wifiManager.startScan();
        if (!success) {
            Log.e(TAG, "First Wi-Fi scan failed. Retrying in 2 seconds...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> wifiManager.startScan(), 2000);
        }else {
            Log.d(TAG, "Wi-Fi scan started.");

            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                android.view.View rootView = activity.findViewById(android.R.id.content);
                com.google.android.material.snackbar.Snackbar.make(rootView,
                                "Scanning Wi-Fi network...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                        .show();
            }
        }


        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
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
                wifiInfoList.clear();

                for (ScanResult scanResult : scanResults) {
                    String ssid = scanResult.SSID;
                    String bssid = scanResult.BSSID;
                    int rssi = scanResult.level;
                    String encryption = scanResult.capabilities;

                    String securityLevel = getSecurityLevelFromCapabilities(encryption);

                    int score = 0;
                    if (encryption.contains("WPA3")) score += 3;
                    else if (encryption.contains("WPA2")) score += 2;
                    else if (encryption.contains("WEP")) score += 1;
                    else score -= 2;

                    int sameSSIDCount = 0;
                    for (ScanResult s : scanResults) {
                        if (s.SSID.equals(ssid)) sameSSIDCount++;
                    }
                    if (sameSSIDCount > 1) score -= 2;

                    String riskLevel = (score >= 3) ? "Low Risk" : (score >= 1) ? "Medium Risk" : "High Risk";
                    String rogueHint = (sameSSIDCount > 1) ? " Possible Rogue AP" : "";

                    String info = "SSID: " + ssid +
                            "\n BSSID: " + bssid +
                            "\n Signal Strength: " + rssi + " dBm" +
                            "\n Encryption: " + securityLevel +
                            "\n Risk Score: " + score + " (" + riskLevel + ")" +
                            (rogueHint.isEmpty() ? "" : "\n " + rogueHint);

                    Log.d(TAG, info);
                    wifiInfoList.add(info);

                    if (riskLevel.equals("High Risk")) {
                        sendThreatNotification(ssid, riskLevel, score, rogueHint);
                        showVpnDialog(ssid);
                    }
                }

                if (wifiListView != null) {
                    adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, wifiInfoList);
                    wifiListView.setAdapter(adapter);
                }

                context.unregisterReceiver(this);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (!success) {
            Toast.makeText(context, "Wi-Fi scan failed", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Wi-Fi scan failed");
        }
    }

    private void sendThreatNotification(String ssid, String riskLevel, int score, String rogueHint) {
        String message = " \"" + ssid + "\" is unsafe\nRisk: " + riskLevel + " (" + score + ")" +
                (rogueHint.isEmpty() ? "" : "\n" + rogueHint);

        NotificationHelper.sendNotification(context,
                "Unsafe Wi-Fi Detected",
                message);

        Toast.makeText(context, "Unsafe Wi-Fi detected: " + ssid, Toast.LENGTH_SHORT).show();
    }

    private void showVpnDialog(String ssid) {
        new Handler(Looper.getMainLooper()).post(() -> {
            new AlertDialog.Builder(context)
                    .setTitle("⚠️ High-Risk Network")
                    .setMessage("The Wi-Fi \"" + ssid + "\" is not safe.\n\nUse VPN for protection?")
                    .setPositiveButton("Yes, Use VPN", (dialog, which) -> {
                        Intent vpnIntent = VpnService.prepare(context);
                        if (vpnIntent != null) {
                            if (context instanceof Activity) {
                                ((Activity) context).startActivityForResult(vpnIntent, 123);
                            }
                        } else {
                            context.startService(new Intent(context, SecureVpnService.class));
                        }
                    })
                    .setNegativeButton("No, Continue Anyway", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    public static class WifiConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && netInfo.isConnected()) {
                Log.d("WiFiReceiver", "Connected to Wi-Fi. Triggering scan...");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (context instanceof HomeScreenActivity) {
                        ((HomeScreenActivity) context).performWifiScan();
                    }
                }, 2000);
            }
        }
    }


    public String getSecurityLevelFromCapabilities(String capabilities) {
        if (capabilities.contains("WPA3")) return "WPA3";
        if (capabilities.contains("WPA2")) return "WPA2";
        if (capabilities.contains("WEP")) return "WEP";
        return "Open";
    }
}
