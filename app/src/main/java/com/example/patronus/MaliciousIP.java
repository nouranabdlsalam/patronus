package com.example.patronus;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "malicious_ips")
public class MaliciousIP {

    @PrimaryKey
    @NonNull
    public String ip;

    public MaliciousIP(@NonNull String ip) {
        this.ip = ip;
    }
}

