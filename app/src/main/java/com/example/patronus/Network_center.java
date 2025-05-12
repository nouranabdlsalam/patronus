    package com.example.patronus;

    import android.Manifest;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.PackageManager;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;
    import android.net.wifi.ScanResult;
    import android.net.wifi.WifiManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.widget.RadioButton;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;

    import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.net.InetSocketAddress;
    import java.net.Socket;
    import java.util.List;

    public class Network_center extends AppCompatActivity {

        WifiManager wifiManager;
        RadioButton radioBasic, radioAdvanced, radioTroubleshoot;
        ImageButton scanButton, back;
        final double[] downloadSpeedValue = {0f};
        final double[] uploadSpeedValue = {0f};


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_network_center);

            back = findViewById(R.id.netcenterback);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            LinearLayout nav_home= findViewById(R.id.nav_home);
            nav_home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Network_center.this, HomeScreenActivity.class);
                    startActivity(intent);
                }
            });

            LinearLayout nav_settings= findViewById(R.id.nav_settings);
            nav_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Network_center.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });

            LinearLayout nav_scan= findViewById(R.id.nav_scan);
            nav_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Network_center.this, ScanScreenActivity.class);
                    startActivity(intent);
                }
            });
            LinearLayout nav_help= findViewById(R.id.nav_help);
            nav_help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Network_center.this, Help.class);
                    startActivity(intent);
                }
            });

            radioBasic = findViewById(R.id.rb_basic);
            radioAdvanced = findViewById(R.id.rb_advanced);
            radioTroubleshoot = findViewById(R.id.rb_troubleshoot);
            scanButton = findViewById(R.id.network_scan);


            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            scanButton.setOnClickListener(v -> {
                if (!wifiManager.isWifiEnabled()) {
                    Toast.makeText(Network_center.this, "Please enable Wi-Fi first.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (radioBasic.isChecked()) {
                    Toast.makeText(Network_center.this, "Network Scanning is running, please wait.", Toast.LENGTH_LONG).show();
                    showGeneralInfo();
                } else if (radioAdvanced.isChecked()) {
                    Toast.makeText(Network_center.this, "Running Speed Test, please wait.", Toast.LENGTH_LONG).show();
                    StringBuilder speedResults = new StringBuilder("Running Speed Test...\n\n");
                    AlertDialog.Builder builder = new AlertDialog.Builder(Network_center.this);
                    builder.setTitle("Speed Test Results");
                    builder.setMessage(speedResults.toString());
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    SpeedChecker speedChecker = new SpeedChecker(Network_center.this);

                    speedChecker.measureDownloadSpeed("https://www.google.com", downloadSpeed -> runOnUiThread(() -> {
                        downloadSpeedValue[0] = downloadSpeed;
                        speedResults.append("📥 Download Speed: ")
                                .append(String.format("%.2f", downloadSpeed)).append(" Mbps\n");
                        dialog.setMessage(speedResults.toString());
                    }));


                    speedChecker.measureUploadSpeed("https://httpbin.org/post", uploadSpeed -> runOnUiThread(() -> {
                        uploadSpeedValue[0] = uploadSpeed;
                        speedResults.append("📤 Upload Speed: ")
                                .append(String.format("%.2f", uploadSpeed)).append(" Mbps\n");

                        String quality;
                        String suggestion;
                        if (downloadSpeedValue[0] >= 25 && uploadSpeedValue[0] >= 5) {
                            quality = "✅ Excellent Connection";
                            suggestion = "Your internet is great! No action needed.";
                        } else if (downloadSpeedValue[0] >= 5 && uploadSpeedValue[0] >= 1) {
                            quality = "⚠️ Moderate Connection";
                            suggestion = "Try moving closer to the router or reducing background usage.";
                        } else {
                            quality = "❌ Poor Connection";
                            suggestion = "Consider restarting your router, or upgrading your plan.";
                        }

                        speedResults.append("\n📊 Connection Quality: ").append(quality).append("\n")
                                .append("💡 Suggestion: ").append(suggestion);
                        dialog.setMessage(speedResults.toString());
                    }));


                } else if (radioTroubleshoot.isChecked()) {
                    Toast.makeText(Network_center.this, "Running troubleshoot test, please wait.", Toast.LENGTH_LONG).show();
                    runConnectivityTest();
                } else {
                    Toast.makeText(Network_center.this, "Please select an option first.", Toast.LENGTH_SHORT).show();
                }
            });

        }
        private void runConnectivityTest() {
            new Thread(() -> {
                StringBuilder result = new StringBuilder();

                boolean reachable = false;
                try (java.net.Socket socket = new java.net.Socket()) {
                    socket.connect(new java.net.InetSocketAddress("8.8.8.8", 53), 3000);
                    reachable = true;
                } catch (Exception e) {
                    result.append("❌ Socket test failed: Unable to reach 8.8.8.8:53\n");
                }

                if (reachable) {
                    result.append("✅ Internet connection is available.\n\n");
                } else {
                    result.append("⚠️ No internet access.\n\n");
                }

                result.append("📡 Ping Test Output:\n");

                try {
                    Process process = Runtime.getRuntime().exec("ping -c 4 8.8.8.8");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    process.waitFor();
                } catch (Exception e) {
                    result.append("⚠️ Ping command failed or not supported on this device.\n");
                }

                if (result.toString().trim().equals("")) {
                    result.append("⚠️ No output from ping. It may be restricted on your device.\n");
                }

                showTroubleshootResult(result.toString());
            }).start();
        }



        private void showGeneralInfo() {
            String ssid = wifiManager.getConnectionInfo().getSSID();
            String bssid = wifiManager.getConnectionInfo().getBSSID();
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String ipAddr = (ipAddress & 0xff) + "." +
                    ((ipAddress >> 8) & 0xff) + "." +
                    ((ipAddress >> 16) & 0xff) + "." +
                    ((ipAddress >> 24) & 0xff);

            String capabilities = "";
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overridingz
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult result : scanResults) {
                if (result.BSSID.equals(bssid)) {
                    capabilities = result.capabilities;
                    break;
                }
            }

            boolean isOpenNetwork = capabilities.contains("WPA") || capabilities.contains("WEP") ? false : true;
            String networkType = wifiManager.getConnectionInfo().getNetworkId() != -1 ? "Wi-Fi" : "Unknown";


            boolean isRogue = ssid != null && ssid.toLowerCase().contains("rogue");
            String riskLevel = isRogue ? "⚠️ Unsafe network detected (Rogue AP?)" : "✅ Network appears safe";


            StringBuilder info = new StringBuilder();
            info.append("📡 SSID (Network Name): ").append(ssid).append("\n")
                    .append("🔗 BSSID (Router MAC): ").append(bssid).append("\n")
                    .append("🌐 IP Address: ").append(ipAddr).append("\n")
                    .append("🔐 Security: ").append(isOpenNetwork ? "Open (⚠️ Not Secured)" : "Secured").append("\n")
                    .append("💻 Interface: ").append(networkType).append("\n")
                    .append("🛡️ Risk Score: ").append(riskLevel);


            new AlertDialog.Builder(this)
                    .setTitle("Network Info")
                    .setMessage(info.toString())
                    .setPositiveButton("Remediate", (dialog, which) -> {
                        if (isRogue) {
                            Toast.makeText(Network_center.this, "Rogue AP Detected!", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(Network_center.this, ThreatRemediation.class);
                        startActivity(intent);

                    })
                    .setNegativeButton("Close", null)
                    .show();
        }



        private void runDiagnosticCommand(String command) {
            new Thread(() -> {
                StringBuilder output = new StringBuilder();

                if (!isNetworkAvailable()) {
                    output.append("No internet connection detected. Please check your network settings.");
                    showTroubleshootResult(output.toString());
                    return;
                }

                try {
                    Process process = Runtime.getRuntime().exec(command);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    process.waitFor();
                } catch (Exception e) {
                    output.append("Error: ").append(e.getMessage());
                }

                showTroubleshootResult(output.toString());
            }).start();
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        private void showTroubleshootResult(String result) {
            runOnUiThread(() -> new AlertDialog.Builder(Network_center.this)
                    .setTitle("Troubleshoot Result")
                    .setMessage(result)
                    .setPositiveButton("OK", null)
                    .show());
        }

    }
