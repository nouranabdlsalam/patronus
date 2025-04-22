package com.example.patronus;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    SharedPreferences prefs;


    public SharedPreferencesManager (Context context){
        prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    }


    public boolean isMalwareMonitoringOn(){
        return prefs.getBoolean("malwareMonitoring", true);
    }

    public boolean isNetworkMonitoringOn(){
        return prefs.getBoolean("networkMonitoring", true);
    }

    public boolean isBalancedModeOn(){
        return prefs.getBoolean("balancedMode", true);
    }


    public void setMalwareMonitoringOn(boolean isEnabled){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("malwareMonitoring", isEnabled);
        editor.apply();
    }

    public void setNetworkMonitoringOn(boolean isEnabled){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("networkMonitoring", isEnabled);
        editor.apply();
    }

    public void setBalancedModeOn(boolean isEnabled){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("balancedMode", isEnabled);
        editor.apply();
    }

}
