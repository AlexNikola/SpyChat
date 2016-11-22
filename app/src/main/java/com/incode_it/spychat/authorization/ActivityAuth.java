package com.incode_it.spychat.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.incode_it.spychat.C;
import com.incode_it.spychat.R;
import com.incode_it.spychat.contacts.ActivityMain;
import com.incode_it.spychat.country_selection.Country;
import com.incode_it.spychat.data_base.MyDbHelper;
import com.incode_it.spychat.interfaces.OnFragmentsAuthorizationListener;

import java.util.ArrayList;

public class ActivityAuth extends AppCompatActivity implements OnFragmentsAuthorizationListener {
    private CoordinatorLayout coordinatorLayout;
    private static final String TAG = "myhttp";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static String myPhoneNumber;
    public static String myCountryCode;
    public static String myCountryISO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPlayServices();

        if (checkIsLoggedIn(this)) {
            Intent intent = new Intent(this, ActivityMain.class);
            intent.putExtra(C.EXTRA_REQUEST_PIN, true);
            startActivity(intent);
            finish();
            return;
        } else {
            setContentView(R.layout.activity_auth);
        }

        findCountyCode();
        myPhoneNumber = getPhoneNumber(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SpyChatter");
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_view_pager);
        AuthFragmentPagerAdapter adapter = new AuthFragmentPagerAdapter(getSupportFragmentManager());
        assert viewPager != null;
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);
    }

    public static boolean checkIsLoggedIn(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, null);
        return accessToken != null;
    }



    public void findCountyCode() {
        ArrayList<Country> countryArrayList = MyDbHelper.readCountries(new MyDbHelper(this).getReadableDatabase());
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myCountryISO = tm.getSimCountryIso();
        if (myCountryISO == null) return;
        for (Country country: countryArrayList)
        {
            if (country.codeISO.equalsIgnoreCase(myCountryISO))
            {
                myCountryCode = country.codePhone;
                break;
            }
        }
    }

    @Override
    public void onLogInSuccess(String accessToken, String refreshToken, String phone) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString(C.SHARED_ACCESS_TOKEN, accessToken)
                .putString(C.SHARED_REFRESH_TOKEN, refreshToken)
                .putString(C.SHARED_MY_PHONE_NUMBER, phone)
                .apply();

        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(C.EXTRA_REQUEST_PIN, false);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSignUpSuccess(String accessToken, String refreshToken, String myPhoneNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString(C.SHARED_ACCESS_TOKEN, accessToken)
                .putString(C.SHARED_REFRESH_TOKEN, refreshToken)
                .putString(C.SHARED_MY_PHONE_NUMBER, myPhoneNumber)
                .apply();

        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(C.EXTRA_REQUEST_PIN, false);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(String error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Error", Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.RED)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    public static String getPhoneNumber(Context context)
    {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
        return tm.getLine1Number();

    }


    public class AuthFragmentPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragmentArrayList;

        public AuthFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentArrayList = new ArrayList<>();
            fragmentArrayList.add(new FragmentLogIn());
            fragmentArrayList.add(new FragmentSingUp());
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentArrayList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) return getString(R.string.log_in);
            else return getString(R.string.sign_up);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }


}
