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
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends WakefulBroadcastReceiver
{
    private static final String TAG = "myserv";
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        deleteMessages(context);

        Intent serviceIntent = new Intent(context, UpdateUIService.class);
        //intent.putExtra();
        startWakefulService(context, serviceIntent);
    }

    private void deleteMessages(Context context)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd_HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        Log.d(TAG, "AlarmReceiver onReceive "+currentDateAndTime);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long globalTimerAdded = sharedPreferences.getLong(C.GLOBAL_TIMER_ADDED, 0);
        long currentTime = System.currentTimeMillis();

        ArrayList<TimeHolder> timeHolderArrayList = MyDbHelper.readTime(new MyDbHelper(context).getReadableDatabase());
        long [] deletedIds = {};
        Log.d(TAG, "AlarmReceiver timeHolderArrayList size "+timeHolderArrayList.size());
        for (TimeHolder timeHolder: timeHolderArrayList)
        {
            if (timeHolder.timerType == Message.TYPE_TIMER_GLOBAL && timeHolder.messageLifeTime != 0)
            {
                if ((currentTime - timeHolder.timerAdded) > timeHolder.messageLifeTime)
                {
                    MyDbHelper.removeMessage(new MyDbHelper(context).getWritableDatabase(), timeHolder.mId);
                }
            }
        }
    }

    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.SECOND, 10);


        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 1000*60, alarmIntent);

        //alarmMgr.setRepeating();

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Log.d(TAG, "AlarmReceiver setAlarm");
    }

    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
