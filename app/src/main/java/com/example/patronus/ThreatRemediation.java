package com.example.patronus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ThreatRemediation extends AppCompatActivity {
    TextView attackTitle, attackDescription;
    MaterialButton option1;
    App MaliciousApp;
    ImageButton back;
    TextView remediationOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manualthreat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back = findViewById(R.id.threatback);
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
                Intent intent = new Intent(ThreatRemediation.this, HomeScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_settings= findViewById(R.id.nav_settings);
        nav_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThreatRemediation.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_help= findViewById(R.id.nav_help);
        nav_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThreatRemediation.this, Help.class);
                startActivity(intent);
            }
        });
        LinearLayout nav_scan= findViewById(R.id.nav_scan);
        nav_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThreatRemediation.this,ScanScreenActivity.class);
                startActivity(intent);
            }
        });

        attackTitle = findViewById(R.id.attacktitle);
        attackDescription = findViewById(R.id.attackdescription);
        option1 = findViewById(R.id.manualremidiationoption1);
        remediationOpt = findViewById(R.id.remed_opts);

        if (getIntent().hasExtra("MaliciousApp")){
            ArrayList<App> MaliciousAppList = getIntent().getParcelableArrayListExtra("MaliciousApp");
            if (!MaliciousAppList.isEmpty()){
                MaliciousApp = MaliciousAppList.get(0);
            }

            attackTitle.setText("Malware Threat Found");
            attackDescription.setText( MaliciousApp.getName() + " requests suspicious permissions and might be malicious. " +
                    "If you don't trust this app, we recommend deleting it.");

        }
        else if(getIntent().hasExtra("MaliciousIP")){
            ArrayList<App> MaliciousAppList = getIntent().getParcelableArrayListExtra("MaliciousIP");
            if (!MaliciousAppList.isEmpty()){
                MaliciousApp = MaliciousAppList.get(0);
            }

            attackTitle.setText("Network Threat Found");

            Log.d("Browser Check", MaliciousApp.getPackageName() + " is a browser? " + MaliciousApp.browser);

            if (MaliciousApp.browser){
                attackDescription.setText("You have visited a potentially malicious site while using " + MaliciousApp.getName() + ". It is communicating with a suspicious IP address: " + MaliciousApp.getMaliciousIPs().get(0) + " and we recommend avoiding it.");
                option1.setVisibility(View.GONE);
                remediationOpt.setVisibility(View.GONE);
            }
            else {
                if (MaliciousApp.getMaliciousIPs().size() > 1) {
                    String IPs = "";
                    for (String ip : MaliciousApp.getMaliciousIPs()) {
                        IPs += (ip + ", ");
                    }
                    attackDescription.setText(MaliciousApp.getName() + " communicates with suspicious IPs: " + IPs + " and might be malicious. " +
                            "If you don't trust this app, we recommend deleting it.");
                } else {
                    attackDescription.setText(MaliciousApp.getName() + " communicates with a suspicious IP: " + MaliciousApp.getMaliciousIPs().get(0) + " and might be malicious. " +
                            "If you don't trust this app, we recommend deleting it.");
                }
            }
        } else if (getIntent().hasExtra("wifi")) {
            option1.setVisibility(View.GONE);
            remediationOpt.setVisibility(View.GONE);
            if (!getIntent().getBooleanExtra("wifi", false)){
                attackDescription.setText("This Wi-Fi network is safe.");
                attackTitle.setText("No threats found.");
            }
        }


        option1.setText("Delete App");
        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                intent.setData(Uri.parse("package:" + MaliciousApp.getPackageName())); // replace with the target app's package name
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                startActivity(intent);
            }
        });

    }
}