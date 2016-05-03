package com.incode_it.spychat.contacts;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyTimerTask;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;
import com.incode_it.spychat.alarm.AlarmReceiverGlobal;
import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.gcm.RegistrationIntentService;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Timer;

public class ActivityMain extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener

{
    private static final String TAG = "debb";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    public static final String IS_NAV_OPEN = "is_nav_open";
    public static final String FRAGMENT_CONTACTS = "fr_con";

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_NAV_OPEN, isNavMenuOpen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
        {
            isNavMenuOpen = savedInstanceState.getBoolean(IS_NAV_OPEN, false);
        }

        initRegBroadcastReceiver();

        contentContainer = findViewById(R.id.content_container);
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




    private void setupFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_CONTACTS);
        if (fragment == null)
        {
            FragmentContacts fragmentContacts = FragmentContacts.newInstance();
            fragmentTransaction.replace(R.id.fragments_container, fragmentContacts, FRAGMENT_CONTACTS);
            fragmentTransaction.commit();
        }
    }



    private void initRegBroadcastReceiver()
    {
        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //Log.d(TAG, "RegBroadcastReceiver sentToken: " + sentToken);
                } else {
                    //Log.d(TAG, "RegBroadcastReceiver sentToken: " + sentToken);
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
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

                startSettingsDialog();
            }
        });
        logOutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityMain.this);
                sharedPreferences.edit().remove(C.ACCESS_TOKEN).remove(C.REFRESH_TOKEN).apply();
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    }

    private void initToolbar()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SPYchat");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.nav_menu);

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

}
