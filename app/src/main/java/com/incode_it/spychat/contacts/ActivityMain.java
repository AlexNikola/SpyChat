package com.incode_it.spychat.contacts;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.incode_it.spychat.settings.ActivitySettings;
import com.incode_it.spychat.C;
import com.incode_it.spychat.MyTimerTask;
import com.incode_it.spychat.R;
import com.incode_it.spychat.alarm.AlarmReceiverGlobal;
import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.pin.FragmentPin;
import com.incode_it.spychat.settings.FragmentSettings;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Timer;

public class ActivityMain extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener, FragmentPin.FragmentPinListener, GestureDetector.OnGestureListener

{
    private static final String TAG = "debb";

    public static final String IS_NAV_OPEN = "is_nav_open";

    private static final int DURATION_SLIDE = 300;
    private static final int DURATION_SCALE = 150;
    private static final int DURATION_NAV_ICONS = 250;
    private static final int DELAY_NAV_ICONS = 80;

    private boolean isNavMenuOpen;
    private View contentContainer;
    private ImageView timerImageView, settingsImageView, logOutImageView;
    private TextView globalTimerTextView;
    private MyTimerTask timerTask;
    private Toolbar toolbar;
    private float translationX;

    private boolean requestPin = true;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("lifes", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_NAV_OPEN, isNavMenuOpen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        requestPin = getIntent().getBooleanExtra(C.REQUEST_PIN, true);

        if (savedInstanceState != null)
        {
            isNavMenuOpen = savedInstanceState.getBoolean(IS_NAV_OPEN, false);
        }

        initContentContainer();

        View navContainer = findViewById(R.id.nav_container);
        assert navContainer != null;
        ViewGroup.LayoutParams layoutParams = navContainer.getLayoutParams();
        translationX = layoutParams.width;

        initNavIcons();
        initToolbar();

        setupFragment();
        startTimer();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (isNavMenuOpen) {
            closeNavMenu();
        } else {
            super.onBackPressed();
        }
    }


