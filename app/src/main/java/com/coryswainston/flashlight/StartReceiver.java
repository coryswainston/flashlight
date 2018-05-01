package com.coryswainston.flashlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Starts flashlight service on device start
 */

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FlashlightService.class);
        context.startService(i);
    }
}
