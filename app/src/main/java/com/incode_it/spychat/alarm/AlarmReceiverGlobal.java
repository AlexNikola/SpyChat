package com.incode_it.spychat.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.incode_it.spychat.C;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.util.ArrayList;

public class AlarmReceiverGlobal extends WakefulBroadcastReceiver
{
    private static final String TAG = "myserv";

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean hasId = deleteMessages(context);
        if (!hasId) return;
        Intent serviceIntent = new Intent(context, UpdateUIService.class);
        startWakefulService(context, serviceIntent);
    }

    private boolean deleteMessages(Context context)
    {
        boolean hasId = false;

        ArrayList<TimeHolder> timeHolderArrayList = MyDbHelper.readTime(new MyDbHelper(context).getReadableDatabase());
        for (TimeHolder timeHolder: timeHolderArrayList)
        {
            if (timeHolder.removalTime == 0)
            {
                hasId = true;
                MyDbHelper.removeMessage(new MyDbHelper(context).getWritableDatabase(), timeHolder.mId);
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putLong(C.REMOVAL_GLOBAL_TIME, 0).apply();

        return hasId;
    }

    public void setAlarm(Context context, long removalTime) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiverGlobal.class);
        int requestCode = (int) removalTime;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

        alarmMgr.set(AlarmManager.RTC_WAKEUP, removalTime, alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        /*ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Log.d(TAG, "AlarmReceiverGlobal setAlarm");*/
    }

    public void cancelAlarm(Context context, long removalTime) {
        int requestCode = (int) removalTime;
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiverGlobal.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        alarmMgr.cancel(pendingIntent);

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        /*ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);*/
    }
}
