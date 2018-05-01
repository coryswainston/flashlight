package com.coryswainston.flashlight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "CREATED");

        if (getCameraPermissions()) {
            startFlashlightService();
        }

        setContentView(R.layout.activity_main);
    }

    private boolean getCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Entered permissions function");
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECEIVE_BOOT_COMPLETED};
            ActivityCompat.requestPermissions(this, permissions, 1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "Entered request permissions result");

        boolean allPermissionsBeenGot = true;

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions not granted");
                Log.d(TAG, "Result " + grantResults[i]);
                allPermissionsBeenGot = false;
            }
        }

        if (allPermissionsBeenGot) {
            startFlashlightService();
        }
    }

    private void startFlashlightService() {
        Intent intent = new Intent(this, FlashlightService.class);
        startService(intent);
        Toast.makeText(this, "Shake to turn on flashlight.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
