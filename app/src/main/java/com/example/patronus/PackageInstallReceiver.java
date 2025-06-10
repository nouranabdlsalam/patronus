package com.example.patronus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class PackageInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        if (data == null) return;

        String packageName = data.getSchemeSpecificPart();
        if (packageName == null) return;

        String msg = "The app: ";

        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            msg = "The app you just installed: ";
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            msg = "The app you just updated: ";
        }

        AppScanner scanner = new AppScanner(context);
        int classification = -1;
        try {
            classification = scanner.scanApp(packageName);
            if (classification==1){
                ArrayList<App> maliciousApps = new ArrayList<>();
                PackageManager packageManager = context.getPackageManager();
                List<ApplicationInfo> InstalledAppsPm = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

                for (ApplicationInfo app: InstalledAppsPm){
                    if (app.packageName.equals(packageName)){
                        App maliciousApp  = new App(app.packageName, app.loadLabel(packageManager).toString());
                        maliciousApp.setIcon(2);
                        maliciousApps.add(maliciousApp);
                        break;
                    }
                }

                showThreatNotification(context, maliciousApps, (msg + maliciousApps.get(0).getName()));

                Log.d("BGSCAN", "App " + packageName + " is Malicious");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void showThreatNotification(Context context, ArrayList<App> maliciousApps, String msg) {
        Intent intent = new Intent(context, MaliciousAppsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putParcelableArrayListExtra("malwareApps", maliciousApps);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "WiFiMonitorChannel")
                .setSmallIcon(R.drawable.warning) // Replace with your actual icon
                .setContentTitle("⚠️ Malicious App Detected")
                .setContentText(msg + maliciousApps.get(0).packageName + " might be malicious. tap to view details.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
