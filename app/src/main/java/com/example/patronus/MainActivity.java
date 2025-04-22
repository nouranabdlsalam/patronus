package com.example.patronus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
>>>>>>> dev

public class MainActivity extends AppCompatActivity {
    private static final int VPN_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

<<<<<<< HEAD
        // Launch HomeScreenActivity when the app starts
        Intent intent = new Intent(MainActivity.this, Network_center.class);
        startActivity(intent);

        // Optionally finish MainActivity if you don't want the user to return to it
        finish();
    }
}
=======
        Intent prepareIntent = VpnService.prepare(this);
        if (prepareIntent != null) {
            startActivityForResult(prepareIntent, VPN_REQUEST_CODE);

        } else {
            Log.i("Main", "Requesting VPN permission...");
            startService(new Intent(this, VPN.class));
        }

        scan = findViewById(R.id.scan_test);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ScanScreenActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startService(new Intent(this, VPN.class));
        } else {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show();
        }
    }

}
>>>>>>> dev
