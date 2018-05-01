package com.coryswainston.flashlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    Camera camera;
    Camera.Parameters params;
    boolean on = false;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
