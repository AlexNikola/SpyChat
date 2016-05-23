package com.incode_it.spychat.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.io.File;
import java.util.ArrayList;

public class AlarmReceiverIndividual extends WakefulBroadcastReceiver
{
    private static final String TAG = "myserv";

    @Override
    public void onReceive(Context context, Intent intent) {

        int mId = intent.getIntExtra(C.ID_TO_DELETE, 0);
        Log.d(TAG, "onReceive mId "+mId);
        Message message = MyDbHelper.readMessage(new MyDbHelper(context).getReadableDatabase(), mId);

        if (message.messageType != Message.MY_MESSAGE_TEXT && message.messageType != Message.NOT_MY_MESSAGE_TEXT)
        {
            File file = new File(message.getMessage());
            file.delete();
        }
        MyDbHelper.removeMessage(new MyDbHelper(context).getWritableDatabase(), mId);

        Intent serviceIntent = new Intent(context, UpdateUIService.class);
        serviceIntent.putExtra(C.ID_TO_DELETE, mId);
        startWakefulService(context, serviceIntent);


    }


    public void setAlarm(Context context, long removalTime, int mId) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiverIndividual.class);
        intent.putExtra(C.ID_TO_DELETE, mId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, mId, intent, 0);
        Log.d(TAG, "setAlarm mId "+mId);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, removalTime, alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        /*ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);*/
    }

    public void cancelAlarm(Context context, int mId) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiverIndividual.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, mId, intent, 0);
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