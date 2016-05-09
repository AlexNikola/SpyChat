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
import android.util.Log;
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
        if (checkIsLoggedIn())
        {
            Intent intent = new Intent(this, ActivityMain.class);
            intent.putExtra(C.REQUEST_PIN, true);
            startActivity(intent);
            finish();
            return;
        }
        else
        {
            setContentView(R.layout.activity_auth);
        }

        findCountyCode();
        myPhoneNumber = getPhoneNumber();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SPYchat");
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_view_pager);
        AuthFragmentPagerAdapter adapter = new AuthFragmentPagerAdapter(getSupportFragmentManager());
        assert viewPager != null;
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);
    }

    private boolean checkIsLoggedIn()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, null);
        return accessToken != null;
    }

    @Override
    public void onLogIn(String phone)
    {
        Log.i(TAG, "onLogIn");
        hideKeyBoard();
        //showPhone(phone);
    }

    @Override
    public void onSignUp(String phone) {
        Log.i(TAG, "onSignUp");
        hideKeyBoard();
        //showPhone(phone);
    }

    private void showPhone(String phone)
    {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, phone, Snackbar.LENGTH_INDEFINITE)
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

    public void findCountyCode() {
        ArrayList<Country> countryArrayList = MyDbHelper.readCountries(new MyDbHelper(this).getReadableDatabase());
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myCountryISO = tm.getSimCountryIso();
        //simCountry = "in";
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
    public void onAuthorizationSuccess(String accessToken, String refreshToken, String myPhoneNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString(C.SHARED_ACCESS_TOKEN, accessToken)
                .putString(C.SHARED_REFRESH_TOKEN, refreshToken)
                .putString(C.SHARED_MY_PHONE_NUMBER, myPhoneNumber)
                .apply();
        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(C.REQUEST_PIN, false);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(String error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Connection error", Snackbar.LENGTH_INDEFINITE)
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

    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public String getPhoneNumber()
    {
        Log.d("myPerm", "AA getPhoneNumber ");
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        return tm.getLine1Number();
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    C.READ_SMS_CODE);
        }
        else
        {
            TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            myPhoneNumber = tm.getLine1Number();
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("qqqqq", "AA onActivityResult resultCode "+resultCode);

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("myPerm", "AA onRequestPermissionsResult " + requestCode);
        if (requestCode == C.READ_SMS_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                myPhoneNumber = tm.getLine1Number();
                if (myPhoneNumber == null || myPhoneNumber.length() == 0)
                {
                    myPhoneNumber = "";
                    Toast.makeText(this, "Phone number is unavailable", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "Sorry!!! Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }*/

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
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void hideKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
