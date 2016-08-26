package com.incode_it.spychat.chat;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.incode_it.spychat.Message;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AudioService extends Service {
    private final IBinder audioBind = new AudioBinder();
    public MediaPlayer player;
    private Callback callback;
    private Timer timer;
    public MyTimerTask timerTask;
    public Message message;

    public AudioService() {
    }

    public void setCallback(Callback callback) {
        Log.d("dfgddddd", "AudioService setCallback: " + this.callback);
        this.callback = callback;
    }

    public void removeCallback(Message message)
    {
        if (this.message == message) {
            Log.d("dfgddddd", "AudioService removeCallback: " + callback);
            callback = null;
        } else {
            Log.d("dfgddddd", "AudioService not removeCallback: " + callback);
        }

    }

    public void playAudio(Message message) {
        Log.d("dfgddddd", "AudioService playAudio: ");
        message.state = Message.STATE_PLAYING;
        MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), message.state, message.getMessageId());

        this.message = message;
        player = new MediaPlayer();
        try {
            player.setDataSource(message.getMessage());
            player.prepare();
            player.start();
            startTimer();
            if (callback != null) callback.onStartAudio();
        } catch (IOException ignored) {
            message.state = Message.STATE_ERROR;
            MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), message.state, message.getMessageId());
            if (callback != null) callback.onError();
        }
    }

    private void startTimer()
    {
        Log.d("dfgddddd", "AudioService startTimer: ");
        timerTask = new MyTimerTask();
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }

    public void stopAudio() {
        Log.d("dfgddddd", "AudioService stopAudio: ");
        if (message != null && message.state != Message.STATE_ERROR) {
            message.state = Message.STATE_SUCCESS;
            MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), message.state, message.getMessageId());
        }

        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        if (callback != null) {
            callback.onStopAudio();
            callback = null;
        }
        stopTimer();
    }

    private void audioTimerOut() {
        Log.d("dfgddddd", "AudioService audioTimerOut: ");
        message.state = Message.STATE_SUCCESS;
        MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), message.state, message.getMessageId());
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
            if (callback != null){
                callback.onAudioTimerOut();
                callback = null;
            }
        }
        stopTimer();
    }

    private void stopTimer()
    {
        Log.d("dfgddddd", "AudioService stopTimer: ");
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }


    public class MyTimerTask extends TimerTask
    {
        public long secondsUntilFinished = player.getDuration() / 1000;
        @Override
        public void run() {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (secondsUntilFinished < 0) {
                        audioTimerOut();
                    } else {
                        if (callback != null) callback.onAudioTimerTick(secondsUntilFinished * 1000);
                        secondsUntilFinished--;
                        Log.d("dsdfsfdcc", "tick: " + secondsUntilFinished);
                    }
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("dfgddddd", "AudioService onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("dfgddddd", "AudioService onDestroy: ");
        callback = null;
        stopAudio();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return audioBind;
    }

    public class AudioBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    public interface Callback
    {
        void onStartAudio();
        void onStopAudio();
        void onAudioTimerOut();
        void onError();
        void onAudioTimerTick(long time);
    }
}
