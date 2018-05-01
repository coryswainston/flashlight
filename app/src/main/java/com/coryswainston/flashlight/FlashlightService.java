package com.coryswainston.flashlight;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Toggles flashlight on device shake
 */

public class FlashlightService extends Service implements SensorEventListener {

    Camera camera;
    Camera.Parameters params;
    boolean on = false;

    SensorManager sensorManager;
    SensorEvent trigger;
    private static final int THRESHOLD = 60;

    long lastToggleTimestamp;

    private static final String TAG = "FlashlightService";

    @Override
    public void onCreate() {
        Log.d(TAG, "CREATED");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void toggleFlashlight() {
        if (on) {
            if (camera != null) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                try {
                    camera.setParameters(params);
                    camera.stopPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                on = false;
            }
        } else {
            try {
                if (camera == null) {
                    camera = Camera.open();
                    camera.setPreviewTexture(new SurfaceTexture(0));
                }
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
                on = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (getSumAcceleration(sensorEvent) > THRESHOLD) {
            if (trigger == null) {
                trigger = sensorEvent;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (sensorEvent.timestamp - trigger.timestamp > 1000 || sensorEvent.timestamp - lastToggleTimestamp < 1000) {
                    trigger = null;
                } else {
                    toggleFlashlight();
                    lastToggleTimestamp = sensorEvent.timestamp;
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
        // ignore for now
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "STARTED");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "BOUND");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "DESTROYED");
        super.onDestroy();
    }
}