    float xDown = 0;
    private void initContentContainer()
    {
        contentContainer = findViewById(R.id.content_container);
        /*contentContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("mytou", "onTouch");
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                        if (contentContainer.getAnimation() != null)contentContainer.getAnimation().cancel();
                        xDown = event.getX();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float translation;
                        if ((event.getRawX() - xDown) > 200)
                        {
                            translation = 200;
                        }
                        else if ((event.getRawX() - xDown) < 0)
                        {
                            translation = 0;
                        }
                        else translation = event.getRawX() - xDown;

                        contentContainer.setTranslationX(translation);
                        break;

                    case MotionEvent.ACTION_UP:
                        if ((event.getRawX() - xDown) < 100)
                        {
                            contentContainer.animate().translationX(0).setDuration(100).start();
                        }
                        else
                        {
                            contentContainer.animate().translationX(200).setDuration(100).start();
                        }
                        break;
                }
                return false;
            }
        });*/
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                if (contentContainer.getAnimation() != null)contentContainer.getAnimation().cancel();
                xDown = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                float translation;
                if ((event.getRawX() - xDown) > 200)
                {
                    translation = 200;
                }
                else if ((event.getRawX() - xDown) < 0)
                {
                    translation = 0;
                }
                else translation = event.getRawX() - xDown;

                contentContainer.setTranslationX(translation);
                break;

            case MotionEvent.ACTION_UP:
                if ((event.getRawX() - xDown) < 100)
                {
                    contentContainer.animate().translationX(0).setDuration(100).start();
                }
                else
                {
                    contentContainer.animate().translationX(200).setDuration(100).start();
                }
                break;
        }
        return true;
    }*/

    private void setupFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment = fragmentManager.findFragmentByTag(FragmentContacts.FRAGMENT_CONTACTS);
        if (fragment == null)
        {
            FragmentContacts fragmentContacts = FragmentContacts.newInstance();
            fragmentTransaction.replace(R.id.fragments_container, fragmentContacts, FragmentContacts.FRAGMENT_CONTACTS);
            fragmentTransaction.commit();
        }
    }



    @Override
    protected void onPause() {
        requestPin = true;
        Log.d("lifes", "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("lifes", "onResume");
        showPinDialog();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        requestPin = false;
        Log.d("lifes", "onActivityResult");
        if (requestCode == C.REQUEST_CODE_ACTIVITY_CHAT) {
             if (resultCode == C.RESULT_EXIT)
            {
                finish();
            }
        }

        timerImageView.setAlpha(0f);
        timerImageView.setScaleX(0f);
        timerImageView.setScaleY(0f);

        settingsImageView.setAlpha(0f);
        settingsImageView.setScaleX(0f);
        settingsImageView.setScaleY(0f);

        logOutImageView.setAlpha(0f);
        logOutImageView.setScaleX(0f);
        logOutImageView.setScaleY(0f);

        contentContainer.setTranslationX(0f);
        contentContainer.setScaleX(1f);
        contentContainer.setScaleY(1f);
        isNavMenuOpen = false;
        toolbar.setNavigationIcon(R.drawable.nav_menu);
    }




    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        requestPin = false;
        Log.d("lifes", "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void initNavIcons()
    {
        globalTimerTextView = (TextView) findViewById(R.id.global_timer_tv);

        timerImageView = (ImageView) findViewById(R.id.timer_global);
        assert timerImageView != null;
        settingsImageView = (ImageView) findViewById(R.id.settings);
        assert settingsImageView != null;
        logOutImageView = (ImageView) findViewById(R.id.log_out);
        assert logOutImageView != null;

        if (!isNavMenuOpen)
        {
            timerImageView.setAlpha(0f);
            timerImageView.setScaleX(0f);
            timerImageView.setScaleY(0f);

            settingsImageView.setAlpha(0f);
            settingsImageView.setScaleX(0f);
            settingsImageView.setScaleY(0f);

            logOutImageView.setAlpha(0f);
            logOutImageView.setScaleX(0f);
            logOutImageView.setScaleY(0f);
        }
        else
        {
            contentContainer.setTranslationX(200f);
            contentContainer.setScaleX(0.95f);
            contentContainer.setScaleY(0.95f);
        }

        timerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerDialog();
            }
        });
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ActivitySettings.class);
                startActivityForResult(intent, C.REQUEST_CODE_ACTIVITY_SETTINGS);
                //startSettingsDialog();
            }
        });
        logOutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().remove(C.SHARED_ACCESS_TOKEN).remove(C.SHARED_REFRESH_TOKEN).apply();
                Intent intent = new Intent(ActivityMain.this, ActivityAuth.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void startTimerDialog()
    {
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                ActivityMain.this, 0, 0, true
        );

        tpd.vibrate(true);
        tpd.setAccentColor(getResources().getColor(R.color.colorPrimary));
        tpd.setTitle("Global timer");
        tpd.enableSeconds(true);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {

        long timer = (hourOfDay * 60 * 60 * 1000) + (minute * 60 * 1000) + (second * 1000);
        long removalTime = System.currentTimeMillis() + timer;
        AlarmReceiverGlobal alarmReceiverGlobal = new AlarmReceiverGlobal();
        if (timer == 0)
        {
            long removalTimeOld = sharedPreferences.getLong(C.REMOVAL_GLOBAL_TIME, 0);
            sharedPreferences.edit().putLong(C.REMOVAL_GLOBAL_TIME, 0).apply();
            alarmReceiverGlobal.cancelAlarm(this, removalTimeOld);
        }
        else
        {
            sharedPreferences.edit().putLong(C.REMOVAL_GLOBAL_TIME, removalTime).apply();
            alarmReceiverGlobal.setAlarm(this, removalTime);
        }
        startTimer();
    }

    public void startTimer()
    {
        if (timerTask != null && timerTask.isRunning)
        {
            timerTask.cancel();
        }
        long removalTime = sharedPreferences.getLong(C.REMOVAL_GLOBAL_TIME, 0);
        if (removalTime > 0)
        {
            timerTask = new MyTimerTask(removalTime, globalTimerTextView);
            timerTask.isRunning = true;
            Timer myTimer = new Timer();
            myTimer.schedule(timerTask, 0, 1000);
        }
        else
        {
            globalTimerTextView.setText("00:00:00");
        }
    }

    private void startSettingsDialog()
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FragmentSettings newFragment = FragmentSettings.newInstance();
        newFragment.show(ft, FragmentSettings.TAG);
    }

    private void initToolbar()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SPYchat");
        setSupportActionBar(toolbar);
        if (isNavMenuOpen)
        {
            toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
        }
        else
        {
            toolbar.setNavigationIcon(R.drawable.nav_menu);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNavMenuOpen)
                {
                    openNavMenu();
                }
                else
                {
                    closeNavMenu();
                }
            }
        });
    }

    private void openNavMenu()
    {
        isNavMenuOpen = true;
        ObjectAnimator translation = ObjectAnimator.ofFloat(contentContainer, "translationX", 0, translationX);
        translation.setDuration(DURATION_SLIDE);

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(contentContainer, "scaleX", 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(contentContainer, "scaleY", 0.95f);
        scaleDownX.setDuration(DURATION_SCALE);
        scaleDownY.setDuration(DURATION_SCALE);


        ObjectAnimator contactsImageViewAlpha = ObjectAnimator.ofFloat(timerImageView, "alpha", 0f, 1f);
        contactsImageViewAlpha.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator settingsImageViewViewAlpha = ObjectAnimator.ofFloat(settingsImageView, "alpha", 0f, 1f);
        settingsImageViewViewAlpha.setDuration(DURATION_NAV_ICONS);
        settingsImageViewViewAlpha.setStartDelay(DELAY_NAV_ICONS);
        ObjectAnimator logOutImageViewAlpha = ObjectAnimator.ofFloat(logOutImageView, "alpha", 0f, 1f);
        logOutImageViewAlpha.setDuration(DURATION_NAV_ICONS);
        logOutImageViewAlpha.setStartDelay(DELAY_NAV_ICONS);

        ObjectAnimator contactsImageViewScaleX = ObjectAnimator.ofFloat(timerImageView, "scaleX", 0f, 1f);
        contactsImageViewScaleX.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator contactsImageViewScaleY = ObjectAnimator.ofFloat(timerImageView, "scaleY", 0f, 1f);
        contactsImageViewScaleY.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator settingsImageViewViewScaleX = ObjectAnimator.ofFloat(settingsImageView, "scaleX", 0f, 1f);
        settingsImageViewViewScaleX.setDuration(DURATION_NAV_ICONS);
        settingsImageViewViewScaleX.setStartDelay(DELAY_NAV_ICONS);
        ObjectAnimator settingsImageViewViewScaleY = ObjectAnimator.ofFloat(settingsImageView, "scaleY", 0f, 1f);
        settingsImageViewViewScaleY.setDuration(DURATION_NAV_ICONS);
        settingsImageViewViewScaleY.setStartDelay(DELAY_NAV_ICONS);
        ObjectAnimator logOutImageViewScaleX = ObjectAnimator.ofFloat(logOutImageView, "scaleX", 0f, 1f);
        logOutImageViewScaleX.setDuration(DURATION_NAV_ICONS);
        logOutImageViewScaleX.setStartDelay(DELAY_NAV_ICONS);
        ObjectAnimator logOutImageViewScaleY = ObjectAnimator.ofFloat(logOutImageView, "scaleY", 0f, 1f);
        logOutImageViewScaleY.setDuration(DURATION_NAV_ICONS);
        logOutImageViewScaleY.setStartDelay(DELAY_NAV_ICONS);

        AnimatorSet animatorSetIconsAlpha = new AnimatorSet();
        animatorSetIconsAlpha.play(settingsImageViewViewAlpha).with(logOutImageViewAlpha).with(contactsImageViewAlpha);

        AnimatorSet animatorSetIconsScaleX = new AnimatorSet();
        animatorSetIconsScaleX.play(settingsImageViewViewScaleX).with(logOutImageViewScaleX).with(contactsImageViewScaleX);

        AnimatorSet animatorSetIconsScaleY = new AnimatorSet();
        animatorSetIconsScaleY.play(settingsImageViewViewScaleY).with(logOutImageViewScaleY).with(contactsImageViewScaleY);

        AnimatorSet animatorSetContainer = new AnimatorSet();
        animatorSetContainer.play(scaleDownX).with(scaleDownY).with(translation);
        animatorSetContainer.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSetContainer.start();

        AnimatorSet animatorSetIcons = new AnimatorSet();
        animatorSetIcons.play(animatorSetIconsAlpha).with(animatorSetIconsScaleX).with(animatorSetIconsScaleY);
        animatorSetIcons.setStartDelay(80);
        animatorSetIcons.setInterpolator(new OvershootInterpolator(3f));
        animatorSetIcons.start();
    }

    private void closeNavMenu()
    {
        isNavMenuOpen = false;
        ObjectAnimator translation = ObjectAnimator.ofFloat(contentContainer, "translationX", translationX, 0f);
        translation.setDuration(DURATION_SLIDE);

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(contentContainer, "scaleX", 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(contentContainer, "scaleY", 1f);
        scaleDownX.setDuration(DURATION_SCALE);
        scaleDownY.setDuration(DURATION_SCALE);

        ObjectAnimator contactsImageViewAlpha = ObjectAnimator.ofFloat(timerImageView, "alpha", 0f);
        contactsImageViewAlpha.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator settingsImageViewViewAlpha = ObjectAnimator.ofFloat(timerImageView, "alpha", 0f);
        settingsImageViewViewAlpha.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator logOutImageViewAlpha = ObjectAnimator.ofFloat(timerImageView, "alpha", 0f);
        logOutImageViewAlpha.setDuration(DURATION_NAV_ICONS);

        ObjectAnimator contactsImageViewScaleX = ObjectAnimator.ofFloat(timerImageView, "scaleX", 0f);
        contactsImageViewScaleX.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator contactsImageViewScaleY = ObjectAnimator.ofFloat(timerImageView, "scaleY", 0f);
        contactsImageViewScaleY.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator settingsImageViewViewScaleX = ObjectAnimator.ofFloat(settingsImageView, "scaleX", 0f);
        settingsImageViewViewScaleX.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator settingsImageViewViewScaleY = ObjectAnimator.ofFloat(settingsImageView, "scaleY", 0f);
        settingsImageViewViewScaleY.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator logOutImageViewScaleX = ObjectAnimator.ofFloat(logOutImageView, "scaleX", 0f);
        logOutImageViewScaleX.setDuration(DURATION_NAV_ICONS);
        ObjectAnimator logOutImageViewScaleY = ObjectAnimator.ofFloat(logOutImageView, "scaleY", 0f);
        logOutImageViewScaleY.setDuration(DURATION_NAV_ICONS);

        AnimatorSet animatorSetIconsAlpha = new AnimatorSet();
        animatorSetIconsAlpha.play(settingsImageViewViewAlpha).with(logOutImageViewAlpha).with(contactsImageViewAlpha);

        AnimatorSet animatorSetIconsScaleX = new AnimatorSet();
        animatorSetIconsScaleX.play(settingsImageViewViewScaleX).with(logOutImageViewScaleX).with(contactsImageViewScaleX);

        AnimatorSet animatorSetIconsScaleY = new AnimatorSet();
        animatorSetIconsScaleY.play(settingsImageViewViewScaleY).with(logOutImageViewScaleY).with(contactsImageViewScaleY);

        AnimatorSet animatorSetContainer = new AnimatorSet();
        animatorSetContainer.play(scaleDownX).with(scaleDownY).with(translation);
        animatorSetContainer.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                toolbar.setNavigationIcon(R.drawable.nav_menu);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSetContainer.start();

        AnimatorSet animatorSetIcons = new AnimatorSet();
        animatorSetIcons.play(animatorSetIconsAlpha).with(animatorSetIconsScaleX).with(animatorSetIconsScaleY);
        animatorSetIcons.start();
    }

    @Override
    public void onSecurityClose() {
        finish();
    }

    @Override
    public void onSecurityLogOut() {
        sharedPreferences.edit().remove(C.SHARED_ACCESS_TOKEN).remove(C.SHARED_REFRESH_TOKEN).apply();
        Intent intent = new Intent(ActivityMain.this, ActivityAuth.class);
        startActivity(intent);
        finish();
    }



    private void showPinDialog()
    {
        boolean isPinOn = sharedPreferences.getBoolean(C.SETTING_PIN, false);
        if (isPinOn && requestPin)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(FragmentPin.TAG);
            if (prev != null) {
                ft.remove(prev);
                ft.addToBackStack(null);
                ft.commit();
            }

            ft = getSupportFragmentManager().beginTransaction();
            FragmentPin fragmentPin = FragmentPin.newInstance();
            fragmentPin.show(ft, FragmentPin.TAG);
        }

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
