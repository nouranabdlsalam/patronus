package com.example.patronus;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MaliciousIP.class}, version = 1)
public abstract class IPDatabase extends RoomDatabase {
    private static volatile IPDatabase INSTANCE;

    public abstract IPDao ipDao();

    public static IPDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (IPDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            IPDatabase.class,
                            "ip_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
