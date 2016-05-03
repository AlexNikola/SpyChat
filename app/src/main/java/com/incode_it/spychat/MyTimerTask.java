package com.incode_it.spychat;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    private Handler handler;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Calendar calendar = Calendar.getInstance();
    public boolean isRunning;
    private TextView textView;

    public MyTimerTask(long removalTime, TextView textView) {
        this.textView = textView;

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long currentTime = System.currentTimeMillis();

        calendar.setTimeInMillis(removalTime - currentTime);
        handler = new Handler();
    }

    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String date = sdf.format(calendar.getTime());
                textView.setText(date);
                if (date.equals("00:00:00")) {
                    cancel();
                    isRunning = false;
                }
                calendar.add(Calendar.SECOND, -1);
            }
        });
    }
}