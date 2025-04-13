package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class BlockedIPsActivity extends AppCompatActivity {
    TextView title, text;
    RecyclerView recyclerView;
    ArrayList<ListItemData> blockedIPs;
    MaterialButton blockBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_networks);

        title = findViewById(R.id.trusted_networks_title);
        text = findViewById(R.id.trusted_networks_text);
        recyclerView = findViewById(R.id.trusted_networks_recycler_view);
        blockBtn = findViewById(R.id.add_trusted_networks_button);

        title.setText("Blocked IPs");
        text.setText("Unblocked IPs remain unblocked for 24 hours only.");
        blockBtn.setText("Block an IP");


        blockedIPs = loadBlockedIPs();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdapter adapter = new ListAdapter(blockedIPs);
        recyclerView.setAdapter(adapter);
    }

    public static ArrayList<ListItemData> loadBlockedIPs(){
        String [] trustedNetworks = {"192.168.15.212", "192.168.13.122"};
        ArrayList <ListItemData> result = new ArrayList<>();
        for (String network: trustedNetworks) {
            result.add(new ListItemData(R.drawable.ip_icon, network, R.drawable.trash, false));
        }
        return result;
    }
}