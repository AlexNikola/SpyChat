package com.incode_it.spychat.alarm;


import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.incode_it.spychat.C;
import com.incode_it.spychat.QuickstartPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateUIService extends IntentService
{
    private static final String TAG = "myserv";

    public UpdateUIService() {
        super("UpdateUIService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Intent i = new Intent(QuickstartPreferences.DELETE_MESSAGES);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
