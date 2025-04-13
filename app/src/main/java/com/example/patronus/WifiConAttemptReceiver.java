package com.example.patronus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;


public class WifiConAttemptReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo != null && networkInfo.getState().equals(NetworkInfo.State.CONNECTING)) {
//            Toast.makeText(context, "Network Connection Attempt Detected", Toast.LENGTH_SHORT).show();
            //disconnecting from the network
//            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            if (wifiManager != null) {
////                wifiManager.disconnect(); // Interrupt connection attempt
//
//                Toast.makeText(context, "Network Connection Paused", Toast.LENGTH_SHORT).show();
//            }
        }
    }
}
