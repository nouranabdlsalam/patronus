package com.example.patronus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class App implements Parcelable {
    String packageName, name;
    int [] features;
    int icon, uid;
    ArrayList<String> IPs;

    ArrayList<String> maliciousIPs;
    boolean browser;

    long txBytes, rxBytes;
    public App(String packageName, String name){
        this.packageName = packageName;
        this.features = new int[28];
        this.name = name;
        this.IPs = new ArrayList<>();
        this.maliciousIPs = new ArrayList<>();
    }

    public ArrayList<String> getMaliciousIPs() {
        return maliciousIPs;
    }

    public void setMaliciousIPs(ArrayList<String> maliciousIPs) {
        this.maliciousIPs = maliciousIPs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int[] getFeatures() {
        return features;
    }

    public void setFeatures(int[] features) {
        this.features = features;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public ArrayList<String> getIPs() {
        return IPs;
    }

    public void setIPs(ArrayList<String> IPs) {
        this.IPs = IPs;
    }

    public long getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(long txBytes) {
        this.txBytes = txBytes;
    }

    public long getRxBytes() {
        return rxBytes;
    }

    public void setRxBytes(long rxBytes) {
        this.rxBytes = rxBytes;
    }

    // Parcelable methods
    protected App(Parcel in) {
        packageName = in.readString();
        features = in.createIntArray();
        name = in.readString();
        icon = in.readInt();
        IPs = in.createStringArrayList();
        maliciousIPs = in.createStringArrayList();
        browser = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeIntArray(features);
        dest.writeString(name);
        dest.writeInt(icon);
        dest.writeStringList(IPs);
        dest.writeStringList(maliciousIPs);
        dest.writeByte((byte) (browser ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };

    public Drawable getAppIcon(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return ContextCompat.getDrawable(context, R.drawable.wifi_icon); // fallback icon
        }
    }
}
