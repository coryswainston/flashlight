package com.coryswainston.flashlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Camera camera;
    Camera.Parameters params;
    boolean on = false;

    SensorManager sensorManager;
    SensorEvent trigger;
    private static final int THRESHOLD = 60;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);

        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View v) {
        Log.d(TAG, "Entered click function");
        if (getCameraPermissions()) {
            toggleFlashlight();
        }
    }

    public void toggleFlashlight() {
        if (on) {
            if (camera != null) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                camera.stopPreview();
                on = false;
            }
        } else {
            try {
                camera = Camera.open();
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.setPreviewTexture(new SurfaceTexture(0));
                camera.startPreview();
                on = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Log.d(TAG, "Event: " + sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2]);

        if (getSumAcceleration(sensorEvent) > THRESHOLD) {
            if (trigger == null) {
                trigger = sensorEvent;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (sensorEvent.timestamp - trigger.timestamp > 1000) {
                    trigger = null;
                } else {
                    toggleFlashlight();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private float getSumAcceleration(SensorEvent e) {
        float sumAcceleration = 0;
        for (int i = 0; i < 3; i++) {
            sumAcceleration += Math.abs(e.values[i]);
        }

        return sumAcceleration - SensorManager.GRAVITY_EARTH;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // ignored for now
    }

    private boolean getCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Entered permissions function");
            String[] permissions = {Manifest.permission.CAMERA};
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
            toggleFlashlight();
        }
    }
}
