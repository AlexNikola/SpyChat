package com.incode_it.spychat.alarm;


import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.incode_it.spychat.C;
import com.incode_it.spychat.QuickstartPreferences;

public class UpdateUIService extends IntentService
{
    private static final String TAG = "myserv";

    public UpdateUIService() {
        super("UpdateUIService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int mId = intent.getIntExtra(C.ID_TO_DELETE, 0);
        Intent i = new Intent(QuickstartPreferences.DELETE_MESSAGES);
        i.putExtra(C.ID_TO_DELETE, mId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
