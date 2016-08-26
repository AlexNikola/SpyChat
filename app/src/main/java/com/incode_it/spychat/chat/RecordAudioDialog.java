package com.incode_it.spychat.chat;

import android.graphics.Point;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecordAudioDialog extends DialogFragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "RecordAudioDialog";

    private static final int STATE_READY = 1;
    private static final int STATE_DONE = 2;
    private static final int STATE_RECORDING = 3;
    private static final int STATE_TIMER_OUT = 4;

    private View containerView;
    private View resultContainerView;
    private TextView timerTextView;
    private View recordBtn, cancelBtn, doneBtn;

    private Callback callback;

    private String audioPath;
    private MediaRecorder mRecorder = null;
    private boolean isRecording;

    private Timer timer;
    private MyTimerTask timerTask;
    private int timerSec = 30;


    public static RecordAudioDialog newInstance(Callback callback) {
        RecordAudioDialog fragment = new RecordAudioDialog();
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.RecordAudioDialog);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_record_audio, container, false);

        mRecorder = new MediaRecorder();

        resultContainerView = view.findViewById(R.id.resultContainer);
        containerView = view.findViewById(R.id.container);
        setSize();

        timerTextView = (TextView) view.findViewById(R.id.timerTextView);
        timerTextView.setText(getStringTime(timerSec * 1000));
        recordBtn = view.findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(this);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);
        doneBtn = view.findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(this);

        setState(STATE_READY);

        return view;
    }

    private void setState(int state)
    {
        switch (state)
        {
            case STATE_RECORDING:
                containerView.setSelected(true);
                recordBtn.setVisibility(View.GONE);
                resultContainerView.setVisibility(View.VISIBLE);
                break;
            case STATE_DONE:

                break;
            case STATE_READY:
                containerView.setSelected(false);
                recordBtn.setVisibility(View.VISIBLE);
                resultContainerView.setVisibility(View.GONE);
                break;
            case STATE_TIMER_OUT:
                containerView.setSelected(false);
                recordBtn.setVisibility(View.GONE);
                resultContainerView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.recordBtn:
                setState(STATE_RECORDING);
                startRecording();
                break;
            case R.id.cancelBtn:
                setState(STATE_READY);
                if (isRecording)stopRecording();
                removeFile();
                break;
            case R.id.doneBtn:
                if (isRecording)stopRecording();
                done();
                dismiss();
                break;
        }
    }

    private void startRecording()
    {
        isRecording = true;

        startTimer();
        generateFileName();

        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(audioPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(FRAGMENT_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        isRecording = false;

        timer.cancel();
        timer = null;

        mRecorder.stop();
        timerTextView.setText(getStringTime(timerSec * 1000));
    }



    @Override
    public void onPause() {
        if (timer != null) {
            timer.cancel();
        }
        if (mRecorder != null) {
            if (isRecording)
            {
                removeFile();
                mRecorder.stop();
            }
            mRecorder.release();
            mRecorder = null;

        }
        dismiss();
        super.onPause();
    }





    private void removeFile()
    {
        Toast.makeText(getContext(), "Record removed", Toast.LENGTH_SHORT).show();
        File file = new File(audioPath);
        boolean isDeleted = file.delete();
        Log.d(FRAGMENT_TAG, "removeFile: " + isDeleted);
    }

    private void done()
    {
        //Toast.makeText(getContext(), "Save and Send", Toast.LENGTH_SHORT).show();
        callback.onRecordAudioCompleted(audioPath);
    }


    private void startTimer()
    {
        timerTask = new MyTimerTask();
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }

    private void generateFileName()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(null);
        //audioPath = storageDir.getAbsolutePath() + "/" + audioFileName + ".mp3";
        audioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + audioFileName + ".mp3";
    }

    private void setSize()
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = (int) (size.x * 0.75f);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, width);
        containerView.setLayoutParams(layoutParams);
    }

    public interface Callback {
        void onRecordAudioCompleted(String audioPath);
    }

    private class MyTimerTask extends TimerTask
    {
        public long secondsUntilFinished = timerSec;
        @Override
        public void run() {
            new Handler(getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (secondsUntilFinished < 0) {
                        setState(STATE_TIMER_OUT);
                        stopRecording();
                    } else {
                        String time = getStringTime(secondsUntilFinished * 1000);
                        timerTextView.setText(time);
                        secondsUntilFinished--;
                        Log.d("dsdfsfdcc", "tick: " + time);
                    }
                }
            });
        }
    }

    public String getStringTime(long milliseconds){
        long second = (milliseconds / 1000) % 60;
        long minute = (milliseconds / (1000 * 60)) % 60;
        long hour = (milliseconds / (1000 * 60 * 60)) % 24;

        String timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        if (timeString.startsWith("00")) {
            timeString = timeString.substring(3);
        }
        return timeString;
    }

}
