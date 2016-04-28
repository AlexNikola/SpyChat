package com.incode_it.spychat.alarm;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd_HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        Log.d(TAG, "UpdateUIService onHandleIntent "+currentDateAndTime);*/
    }
}
