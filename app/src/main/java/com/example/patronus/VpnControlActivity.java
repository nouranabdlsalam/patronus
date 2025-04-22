package com.example.patronus;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VpnControlActivity extends AppCompatActivity {

    private static final String TAG = "VpnControlActivity";
    private static final int VPN_REQUEST_CODE = 123;

    private TextView vpnStatus;
    private Button startVpnButton, stopVpnButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_test);

        vpnStatus = findViewById(R.id.vpnStatus);
        startVpnButton = findViewById(R.id.startVpnButton);
        stopVpnButton = findViewById(R.id.stopVpnButton);

        Log.d(TAG, "UI initialized");

        startVpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start VPN button clicked");
                prepareVpn();
            }
        });

        stopVpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stop VPN button clicked");
                stopService(new Intent(VpnControlActivity.this, SecureVpnService.class));
                vpnStatus.setText("VPN Status: Stopped");
                Log.i(TAG, "VPN manually stopped");
            }
        });
    }

    private void prepareVpn() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            Log.d(TAG, "VPN permission not yet granted");
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            Log.d(TAG, "VPN permission already granted");
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "VPN permission granted, starting service...");
            startService(new Intent(this, SecureVpnService.class));
            vpnStatus.setText("VPN Status: Running");
        } else {
            Log.w(TAG, "VPN permission denied by user.");
        }
    }

}
