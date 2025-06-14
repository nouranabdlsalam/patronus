package com.example.patronus;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrustedNetworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addNetwork(TrustedNetwork network);

    @Query("SELECT * FROM trusted_networks")
    List<TrustedNetwork> getAllNetworks();

    @Delete
    void deleteNetwork(TrustedNetwork network);

    @Query("DELETE FROM trusted_networks WHERE bssid = :bssid")
    void deleteByBssid(String bssid);
}

