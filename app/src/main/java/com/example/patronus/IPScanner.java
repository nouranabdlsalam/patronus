package com.example.patronus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkMetadataCollector {

    private final Context context;

    public NetworkMetadataCollector(Context context) {
        this.context = context;
    }

    // Only return USER apps with UID >= 10000
    public Map<Integer, String> getUserAppUidMap() {
        Map<Integer, String> uidPackageMap = new HashMap<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            if (app.uid < 10000) continue;  // skip system-level UIDs

            uidPackageMap.put(app.uid, pm.getNameForUid(app.uid));
        }

        return uidPackageMap;
    }

    public void printTrafficStats() {
        Map<Integer, String> uidToPkg = getUserAppUidMap();

        for (Map.Entry<Integer, String> entry : uidToPkg.entrySet()) {
            int uid = entry.getKey();
            String pkg = entry.getValue();

            long txBytes = TrafficStats.getUidTxBytes(uid);
            long rxBytes = TrafficStats.getUidRxBytes(uid);

            if (txBytes == TrafficStats.UNSUPPORTED || rxBytes == TrafficStats.UNSUPPORTED) continue;

            System.out.println("📦 App: " + pkg + " | UID: " + uid);
            System.out.println("   ↳ Sent: " + txBytes + " bytes | Received: " + rxBytes + " bytes");
        }
    }

    public void printConnections(String protocol) {
        String file = "/proc/net/" + protocol;
        Map<Integer, String> uidToPkg = getUserAppUidMap();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] fields = line.trim().split("\\s+");
                if (fields.length < 10) continue;

                String localAddressHex = fields[1];
                String remoteAddressHex = fields[2];
                int uid = Integer.parseInt(fields[7]);

                if (!uidToPkg.containsKey(uid)) continue; // only user apps

                String localIpPort = parseIpPort(localAddressHex);
                String remoteIpPort = parseIpPort(remoteAddressHex);
                String appName = uidToPkg.get(uid);

                System.out.println("🔌 " + protocol.toUpperCase() + " Connection:");
                System.out.println("   ↳ App: " + appName + " | UID: " + uid);
                System.out.println("   ↳ Local: " + localIpPort + " | Remote: " + remoteIpPort);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseIpPort(String hex) {
        String[] parts = hex.split(":");
        String ipHex = parts[0];
        String portHex = parts[1];

        String ip = hexToIp(ipHex);
        int port = Integer.parseInt(portHex, 16);

        return ip + ":" + port;
    }

    private String hexToIp(String hex) {
        StringBuilder ip = new StringBuilder();

        // The IP should be represented by 8 hex characters (4 octets, each 2 hex digits)
        for (int i = 0; i < 8; i += 2) {
            // Extract 2 characters, parse to int, and convert to decimal
            String hexOctet = hex.substring(i, i + 2);
            int octet = Integer.parseInt(hexOctet, 16);

            // Append octets in correct order (Big Endian)
            ip.insert(0, octet);  // Insert at the beginning for correct order

            // Add dot separator for octets (except the last one)
            if (i < 6) {
                ip.insert(0, ".");  // Add dot in the correct place
            }
        }

        return ip.toString();
    }

    public void runFullScan() {
        System.out.println("===== 📈 TRAFFIC STATS =====");
        printTrafficStats();

        System.out.println("\n===== 🌐 TCP CONNECTIONS =====");
        printConnections("tcp");

        System.out.println("\n===== 🌐 UDP CONNECTIONS =====");
        printConnections("udp");
    }
}

