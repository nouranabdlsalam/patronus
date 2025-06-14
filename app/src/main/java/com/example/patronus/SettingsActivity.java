package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    MaterialSwitch malwareSwitch, networkSwitch, balancedSwitch;
    SharedPreferencesManager prefs;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        back = findViewById(R.id.settings_back);
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

        malwareSwitch = findViewById(R.id.malwareSwitch);
        networkSwitch = findViewById(R.id.networkMonitoringSwitch);
        balancedSwitch = findViewById(R.id.BalancedModeSwitch);


        malwareSwitch.setChecked(prefs.isMalwareMonitoringOn());
        networkSwitch.setChecked(prefs.isNetworkMonitoringOn());
        balancedSwitch.setChecked(prefs.isBalancedModeOn());


        malwareSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setMalwareMonitoringOn(isChecked);
            if (isChecked){
                balancedSwitch.setChecked(false);
                prefs.setBalancedModeOn(false);
            }
            restartSystemMonitoringService(this);
        });


        networkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setNetworkMonitoringOn(isChecked);
            restartSystemMonitoringService(this);
        });

        balancedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setBalancedModeOn(isChecked);
            if (isChecked){
                malwareSwitch.setChecked(false);
                prefs.setMalwareMonitoringOn(false);
            }
            restartSystemMonitoringService(this);
        });


    }

    private static void restartSystemMonitoringService(Context context) {
        Intent serviceIntent = new Intent(context, SystemMonitoringService.class);
        context.startService(serviceIntent);
    }

}