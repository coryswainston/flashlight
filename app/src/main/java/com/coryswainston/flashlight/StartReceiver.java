package com.coryswainston.flashlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

/**
 * Starts flashlight service on device start
 */

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FlashlightService.class);

        SharedPreferences sharedPreferences = context.getSharedPreferences("shakelight", 0);
        if (sharedPreferences.getBoolean("activated", false)) {
            ContextCompat.startForegroundService(context, i);
        }
    }
}
