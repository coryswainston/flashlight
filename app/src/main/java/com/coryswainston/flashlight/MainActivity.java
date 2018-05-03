package com.coryswainston.flashlight;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String SHAKELIGHT = "shakelight";
    private static final String ACTIVATED = "activated";
    private static final String THRESHOLD = "threshold";

    private Switch mSwitch;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSwitch = findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateFlashlightActivated(b);
                if (b && getCameraPermissions()) {
                    startFlashlightService();
                } else {
                    stopFlashlightService();
                }
            }
        });
        mSwitch.setChecked(getFlashlightActivated());

        mSeekBar = findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateThreshold(i);
                stopFlashlightService();
                if (getCameraPermissions()) {
                    startFlashlightService();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ignore
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ignore
            }
        });
        mSeekBar.setProgress(getThreshold());
    }

    private boolean getFlashlightActivated() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHAKELIGHT, 0);

        return sharedPreferences.getBoolean(ACTIVATED, true);
    }

    private void updateFlashlightActivated(boolean b) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHAKELIGHT, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(ACTIVATED, b);
        editor.apply();
    }

    private int getThreshold() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHAKELIGHT, 0);

        int threshold = sharedPreferences.getInt(THRESHOLD, 60);
        return (6 - threshold / 15);
    }

    private void updateThreshold(int n) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHAKELIGHT, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int threshold = -15 * (n - 6);

        editor.putInt(THRESHOLD, threshold);
        editor.apply();
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
    }

    private void stopFlashlightService() {
        Intent intent = new Intent(this, FlashlightService.class);
        stopService(intent);

    }
}
