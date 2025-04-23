package com.example.patronus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.patronus.ml.HybridMalwareModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppScanner {
    Context context;
    PackageManager packageManager;

    public AppScanner(Context context){
        this.context = context;
    }


    protected int scanApp(App app) throws PackageManager.NameNotFoundException {
        Log.d("SCANNING:", app.getFeatures()[0] + " ");
        app.setFeatures(extractFeatures(app));
        Log.d("SCANNING:", "set features for: " + app.getPackageName());
        double modelConfidence = classify(app);
        int category = getAppCategory(app);
        int sideloadingScore = getSideloadingScore(app);
        int contextClassification = getContextAwareScore(modelConfidence, category, sideloadingScore);
        return contextClassification;
    }

    protected int scanApp(String packageName) throws PackageManager.NameNotFoundException {
        packageManager = context.getPackageManager();
        List<ApplicationInfo> InstalledAppsPm = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        String appName = "";
        for (ApplicationInfo app: InstalledAppsPm) {
            if (app.packageName.equals(packageName)){
                appName = app.loadLabel(packageManager).toString();
                break;
            }
        }
        return scanApp(new App(packageName, appName));
    }

    protected int[] extractFeatures(App app) throws PackageManager.NameNotFoundException {
        int [] features = new int[28];
        packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
        // Get all permissions
        String[] permissions = packageInfo.requestedPermissions;

        // Create a list to hold just the permission names
        List<String> permissionNames = new ArrayList<>();

        // Loop through all permissions
        if (permissions != null) {
            for (String permission : permissions) {
                // Split the permission string by "." and get the last part
                String[] permissionParts = permission.split("\\.");
                String permissionName = permissionParts[permissionParts.length - 1]; // Last part is the actual permission
                permissionNames.add(permissionName);
            }
        }

        List<String> modelPermissions = Arrays.asList(
                //          0                   1                       2
                "WRITE_EXTERNAL_STORAGE", "READ_PHONE_STATE", "KILL_BACKGROUND_PROCESSES",
                //      3             4            5                     6                   7
                "READ_CONTACTS", "SEND_SMS", "RECEIVE_SMS", "RECEIVE_BOOT_COMPLETED", "READ_EXTERNAL_STORAGE",
                //     8            9               10           11          12              13
                "READ_SMS", "READ_CALL_LOG", "RECORD_AUDIO", "CAMERA", "INTERNET", "ACCESS_NETWORK_STATE"
        );

        int index = 0;

        for (String modelPermission: modelPermissions) {
//            Log.d("model permission", modelPermission + " " + permissionNames.contains(modelPermission));
            if(permissionNames.contains(modelPermission)){
                features[index++] = 1;
            }
            else {
                features[index++] = 0;
            }
        }

        int WAKE_LOCK = 0;
        int ACCESS_LOCATION_EXTRA_COMMANDS = 0;
        int ACCESS_WIFI_STATE = 0;

        if (permissionNames.contains("WAKE_LOCK")) WAKE_LOCK = 1;
        if (permissionNames.contains("ACCESS_LOCATION_EXTRA_COMMANDS")) ACCESS_LOCATION_EXTRA_COMMANDS = 1;
        if (permissionNames.contains("ACCESS_WIFI_STATE")) ACCESS_WIFI_STATE = 1;

        //Derived Features
        // persistence_basic: (RECEIVE_BOOT_COMPLETED == 1) && (KILL_BACKGROUND_PROCESSES == 1)
        features[14] = (features[6] == 1 && features[2] == 1) ? 1 : 0;
        // persistence_extended: (RECEIVE_BOOT_COMPLETED + KILL_BACKGROUND_PROCESSES + WAKE_LOCK == 1)
        features[15] = (features[6] + features[2] + WAKE_LOCK);
        // c2_sms_exfiltration: (SEND_SMS + INTERNET + READ_PHONE_STATE + ACCESS_NETWORK_STATE)
        features[16] = (features[4] + features[12] + features[1] + features[13]);
        // c2_persistence: (WAKE_LOCK==1) && (INTERNET==1)
        features[17] = (WAKE_LOCK == 1 && features[12] == 1) ? 1 : 0;
        // c2_exfil_with_storage: (WAKE_LOCK + INTERNET + READ_EXTERNAL_STORAGE)
        features[18] = (WAKE_LOCK + features[12] + features[7]);
        // spyware_file_access: (CAMERA == 1) && (ACCESS_LOCATION_EXTRA_COMMANDS == 1)
        features[19] = (features[11] == 1 && ACCESS_LOCATION_EXTRA_COMMANDS == 1) ? 1 : 0;
        // spyware_privacy_invasion: (CAMERA + RECORD_AUDIO + SEND_SMS)
        features[20] = (features[11] + features[10] + features[4]);
        // sms_worm_pattern: (SEND_SMS + READ_SMS + READ_CONTACTS)
        features[21] = (features[4] + features[8] + features[3]);
        // ransomware_core: (READ_EXTERNAL_STORAGE == 1) && (WRITE_EXTERNAL_STORAGE == 1)
        features[22] = (features[7] + features[0]);
        // ransomware_extended: (READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE + INTERNET + WAKE_LOCK + SEND_SMS)
        features[23] = (features[7] + features[0] + features[12] + WAKE_LOCK + features[4]);
        // file_access_count: (READ_CALL_LOG + READ_EXTERNAL_STORAGE + READ_CONTACTS + READ_SMS)
        features[24] = (features[9] + features[7] + features[3] + features[8]);
        // network_permissions: (INTERNET + ACCESS_NETWORK_STATE + ACCESS_WIFI_STATE + READ_PHONE_STATE)
        features[25] = (features[12] + features[13] + ACCESS_WIFI_STATE + features[1]);
        // malware_behavior_class_count: (persistence_extended + c2_sms_exfiltration + spyware_privacy_invasion +
        //              sms_worm_pattern + ransomware_extended) --> all binaries
        features[26] = ((features[15] > 0 ? 1 : 0) + ( (features[16] > 0) ? 1 : 0) + ( (features[20] > 0) ? 1 : 0)
                + ( (features[21] > 0) ? 1 : 0) + ( (features[23] > 0) ? 1 : 0));
        // permission count (out of 114 permissions)
        List<String> totalPermissions = Arrays.asList(
                "SEND_SMS", "READ_PHONE_STATE", "GET_ACCOUNTS", "RECEIVE_SMS",
                "READ_SMS", "USE_CREDENTIALS", "MANAGE_ACCOUNTS", "WRITE_SMS",
                "READ_SYNC_SETTINGS", "AUTHENTICATE_ACCOUNTS",
                "WRITE_HISTORY_BOOKMARKS", "INSTALL_PACKAGES", "CAMERA",
                "WRITE_SYNC_SETTINGS", "READ_HISTORY_BOOKMARKS", "INTERNET",
                "RECORD_AUDIO", "NFC", "ACCESS_LOCATION_EXTRA_COMMANDS",
                "WRITE_APN_SETTINGS", "BIND_REMOTEVIEWS", "READ_PROFILE",
                "MODIFY_AUDIO_SETTINGS", "READ_SYNC_STATS", "BROADCAST_STICKY",
                "WAKE_LOCK", "RECEIVE_BOOT_COMPLETED", "RESTART_PACKAGES",
                "BLUETOOTH", "READ_CALENDAR", "READ_CALL_LOG",
                "SUBSCRIBED_FEEDS_WRITE", "READ_EXTERNAL_STORAGE", "VIBRATE",
                "ACCESS_NETWORK_STATE", "SUBSCRIBED_FEEDS_READ",
                "CHANGE_WIFI_MULTICAST_STATE", "WRITE_CALENDAR", "MASTER_CLEAR",
                "UPDATE_DEVICE_STATS", "WRITE_CALL_LOG", "DELETE_PACKAGES",
                "GET_TASKS", "GLOBAL_SEARCH", "DELETE_CACHE_FILES",
                "WRITE_USER_DICTIONARY", "REORDER_TASKS", "WRITE_PROFILE",
                "SET_WALLPAPER", "BIND_INPUT_METHOD", "READ_SOCIAL_STREAM",
                "READ_USER_DICTIONARY", "PROCESS_OUTGOING_CALLS",
                "CALL_PRIVILEGED", "BIND_WALLPAPER", "RECEIVE_WAP_PUSH", "DUMP",
                "BATTERY_STATS", "ACCESS_COARSE_LOCATION", "SET_TIME",
                "WRITE_SOCIAL_STREAM", "WRITE_SETTINGS", "REBOOT",
                "BLUETOOTH_ADMIN", "BIND_DEVICE_ADMIN", "WRITE_GSERVICES",
                "KILL_BACKGROUND_PROCESSES", "STATUS_BAR", "PERSISTENT_ACTIVITY",
                "CHANGE_NETWORK_STATE", "RECEIVE_MMS", "SET_TIME_ZONE",
                "CONTROL_LOCATION_UPDATES", "BROADCAST_WAP_PUSH",
                "BIND_ACCESSIBILITY_SERVICE", "ADD_VOICEMAIL", "CALL_PHONE",
                "BIND_APPWIDGET", "FLASHLIGHT", "READ_LOGS", "SET_PROCESS_LIMIT",
                "MOUNT_UNMOUNT_FILESYSTEMS", "BIND_TEXT_SERVICE",
                "INSTALL_LOCATION_PROVIDER", "SYSTEM_ALERT_WINDOW",
                "MOUNT_FORMAT_FILESYSTEMS", "CHANGE_CONFIGURATION",
                "CLEAR_APP_USER_DATA", "CHANGE_WIFI_STATE", "READ_FRAME_BUFFER",
                "ACCESS_SURFACE_FLINGER", "BROADCAST_SMS", "EXPAND_STATUS_BAR",
                "INTERNAL_SYSTEM_WINDOW", "SET_ACTIVITY_WATCHER", "WRITE_CONTACTS",
                "BIND_VPN_SERVICE", "DISABLE_KEYGUARD", "ACCESS_MOCK_LOCATION",
                "GET_PACKAGE_SIZE", "MODIFY_PHONE_STATE",
                "CHANGE_COMPONENT_ENABLED_STATE", "CLEAR_APP_CACHE",
                "SET_ORIENTATION", "READ_CONTACTS", "DEVICE_POWER",
                "HARDWARE_TEST", "ACCESS_WIFI_STATE", "WRITE_EXTERNAL_STORAGE",
                "ACCESS_FINE_LOCATION", "SET_WALLPAPER_HINTS",
                "SET_PREFERRED_APPLICATIONS", "WRITE_SECURE_SETTINGS"
        );

        int permissionCount = 0;
        for (String permission: permissionNames) {
            if(totalPermissions.contains(permission)) permissionCount++;

        }
        features[27] = permissionCount;

        // Log the extracted permission names
        Log.d("Perm Check", permissionNames.toString());
        Log.d("Perm Check2", Arrays.toString(features));
        return features;
    }

    protected double classify(App app){
        double classification = -1;
        try {
            HybridMalwareModel model = HybridMalwareModel.newInstance(context);

            int[] features = app.features;
            float[] featureFloats = new float[features.length];

            for (int i = 0; i < features.length; i++) {
                featureFloats[i] = (float) features[i];  // Convert each feature to a float
            }

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(featureFloats.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            for (float feature : featureFloats) {
                byteBuffer.putFloat(feature);  // Put each float into the ByteBuffer
            }
            byteBuffer.rewind();

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 28}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            HybridMalwareModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] outputArray = outputFeature0.getFloatArray();

//            if (outputArray[0] == 1){
//                classification = 1;
//            }
//            else {
//                classification = 0;
//            }

            classification = outputArray[0]; //context trial

            Log.d("Classification", app.getPackageName() + ": " + classification);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return classification;
    }

    public int getContextAwareScore(double modelConfidence, int category, int sideloadingScore){
        double categoryRisk = getCategoryRisk(category);
        double contextAwareScore = 0.6 * modelConfidence + 0.3 * categoryRisk + 0.1 * sideloadingScore;
        Log.d("CONTEXT", "model confidence: " + modelConfidence + ", category: " + category + ", sideloading score: " + sideloadingScore + ", context score: " + contextAwareScore);
        return contextAwareScore > 0.7 ? 1 : 0;
    }

    public int getAppCategory(App app) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(app.getPackageName(), 0);
        return appInfo.category;
    }

    public int getSideloadingScore(App app){
        String installer = context.getPackageManager().getInstallerPackageName(app.getPackageName());
        int sideloadingScore;
        if (installer == null || !installer.equals("com.android.vending")) {
            sideloadingScore = 1;
        } else {
            sideloadingScore = -1;
        }
        return sideloadingScore;
    }

    public double getCategoryRisk(int category){
        double categoryRisk;
        switch (category){
            case 8: // Category for apps which are primarily accessibility apps, such as screen-readers. (src: Android Docs)
                categoryRisk = 0.7;
                break;
            case 1: // Category for apps which primarily work with audio or music, such as music players.
                categoryRisk = 0.2;
                break;
            case 0: // Category for apps which are primarily games.
                categoryRisk = 0.6;
                break;
            case 3: // Category for apps which primarily work with images or photos, such as camera or gallery apps.
                categoryRisk = 0.1;
                break;
            case 6: // Category for apps which are primarily maps apps, such as navigation apps.
                categoryRisk = 0.4;
                break;
            case 5: // Category for apps which are primarily news apps, such as newspapers, magazines, or sports apps.
                categoryRisk = 0.3;
                break;
            case 7: // Category for apps which are primarily productivity apps, such as cloud storage or workplace apps.
                categoryRisk = 0.3;
                break;
            case 4: // Category for apps which are primarily social apps, such as messaging, communication, email, or social network apps.
                categoryRisk = 0.2;
                break;
            case -1: // Value when category is undefined.
                categoryRisk = 0.8;
                break;
            case 2: // Category for apps which primarily work with video or movies, such as streaming video apps.
                categoryRisk = 0.4;
                break;
            default:
                categoryRisk = 0.5;
        }
        return  categoryRisk;
    }
}
