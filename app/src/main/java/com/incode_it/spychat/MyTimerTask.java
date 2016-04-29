package com.incode_it.spychat;

import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    private Handler handler;
    long timerAdded, timer, currentTime;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Calendar calendar = Calendar.getInstance();
    boolean isAllowed = true;
    boolean isRunning;
    TextView textView;

    public MyTimerTask(long timerAdded, long timer, TextView textView) {
        this.timerAdded = timerAdded;
        this.timer = timer;
        this.textView = textView;

        sdf .setTimeZone(TimeZone.getTimeZone("UTC"));
        currentTime = System.currentTimeMillis();

        calendar.setTimeInMillis((timerAdded + timer) - currentTime);
        handler = new Handler();

        if ((timerAdded + timer - currentTime) < 0) isAllowed = false;
    }

    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                calendar.add(Calendar.SECOND, -1);
                String date = sdf.format(calendar.getTime());
                textView.setText(date);
                if (date.equals("00:00:00")) {
                    cancel();
                    isRunning = false;

                }
            }
        });
    }
}