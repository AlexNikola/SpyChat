package com.incode_it.spychat;

import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TimerTask;

public class MyGlobalTimerTask extends TimerTask {

    private Handler handler;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Calendar calendar = Calendar.getInstance();
    public boolean isRunning;
    private TextView textView;
    private long timer;
    private long removalTime;
    private long currentTime;

    public MyGlobalTimerTask(long removalTime, TextView textView, long timer) {
        this.textView = textView;
        this.timer = timer;
        this.removalTime = removalTime;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        currentTime = System.currentTimeMillis();
        calendar.setTimeInMillis(removalTime - currentTime);
        handler = new Handler();
    }

    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String date = sdf.format(calendar.getTime());
                if (removalTime < currentTime )
                {
                    textView.setText("00:00:00");
                }
                else
                {
                    textView.setText(date);
                }


                //Log.d("timmmer", "date " + date);
                if (date.equals("00:00:00")) {
                    calendar.setTimeInMillis(timer);
                }
                calendar.add(Calendar.SECOND, -1);
            }
        });
    }
}
