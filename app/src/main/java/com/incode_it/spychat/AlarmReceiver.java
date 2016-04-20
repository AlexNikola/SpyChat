package com.incode_it.spychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // For our recurring task, we'll just display a message
        Log.d("myalarm", "onReceive "+context.hashCode() + " " + intent.hashCode());
        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();

    }
}