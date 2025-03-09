package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.radiobutton.MaterialRadioButton;

public class ScanScreenActivity extends AppCompatActivity {
    AppCompatRadioButton allApps, selectApps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_screen);
        allApps = findViewById(R.id.all_apps_radio);
        selectApps = findViewById(R.id.select_apps_radio);

        allApps.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color));
        selectApps.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color));
    }
}