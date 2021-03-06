package com.incode_it.spychat.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.incode_it.spychat.C;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "myserv";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            startIndividualAlarms(context, intent);
            startGlobalAlarm(context, intent);
        }
    }

    private void startIndividualAlarms(Context context, Intent intent)
    {
        ArrayList<TimeHolder> timeHolderArrayList = MyDbHelper.readTime(new MyDbHelper(context).getReadableDatabase(), context);
        for (TimeHolder timeHolder: timeHolderArrayList)
        {
            if (timeHolder.individualRemovalTime != 0)
            {
                AlarmReceiverIndividual receiverIndividual = new AlarmReceiverIndividual();
                receiverIndividual.setAlarm(context, timeHolder.individualRemovalTime, timeHolder.mId);
            }
        }
    }

    private void startGlobalAlarm(Context context, Intent intent)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long timer = sharedPreferences.getLong(C.GLOBAL_TIMER, 0);
        long removalTime = sharedPreferences.getLong(C.REMOVAL_GLOBAL_TIME, 0);
        if (removalTime == 0) return;
        AlarmReceiverGlobal alarm = new AlarmReceiverGlobal();
        alarm.setAlarm(context, removalTime, timer);
    }
}