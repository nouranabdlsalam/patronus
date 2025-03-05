package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class TrustedNetworksActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<ListItemData> trustedNetworks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_networks);

        recyclerView = findViewById(R.id.trusted_networks_recycler_view);
        trustedNetworks = loadTrustedNetworks();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdapter adapter = new ListAdapter(trustedNetworks);
        recyclerView.setAdapter(adapter);

    }

    public static ArrayList<ListItemData> loadTrustedNetworks(){
        String [] trustedNetworks = {"Cafe", "My Home", "Network2"};
        ArrayList <ListItemData> result = new ArrayList<>();
        for (String network: trustedNetworks) {
            result.add(new ListItemData(R.drawable.wifi_icon, network, R.drawable.trash));
        }
        return result;
    }
}