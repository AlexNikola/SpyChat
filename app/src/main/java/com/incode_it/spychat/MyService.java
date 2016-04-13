package com.incode_it.spychat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

public class MyService extends Service {

    static final String LOG_TAG = "myLogs";

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate "+this.hashCode());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        Log.d(LOG_TAG, "Intent "+ intent.hashCode());
        Log.d(LOG_TAG, "flags "+ flags);
        someTask();
        return START_REDELIVER_INTENT;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(LOG_TAG, "onTaskRemoved");

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return new Binder();
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");
        return true;
    }

    void someTask() {
        new CountDownTimer(500000, 1000)
        {
            public void onTick(long millisUntilFinished) {
                Log.d(LOG_TAG, "seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                stopSelf();
                Log.d(LOG_TAG, "done!");
            }
        }.start();
    }
}
