package com.example.patronus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch HomeScreenActivity when the app starts
        Intent intent = new Intent(MainActivity.this, Network_center.class);
        startActivity(intent);

        // Optionally finish MainActivity if you don't want the user to return to it
        finish();
    }
}
