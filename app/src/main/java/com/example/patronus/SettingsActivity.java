package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    MaterialSwitch preconSwitch, networkSwitch;
    SharedPreferencesManager prefs;
    ImageButton SecurityModes, TrustedNetworks, home, scan, help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        LinearLayout nav_home= findViewById(R.id.nav_home);
        nav_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, HomeScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_scan= findViewById(R.id.nav_scan);
        nav_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ScanScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_help= findViewById(R.id.nav_help);
        nav_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, Help.class);
                startActivity(intent);
            }
        });


        prefs = new SharedPreferencesManager(this);

        preconSwitch = findViewById(R.id.PreconSwitch);
        networkSwitch = findViewById(R.id.networkMonitoringSwitch);


        preconSwitch.setChecked(prefs.isPreconnectionScanningOn());
        networkSwitch.setChecked(prefs.isNetworkMonitoringOn());


        preconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setPreconnectionScanningOn(isChecked);
            restartBgReceiverService(this);
        });


        networkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setNetworkMonitoringOn(isChecked);
            restartBgReceiverService(this);
        });

        SecurityModes = findViewById(R.id.SecModeSwitch);


        SecurityModes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SecurityModesActivity.class);
                startActivity(intent);
            }
        });

        TrustedNetworks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, TrustedNetworksActivity.class);
                startActivity(intent);
            }
        });





    }

    private static void restartBgReceiverService(Context context) {
        Intent serviceIntent = new Intent(context, BgReceiverService.class);
//        stopService(serviceIntent);
        context.startService(serviceIntent);
    }

}