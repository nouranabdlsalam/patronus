package com.example.patronus;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface IPDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIP(MaliciousIP ip);

    @Query("SELECT EXISTS(SELECT 1 FROM malicious_ips WHERE ip = :ipToCheck)")
    boolean isMalicious(String ipToCheck);

    @Query("SELECT * FROM malicious_ips")
    List<MaliciousIP> getAllIPs();

    @Query("DELETE FROM malicious_ips WHERE ip = :ipToDelete")
    void deleteIP(String ipToDelete);

    // Bulk insertion method
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIPs(List<MaliciousIP> ipList);

    @Query("SELECT COUNT(*) FROM malicious_ips")
    int getTotalIPsCount();
}
