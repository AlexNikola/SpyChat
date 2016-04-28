package com.incode_it.spychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.incode_it.spychat.alarm.AlarmReceiver;

import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener

{
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "myhttp";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    public static final String FRAGMENT = "fragment";
    public static final String TITLE = "title";
    public static final String FRAGMENT_SETTINGS = "fr_set";
    public static final String FRAGMENT_CONTACTS = "fr_con";
    public static final String FRAGMENT_SECURITY = "fr_sec";
    public static final String FRAGMENT_HOME     = "fr_home";
    public static String CURRENT_FRAGMENT;
    public static String CURRENT_TITLE;

    Toolbar toolbar;
    private TabLayout tabLayout;
    static Typeface typeface;
    ViewPager viewPager;
    public static String myPhoneNumber;

    AlarmReceiver alarmReceiver = new AlarmReceiver();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FRAGMENT, CURRENT_FRAGMENT);
        outState.putString(TITLE, CURRENT_TITLE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmReceiver.setAlarm(this);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
        CURRENT_TITLE = getResources().getString(R.string.nav_home);
        CURRENT_FRAGMENT = FRAGMENT_CONTACTS;

        if (savedInstanceState != null)
        {
            CURRENT_FRAGMENT = savedInstanceState.getString(FRAGMENT, FRAGMENT_CONTACTS);
            CURRENT_TITLE = savedInstanceState.getString(TITLE, CURRENT_TITLE);
        }

        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        myPhoneNumber = tm.getLine1Number();
        if (myPhoneNumber == null)
        {
            Toast.makeText(this, "phone number is unavailable", Toast.LENGTH_SHORT).show();
        }

        initRegBroadcastReceiver();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SPYchat");
        setSupportActionBar(toolbar);

        final View contentContainer = findViewById(R.id.content_container);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                float moveFactor = (drawer.getWidth() * slideOffset);

                contentContainer.setTranslationX(slideOffset);
            }
        };
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        setupFragment(CURRENT_FRAGMENT);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.nav_contacts:
                setupFragment(FRAGMENT_CONTACTS);
                break;
            case R.id.nav_settings:
                setupFragment(FRAGMENT_SETTINGS);
                break;
            case R.id.nav_log_out:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().remove(C.ACCESS_TOKEN).remove(C.REFRESH_TOKEN).apply();
                Intent intent = new Intent(this, ActivityAuth.class);
                startActivity(intent);
                finish();
                break;
        }

        return true;
    }

    private void setupFragment(String tag)
    {
        CURRENT_FRAGMENT = tag;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null)
        {
            switch (tag)
            {
                case FRAGMENT_SETTINGS:
                    FragmentSettings fragmentSettings = FragmentSettings.newInstance();
                    fragmentTransaction.replace(R.id.fragments_container, fragmentSettings, FRAGMENT_SETTINGS);
                    break;
                case FRAGMENT_CONTACTS:
                    FragmentContacts fragmentContacts = FragmentContacts.newInstance();
                    fragmentTransaction.replace(R.id.fragments_container, fragmentContacts, FRAGMENT_CONTACTS);
                    break;
            }
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
    }

    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragmentArrayList;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentArrayList = new ArrayList<>();
            fragmentArrayList.add(FragmentHome.newInstance());
            fragmentArrayList.add(FragmentContacts.newInstance());
            fragmentArrayList.add(FragmentSecurity.newInstance());
            fragmentArrayList.add(FragmentSettings.newInstance());
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentArrayList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }

        /*@Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }    */

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

}
