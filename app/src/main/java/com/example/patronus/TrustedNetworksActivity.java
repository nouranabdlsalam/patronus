package com.example.patronus;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TrustedNetworksActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<ListItemData> trustedNetworks;
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

    public List<ListItemData> loadTrustedNetworks(){
//        String [] trustedNetworks = {"Cafe", "My Home", "Network2"};
        AppDatabase db = AppDatabase.getInstance(this.getApplicationContext());
        TrustedNetworkDao dao = db.trustedNetworkDao();

//        new Thread(() -> {
//            dao.addNetwork(new TrustedNetwork("00:11:22:33:44:55", "MyHomeWiFi"));
//        }).start();

        List <ListItemData> result = new ArrayList<>();

        new Thread(() -> {
            List<TrustedNetwork> networks = dao.getAllNetworks();
//            for (TrustedNetwork n : networks) {
//                System.out.println("SSID: " + n.getSsid() + ", BSSID: " + n.getBssid());
//            }
            for (TrustedNetwork network: networks) {
                result.add(new ListItemData(R.drawable.wifi_icon, network.getSsid(), R.drawable.trash, false));
            }
        }).start();

        return result;
    }
}