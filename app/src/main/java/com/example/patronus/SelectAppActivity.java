package com.example.patronus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SelectAppActivity extends AppCompatActivity {
    ImageButton back;
    TextView title, text;
    ImageView warning;
    RecyclerView recyclerView;
    Button done;
    ScrollView scrollView;
    static ArrayList<App> selectedApps;
    LinearLayout navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_networks);

        selectedApps = new ArrayList<>();

        back = findViewById(R.id.trusted_networks_back);
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
                Intent intent = new Intent(SelectAppActivity.this, HomeScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_settings= findViewById(R.id.nav_settings);
        nav_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAppActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_help= findViewById(R.id.nav_help);
        nav_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAppActivity.this, Help.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_scan= findViewById(R.id.nav_scan);
        nav_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAppActivity.this, ScanScreenActivity.class);
                startActivity(intent);
            }
        });


        title = findViewById(R.id.trusted_networks_title);
        title.setText("Select App(s)");

        warning =  findViewById(R.id.trusted_networks_warning_icon);
        warning.setVisibility(View.GONE);

        text = findViewById(R.id.trusted_networks_text);
        text.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.trusted_networks_recycler_view);

        scrollView = findViewById(R.id.trusted_networks_scroll);

        ViewGroup.LayoutParams layoutParams = scrollView.getLayoutParams();
        layoutParams.height = 900;
        scrollView.setLayoutParams(layoutParams);
        done = findViewById(R.id.add_trusted_networks_button);
        navbar = findViewById(R.id.navigationBar);






        List<App> allApps = getIntent().getParcelableArrayListExtra("allApps");
        if (allApps == null || allApps.size() < 1) {
            Log.d("AppData", "Received apps list is NULL.");
            text.setVisibility(View.VISIBLE);
            text.setText("No apps found.");
            done.setVisibility(View.GONE);
        } else {
            Log.d("AppData", "Size of received apps list: " + allApps.get(0).packageName);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            AppListAdapter adapter = new AppListAdapter(getApplicationContext(), allApps);
            recyclerView.setAdapter(adapter);

            done.setText("Select Apps");

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ScanScreenActivity.class);
                    intent.putParcelableArrayListExtra("selectedApps", selectedApps);
                    startActivity(intent);
                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedApps = new ArrayList<>();
    }

    protected static ArrayList<App> setSelectedApps(String packageName, String name, int add){
        App selectedApp = new App(packageName, name);
        if (add == 0){
            selectedApps.add(selectedApp);
        }
        else{
            for (App app: selectedApps) {
                if (app.packageName.equals(packageName)){
                    selectedApps.remove(app);
                }
            }
        }
        return selectedApps;
    }


}