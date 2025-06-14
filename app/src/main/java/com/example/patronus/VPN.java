package com.example.patronus;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VPN extends VpnService implements Runnable {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (vpnThread != null) return START_STICKY;

        Builder builder = new Builder();
        builder.setSession("SnifferVPN")
                .addAddress("10.0.2.0", 32)
                .addDnsServer("8.8.8.8");
//                .addRoute("0.0.0.0", 0);

        try {
            vpnInterface = builder.establish();
            Log.d("VPN", "ESTABLISHED");
        } catch (Exception e) {
            Log.e("VPN", "Failed to establish VPN", e);
            stopSelf();
            return START_NOT_STICKY;
        }

        isRunning = true;
        vpnThread = new Thread(this, "SnifferVPN");
        vpnThread.start();

        return START_STICKY;
    }

    @Override
    public void run() {
        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
        ByteBuffer packet = ByteBuffer.allocate(32767);

        while (isRunning) {
            try {
                packet.clear();
                int length = in.read(packet.array());

                if (length > 0) {
                    int version = (packet.get(0) >> 4) & 0xF;

                    if (version == 4) { // IPv4
                        String srcIP = getIPv4Address(packet.getInt(12));
                        String destIP = getIPv4Address(packet.getInt(16));

                        Log.d("Sniffer", "IPv4 Packet: " + srcIP + " -> " + destIP);

                        // Inspect the packet for threats
                        inspectPacketForThreats(srcIP, destIP);

                        // Forward the packet to its destination
                        forwardPacket(packet);
                    }
                }
            } catch (Exception e) {
                Log.e("Sniffer", "Packet read error", e);
            }
        }
    }

    private void forwardPacket(ByteBuffer packet) {
        try {
            // Write the packet back to the VPN interface (sending it to the original destination)
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            out.write(packet.array());
            Log.d("FORWARD", "Forwarded Packet");
        } catch (IOException e) {
            Log.e("Sniffer", "Failed to forward packet", e);
        }
    }

    private void inspectPacketForThreats(String srcIP, String destIP) {
        Log.d("COMPARE", "Compared " + destIP);
    }


    private String getIPv4Address(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (IOException ignored) {}
        super.onDestroy();
    }
}


