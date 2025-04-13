package com.example.patronus;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trusted_networks")
public class TrustedNetwork {
    @PrimaryKey
    @androidx.annotation.NonNull
    private String bssid;  // Unique identifier
    private String ssid;   // Network name

    public TrustedNetwork(String bssid, String ssid) {
        this.bssid = bssid;
        this.ssid = ssid;
    }

    public String getBssid() { return bssid; }
    public String getSsid() { return ssid; }
}

