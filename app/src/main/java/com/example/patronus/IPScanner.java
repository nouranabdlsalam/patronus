package com.example.patronus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IPScanner {

    private final Context context;

    public IPScanner(Context context) {
        this.context = context;
    }

    // Only return USER apps with UID >= 10000
    public ArrayList<App> getUserApps() {
        ArrayList<App> userApps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
//
//        ArrayList<String> browsers = new ArrayList<>();
//        browsers.add("com.android.chrome");
//        browsers.add("com.google.android.googlequicksearchbox");
//        browsers.add("org.mozilla.firefox");
//        browsers.add("com.opera.browser.afin");
//        browsers.add("com.brave.browser");

        for (ApplicationInfo app : apps) {
//            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
//            if (app.uid < 10000) continue;  // skip system-level UIDs

            App current = new App(pm.getNameForUid(app.uid), app.loadLabel(pm).toString());

//            if (browsers.contains(app.packageName)){
//                current.browser = true;
//            }

            current.setUid(app.uid);
            userApps.add(current);
        }

        return userApps;
    }

    public ArrayList<App> getAppTrafficStats(ArrayList<App> userApps) {

        for (App app : userApps) {
            int uid = app.getUid();
            String pkg = app.getPackageName();

            long txBytes = TrafficStats.getUidTxBytes(uid);
            long rxBytes = TrafficStats.getUidRxBytes(uid);

            if (txBytes == TrafficStats.UNSUPPORTED || rxBytes == TrafficStats.UNSUPPORTED) continue;

            app.setTxBytes(txBytes);
            app.setRxBytes(rxBytes);

            System.out.println("📦 App: " + pkg + " | UID: " + uid);
            System.out.println("   ↳ Sent: " + txBytes + " bytes | Received: " + rxBytes + " bytes");
        }

        return  userApps;
    }

    public ArrayList<App> getConnections(String protocol, ArrayList<App> userApps) {
        String file = "/proc/net/" + protocol;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] fields = line.trim().split("\\s+");
                if (fields.length < 10) continue;

                String localAddressHex = fields[1];
                String remoteAddressHex = fields[2];
                int uid = Integer.parseInt(fields[7]);

//                if (!uidToPkg.containsKey(uid)) continue; // only user apps
                App matchingApp = null;
                for (App app : userApps) {
                    if (app.getUid() == uid) {
                        matchingApp = app;
                        break;
                    }
                }
                if (matchingApp == null) continue;

                String localIpPort = parseIpPort(localAddressHex);
//                String remoteIpPort = parseIpPort(remoteAddressHex);
                String remoteIp = parseIp(remoteAddressHex);
                if (remoteIp.equals("0.0.0.0")) continue; // skip junk connections

                String appName = matchingApp.getName();

                if (!matchingApp.getIPs().contains(remoteIp)){
                    matchingApp.IPs.add(remoteIp);
                }

                System.out.println("🔌 " + protocol.toUpperCase() + " Connection:");
                System.out.println("   ↳ App: " + appName + " | UID: " + uid);
                System.out.println("   ↳ Local: " + localIpPort + " | Remote: " + remoteIp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userApps;
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

    private String parseIp(String hex) {
        String ipHex = hex.split(":")[0];
        return hexToIp(ipHex);
    }

    public ArrayList<App> getAllConnections() {
        ArrayList<App> userApps = getUserApps();
        getAppTrafficStats(userApps);
        getConnections("tcp", userApps);
        getConnections("udp", userApps);

        for (App app : userApps) {
            if (!app.getIPs().isEmpty()){
            System.out.println("App: " + app.getPackageName());
            System.out.println("Tx: " + app.getTxBytes() + " | Rx: " + app.getRxBytes());
            System.out.println("Connections: ");
            for (String ip: app.getIPs()) {
                System.out.print(ip + " , ");
            }
            System.out.println("\n -----------------------------------------------------");
            }
        }

        return userApps;
    }

    public ArrayList<App> scanConnections(ArrayList<App> userApps){
        ArrayList<App> maliciousApps = new ArrayList<>();
        IPDatabase db = IPDatabase.getInstance(context);
        IPDao ipDao = db.ipDao();

//        MaliciousIP maliciousIP = new MaliciousIP("104.199.65.9");
//        ipDao.insertIP(maliciousIP);

//        MaliciousIP maliciousIP = new MaliciousIP("192.168.1.36");
//        ipDao.insertIP(maliciousIP);

//        MaliciousIP maliciousIP = new MaliciousIP("142.251.37.202");
//        ipDao.insertIP(maliciousIP); ------> fix. (SelectAppsScreen?)

        Log.d("Malicious Check", "192.168.1.36 malicious? " + ipDao.isMalicious("192.168.1.36"));

        for (App app : userApps) {
            if (!app.getIPs().isEmpty()){
                for (String ip: app.getIPs()) {
                    boolean isMalicious = ipDao.isMalicious(ip);
                    if (isMalicious){
                        app.maliciousIPs.add(ip);
                        if (!maliciousApps.contains(app)){
                            maliciousApps.add(app);
                        }
                        Log.d("NETWORK MONITOR ALERT", app.getName() + " communicates with a suspicious IP " + ip);
                        Log.d("Browser Check", app.getName() + " is a browser? " + app.browser);
                    }
                }
            }
        }
        Log.d("malicious apps", "count: " + maliciousApps.size());
        return maliciousApps;
    }

}

