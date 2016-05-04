package com.incode_it.spychat.pin;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.incode_it.spychat.QuickstartPreferences;

public class ShowPinService extends IntentService
{
    private static final String TAG = "myserv";

    public ShowPinService() {
        super("ShowPinService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Intent i = new Intent(QuickstartPreferences.SHOW_PIN);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}