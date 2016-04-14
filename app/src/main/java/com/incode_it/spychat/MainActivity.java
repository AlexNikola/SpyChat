package com.incode_it.spychat;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener

{
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
        typeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
        CURRENT_TITLE = getResources().getString(R.string.nav_home);
        CURRENT_FRAGMENT = FRAGMENT_HOME;

        if (savedInstanceState != null)
        {
            CURRENT_FRAGMENT = savedInstanceState.getString(FRAGMENT, FRAGMENT_HOME);
            CURRENT_TITLE = savedInstanceState.getString(TITLE, CURRENT_TITLE);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SPYchat");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        //setupFragment(CURRENT_FRAGMENT);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.home_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.person_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.security_24dp);
        tabLayout.getTabAt(3).setIcon(R.drawable.settings_24dp);

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

        /*CURRENT_TITLE = (String) item.getTitle();
        toolbar.setTitle(CURRENT_TITLE);
        switch (item.getItemId())
        {
            case R.id.nav_settings:
                setupFragment(FRAGMENT_SETTINGS);
                break;
            case R.id.nav_contacts:
                setupFragment(FRAGMENT_CONTACTS);
                break;
            case R.id.nav_security:
                setupFragment(FRAGMENT_SECURITY);
                break;
            case R.id.nav_home:
                setupFragment(FRAGMENT_HOME);
                break;
        }*/

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
                case FRAGMENT_SECURITY:
                    FragmentSecurity fragmentSecurity = FragmentSecurity.newInstance();
                    fragmentTransaction.replace(R.id.fragments_container, fragmentSecurity, FRAGMENT_SECURITY);
                    break;
                case FRAGMENT_HOME:
                    FragmentHome fragmentHome = FragmentHome.newInstance();
                    fragmentTransaction.replace(R.id.fragments_container, fragmentHome, FRAGMENT_HOME);
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



    @Override
    public void onFragmentInteraction() {

    }


}
