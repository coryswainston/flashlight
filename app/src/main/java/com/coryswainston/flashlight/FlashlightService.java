package com.coryswainston.flashlight;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Arrays;

/**
 * Toggles flashlight on device shake
 */

public class FlashlightService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "shakelight-channel-1";
    private static final String TAG = "FlashlightService";
    private static final String SHAKELIGHT = "shakelight";

    private Camera camera;
    private Camera.Parameters params;
    private SensorManager sensorManager;

    private boolean on = false;
    private long lastToggleTimestamp;
    private int threshold;

    @Override
    public void onCreate() {
        Log.d(TAG, "CREATED");

        NotificationChannel channel;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent homeIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(homeIntent);
        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Shakelight is running")
                .setContentText("Tap to change settings.")
                .setColor(getResources().getColor(R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        startForeground(1, builder.build());

        threshold = getSharedPreferences(SHAKELIGHT, 0).getInt("threshold", 60);

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
                    camera.release();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage() + Arrays.asList(e.getStackTrace()).toString());
                    on = false;
                }
                on = false;
            }
        } else {
            try {
                Log.d(TAG, "OPENING CAM");
                camera = Camera.open();
                camera.setPreviewTexture(new SurfaceTexture(0));
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
                Log.d(TAG, "should have turned on");
                on = true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + Arrays.asList(e.getStackTrace()).toString());
                retry();
            }
        }
    }

    private void retry() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        toggleFlashlight();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (getSumAcceleration(sensorEvent) > threshold) {
            Log.d(TAG, "Threshold is: " + threshold);
            if (sensorEvent.timestamp - lastToggleTimestamp > 1000000000 /* one second in nanos */) {
                Log.d(TAG, "This time: " + sensorEvent.timestamp + ", Last time: " + lastToggleTimestamp);
                lastToggleTimestamp = sensorEvent.timestamp;
                toggleFlashlight();
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
        if (camera != null) {
            camera.release();
        }
        sensorManager.unregisterListener(this);
        on = false;
        Log.d(TAG, "DESTROYED");
    }


}
