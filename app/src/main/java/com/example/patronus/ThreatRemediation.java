package com.example.patronus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ThreatRemediation extends AppCompatActivity {
    TextView attackTitle, attackDescription, title;
    MaterialButton option1, option2;
    App MaliciousApp;

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

        title = findViewById(R.id.tvTitle);
        attackTitle = findViewById(R.id.attacktitle);
        attackDescription = findViewById(R.id.attackdescription);
        option1 = findViewById(R.id.manualremidiationoption1);
        option2 = findViewById(R.id.manualremidiationoption2);

        title.setText("Threat Remediation");

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

            if (MaliciousApp.getMaliciousIPs().size() > 1){
                String IPs = "";
                for (String ip: MaliciousApp.getMaliciousIPs()) {
                    IPs += (ip + ", ");
                }
                attackDescription.setText( MaliciousApp.getName() + " communicates with suspicious IPs: " + IPs + " and might be malicious. " +
                        "If you don't trust this app, we recommend deleting it.");
            }
            else{
                attackDescription.setText( MaliciousApp.getName() + " communicates with a suspicious IP: " + MaliciousApp.getMaliciousIPs().get(0) + " and might be malicious. " +
                        "If you don't trust this app, we recommend deleting it.");
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

        option2.setVisibility(View.GONE);
    }
}