package com.incode_it.spychat.chat;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyTimerTask;
import com.incode_it.spychat.R;

import java.util.Timer;

public class FakeToolbar extends FrameLayout {

    private MyTimerTask timerTask;
    private TextView globalTimerTextView;

    private float popUpTranslationY;
    private View attachmentsBtn;
    private View toolbarUpper;
    private View toolbarLower;
    private boolean isPopupVisible;
    private ImageView takePhoto, takeVideo, openGallery;
    private TextView title;
    private View backBtn;

    public FakeToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View content = layoutInflater.inflate(R.layout.toolbar_fake_layout, null, false);
        addView(content);
        init();
    }

    /*public FakeToolbar(Context context) {
        super(context);
    }

    public FakeToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FakeToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }*/



    private void init()
    {
        Log.d("faket", "init");
        title = (TextView) findViewById(R.id.title);
        backBtn = findViewById(R.id.back_to_contacts);
        toolbarUpper = findViewById(R.id.toolbar_upper);
        popUpTranslationY = toolbarUpper.getLayoutParams().height;
        toolbarLower = findViewById(R.id.toolbar_lower);
        attachmentsBtn = findViewById(R.id.attachments);
        attachmentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopupVisible) hidePopup();
                else showPopup();
            }
        });
        takePhoto = (ImageView) findViewById(R.id.take_photo);
        takeVideo = (ImageView) findViewById(R.id.take_video);
        openGallery = (ImageView) findViewById(R.id.open_gallery);
        globalTimerTextView = (TextView) findViewById(R.id.global_timer_text);
    }

    private void showPopup()
    {
        Log.d("faket", "showPopup "+popUpTranslationY);
        isPopupVisible = true;
        toolbarLower.animate().translationY(popUpTranslationY).start();
    }

    private void hidePopup()
    {
        Log.d("faket", "hidePopup "+popUpTranslationY);
        isPopupVisible = false;
        toolbarLower.animate().translationY(0f).start();
    }

    public void startTimer()
    {
        Log.d("faket", "startTimer");
        if (timerTask != null && timerTask.isRunning)
        {
            timerTask.cancel();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        long removalTime = sharedPreferences.getLong(C.REMOVAL_GLOBAL_TIME, 0);
        long timer = sharedPreferences.getLong(C.GLOBAL_TIMER, 0);
        Log.d("timmmer", "startTimer timer " + timer);
        Log.d("timmmer", "startTimer removalTime " + removalTime);
        if (removalTime > 0)
        {
            timerTask = new MyTimerTask(removalTime, globalTimerTextView, timer);
            timerTask.isRunning = true;
            Timer myTimer = new Timer();
            myTimer.schedule(timerTask, 0, 1000);
        }
        else
        {
            globalTimerTextView.setText("00:00:00");
        }
    }

    public void setTitle(String text)
    {
        title.setText(text);
    }

    public void setOnBackClickListener(OnClickListener listener)
    {
        backBtn.setOnClickListener(listener);
    }

    public void setOnPhotoClickListener(OnClickListener listener)
    {
        takePhoto.setOnClickListener(listener);
    }

    public void setOnVideoClickListener(OnClickListener listener)
    {
        takeVideo.setOnClickListener(listener);
    }

    public void setOnGalleryClickListener(OnClickListener listener)
    {
        openGallery.setOnClickListener(listener);
    }
}
