package com.example.patronus;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;


public class NetworkMonitorService extends Service {

    private Handler handler = new Handler();
    private Runnable stopTask;
    ArrayList<App> appConnections = new ArrayList<>();
    SharedPreferencesManager sharedPreferencesManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NETWORK MONITOR", " Started Service");
        sharedPreferencesManager = new SharedPreferencesManager(getApplicationContext());

        new Thread(() -> {
            IPScanner scanner = new IPScanner(this);
//            long endTime = System.currentTimeMillis() + 30_000;

//            while (System.currentTimeMillis() < endTime) {
                if (!sharedPreferencesManager.isNetworkMonitoringOn()){
                    Log.d("NETWORK MONITOR", "Network Monitoring is OFF. Stopping Service.");
                    stopSelf();
                    return;
                }
                appConnections = scanner.getAllConnections(); // or your enhanced one

//                try {
//                    Thread.sleep(3000); // collect every 3 seconds (tweak if needed)
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            ArrayList<App> maliciousApps = scanner.scanConnections(appConnections);
            if (maliciousApps.size() > 0){
                showThreatNotification(getApplicationContext(), maliciousApps);
            }

            stopSelf(); // stop service after loop ends
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(stopTask);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showThreatNotification(Context context, ArrayList<App> maliciousApps) {
        Intent intent = new Intent(context, MaliciousAppsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        for (App app: maliciousApps) {
            app.setIcon(2);
        }
        intent.putParcelableArrayListExtra("MaliciousIP", maliciousApps);


        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "WiFiMonitorChannel")
                .setSmallIcon(R.drawable.warning) // Replace with your actual icon
                .setContentTitle("⚠️ Malicious IP Detected")
                .setContentText("We've found " + maliciousApps.size() + " network threat(s). Tap to view threat details.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1001, builder.build()); // 1001 = Notification ID
    }

}

