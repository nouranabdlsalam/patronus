package com.example.patronus;

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

public class MaliciousAppsActivity extends AppCompatActivity {
    ImageButton back;
    TextView title, text;
    ImageView warning;
    RecyclerView recyclerView;
    Button done;
    ScrollView scrollView;
    static ArrayList<App> selectedApps = new ArrayList<>();
    LinearLayout navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_networks);

        back = findViewById(R.id.trusted_networks_back);

        title = findViewById(R.id.trusted_networks_title);
        title.setText("Malicious Apps");

        warning =  findViewById(R.id.trusted_networks_warning_icon);
        warning.setVisibility(View.GONE);

        text = findViewById(R.id.trusted_networks_text);

        scrollView = findViewById(R.id.trusted_networks_scroll);

        ViewGroup.LayoutParams layoutParams = scrollView.getLayoutParams();
        layoutParams.height = 1200;
        scrollView.setLayoutParams(layoutParams);

        recyclerView = findViewById(R.id.trusted_networks_recycler_view);

        navbar = findViewById(R.id.navigationBar);
        navbar.setVisibility(View.GONE);

        List<App> allApps = new ArrayList<>();
        boolean malware = false;
        if (getIntent().hasExtra("malwareApps")){
            malware = true;
            allApps = getIntent().getParcelableArrayListExtra("malwareApps");
            if (allApps == null) {
                Log.d("AppData", "Received malware list is NULL.");
            } else {
                Log.d("AppData", "Size of malware apps list: " + allApps.size());
            }
        }
        else {
            allApps = getIntent().getParcelableArrayListExtra("MaliciousIP");
        }

        if (allApps.isEmpty()){
            text.setText("No malicious apps were found.");
        }
        else {
            if (malware){
                text.setText("These apps request dangerous permissions and might be malicious. Click on an app to take action.");
            }
            else{
                text.setText("These apps communicate with suspicious IPs and might be malicious. Click on an app to take action.");
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            AppListAdapter adapter = new AppListAdapter(getApplicationContext(), allApps);
            recyclerView.setAdapter(adapter);
        }

        done = findViewById(R.id.add_trusted_networks_button);
        done.setVisibility(View.GONE);

    }
}