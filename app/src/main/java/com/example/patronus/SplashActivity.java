package com.example.patronus;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_splash);
        super.onCreate(savedInstanceState);

        // Delay 2 seconds then start MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));
            finish();
        }, 2000); // 2000ms = 2 seconds
    }
}
