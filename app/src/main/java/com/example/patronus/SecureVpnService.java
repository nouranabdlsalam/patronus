package com.example.patronus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;

public class SecureVpnService extends VpnService implements Runnable {

    private static final String TAG = "SecureVpnService";
    private static final String CHANNEL_ID = "vpn_channel_id";

    private Thread vpnThread;
    private ParcelFileDescriptor vpnInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting VPN...");

        // Setup VPN
        Builder builder = new Builder();
        builder.setSession("PatronusVPN")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0);

        vpnInterface = builder.establish();

        startForeground(1, createNotification());
        vpnThread = new Thread(this, "VPNThread");
        vpnThread.start();

        return START_STICKY;
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, HomeScreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "VPN Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Patronus VPN is active");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Patronus VPN Active")
                .setContentText("You're protected on this network")
                .setSmallIcon(R.drawable.vpn)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
    }

    @Override
    public void run() {
        try {
            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            byte[] packet = new byte[32767];

            while (!Thread.interrupted()) {
                int length = in.read(packet);
                if (length > 0) {
                    if (PacketParser.containsHttpPlaintext(packet, length)) {
                        Log.w(TAG, "Suspicious plaintext traffic detected!");
                        ThreatLogger.log("Plaintext HTTP traffic detected: potential sensitive info leaked.");
                    }
                    String domain = PacketParser.parseDNSPacket(packet, length);
                    if (domain != null && !domain.isEmpty()) {
                        Log.i(TAG, "🌐 DNS Request: " + domain);
                        ThreatLogger.log("DNS Query: " + domain);

                        if (Blacklist.isBlacklisted(domain)) {
                            Log.w(TAG, "🚫 Blocked Domain Access Attempt: " + domain);
                            ThreatLogger.log("Blocked domain: " + domain);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "VPN thread failed", e);
        }
    }


    private boolean containsNonAscii(byte[] data, int len) {
        for (int i = 0; i < len; i++) {
            byte b = data[i];
            if (b < 32 || b > 126) return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        if (vpnThread != null) vpnThread.interrupt();
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close VPN interface", e);
        }
        stopForeground(true);
        Log.i(TAG, "VPN stopped.");
        super.onDestroy();
    }

    public static boolean isVpnActive() {
        try {
            for (NetworkInterface net : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (net.isUp() && net.getName().equals("tun0")) return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "VPN status check failed", e);
        }
        return false;
    }
}
