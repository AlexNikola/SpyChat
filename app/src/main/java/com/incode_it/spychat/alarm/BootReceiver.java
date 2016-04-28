package com.incode_it.spychat.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();
    private static final String TAG = "myserv";
    @Override
    public void onReceive(Context context, Intent intent) {
        //listener.startService(new Intent(listener, MyService.class));
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            Log.d(TAG, "BootReceiver onReceive");
            alarm.setAlarm(context);
        }
    }
}