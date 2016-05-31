package com.incode_it.spychat.pin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.incode_it.spychat.C;

public class UserPresentBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Sent when the user is present after
         * device wakes up (e.g when the keyguard is gone)
         * */
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isPinOn = sharedPreferences.getBoolean(C.SETTING_PIN, false);
            if (isPinOn)
            {
                /*Intent i = new Intent(QuickstartPreferences.SHOW_PIN);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);*/
                Intent serviceIntent = new Intent(context, ShowPinService.class);
                startWakefulService(context, serviceIntent);
            }
        }
        /*Device is shutting down. This is broadcast when the device
         * is being shut down (completely turned off, not sleeping)
         * */
        else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {

        }
    }

}