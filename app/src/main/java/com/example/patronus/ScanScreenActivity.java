package com.example.patronus;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ScanScreenActivity extends AppCompatActivity {
    RadioButton allApps, selectApps;
    ArrayList<App> selectedApps;
    PackageManager packageManager;
    List<ApplicationInfo> InstalledAppsPm;
    ArrayList<App> malware;
    ImageButton scanButton, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_screen);
        allApps = findViewById(R.id.all_apps_radio);
        selectApps = findViewById(R.id.select_apps_radio);
        scanButton = findViewById(R.id.malware_scan);
        back = findViewById(R.id.scanback);

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
                Intent intent = new Intent(ScanScreenActivity.this, HomeScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_settings= findViewById(R.id.nav_settings);
        nav_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanScreenActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_help= findViewById(R.id.nav_help);
        nav_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanScreenActivity.this, Help.class);
                startActivity(intent);
            }
        });



        selectedApps = new ArrayList<App>();

        allApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedApps = getAllApps();
            }
        });

       selectApps.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
//               if (!(getIntent().hasExtra("selectedApps"))) {
                   getSelectedApps();
//               }
           }
       });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedApps.isEmpty()){
                    Log.d("SCANNING: ", "No Apps Selected");
                    selectedApps = getAllApps();
                }

                AppScanner scanner = new AppScanner(getApplicationContext());
                malware = new ArrayList<App>();
                long start = System.currentTimeMillis();
                for (App app: selectedApps) {
                    try {
                        if (scanner.scanApp(app) == 1){
                            app.setIcon(2);
                            malware.add(app);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                Log.d("SCAN TIME", (System.currentTimeMillis() - start) + " ms");
                triggerThreatRemediation(malware);
            }
        });
    }

    protected ArrayList<App> getAllApps(){
        ArrayList<App> allApps = new ArrayList<>();
        packageManager = getApplicationContext().getPackageManager();
        InstalledAppsPm = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app: InstalledAppsPm) {
            if ((app.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                if (!app.packageName.equals(getPackageName())){
                    Log.d("AppDebug", "Adding app: " + app.packageName);
                    App currentApp = new App(app.packageName, app.loadLabel(packageManager).toString());
                    currentApp.setIcon(0);
                    allApps.add(currentApp);
                }
            }
        }
        return allApps;
    }

    protected void getSelectedApps(){
        ArrayList<App> allApps = getAllApps();
        Intent intent = new Intent(getApplicationContext(), SelectAppActivity.class);
        intent.putParcelableArrayListExtra("allApps", allApps);
        startActivity(intent);
    }


    protected void triggerThreatRemediation(ArrayList<App> malware){
        for (App app: malware) {
            Log.d("MALWARE", app.packageName);
        }
        Intent intent = new Intent(getApplicationContext(), MaliciousAppsActivity.class);
        intent.putParcelableArrayListExtra("malwareApps",malware);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((getIntent().hasExtra("selectedApps"))) {
            selectedApps = getIntent().getParcelableArrayListExtra("selectedApps");
            selectApps.setChecked(true);
        }
    }
}