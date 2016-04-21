package com.incode_it.spychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ActivityAuth extends AppCompatActivity implements OnLogInListener,
        OnSignUpListener
{
    private View progressBarContainer;
    private ViewPager viewPager;
    private static final String TAG = "myhttp";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPlayServices();
        if (checkIsLoggedIn())
        {
            Intent intent = new Intent(this, ActivityMain.class);
            startActivity(intent);
            return;
        }
        else
        {
            setContentView(R.layout.activity_auth);
        }

        progressBarContainer = findViewById(R.id.progressBar_cont);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle("SPYchat");
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.fragment_view_pager);
        AuthFragmentPagerAdapter adapter = new AuthFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private boolean checkIsLoggedIn()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = sharedPreferences.getString(C.ACCESS_TOKEN, null);
        return accessToken != null;
    }

    @Override
    public void onLogIn(String phoneNumber, String password)
    {
        Log.i(TAG, "onLogIn");
        hideKeyBoard();
        new LogInTask().execute(phoneNumber, password);
    }

    @Override
    public void onSignUp(String phoneNumber, String password)
    {
        Log.i(TAG, "onSignUp");
        hideKeyBoard();
        new SignUpTask().execute(phoneNumber, password);
    }

    private class LogInTask extends AsyncTask<String, Void, String>
    {

        public LogInTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];

            try
            {

                InstanceID instanceID = InstanceID.getInstance(ActivityAuth.this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.i(TAG, "GCM Registration Token: " + token);

                String urlParameters = "phone=" +
                        phoneNumber +
                        "&" +
                        "password=" +
                        password +
                        "&" +
                        "regToken=" +
                        token;

                URL url = new URL(C.BASE_URL + "api/v1/auth/getAccessToke/");
                Log.i(TAG, "URL: " + url.toString() + urlParameters);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(20000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStreamWriter outputWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
                outputWriter.write(urlParameters);
                outputWriter.flush();
                outputWriter.close();

                int httpResponse = httpURLConnection.getResponseCode();
                //BufferedReader bufferedReader;
                InputStream inputStream;
                if (httpResponse == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "HTTP_OK");
                    /*bufferedReader = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream()));*/
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    Log.d(TAG, "HTTP_ERROR");
                    /*bufferedReader = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getErrorStream()));*/
                    inputStream = httpURLConnection.getErrorStream();
                }

                /*StringBuilder result = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null)
                {
                    result.append(line).append("\n");
                }
                bufferedReader.close();*/
                String response = IOUtils.toString(inputStream);
                inputStream.close();
                Log.d(TAG, "resp: " + response);
                return response;
            }
            catch (IOException e)
            {
                Log.d(TAG, "my err " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBarContainer.setVisibility(View.INVISIBLE);
            if (result == null) {
                Toast.makeText(ActivityAuth.this, "Connection error", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityAuth.this);
                        sharedPreferences.edit().putString(C.ACCESS_TOKEN, accessToken).putString(C.REFRESH_TOKEN, refreshToken).apply();
                        Intent intent = new Intent(ActivityAuth.this, ActivityMain.class);
                        startActivity(intent);

                    } else if (res.equals("error")) {
                        /*
                        * HTTP_ERROR resp: {"result":"error","param":"user","message":"Incorrect password"}
                        * resp: {"result":"error","param":"user","message":"User not found"}*/
                        String param = jsonResponse.getString("param");
                        if (param.equals("password")){
                            Toast.makeText(ActivityAuth.this, "password", Toast.LENGTH_SHORT).show();
                        } else if (param.equals("phone")) {
                            Toast.makeText(ActivityAuth.this, "phone exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class SignUpTask extends AsyncTask<String, Void, String>
    {

        public SignUpTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];

            try
            {

                InstanceID instanceID = InstanceID.getInstance(ActivityAuth.this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.i(TAG, "GCM Registration Token: " + token);

                String urlParameters = "phone=" +
                        phoneNumber +
                        "&" +
                        "password=" +
                        password +
                        "&" +
                        "confirm=" +
                        password +
                        "&" +
                        "regToken=" +
                        token;

                URL url = new URL(C.BASE_URL + "api/v1/users/register/");
                Log.i(TAG, "URL: " + url.toString() + urlParameters);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(20000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStreamWriter outputWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
                outputWriter.write(urlParameters);
                outputWriter.flush();
                outputWriter.close();

                int httpResponse = httpURLConnection.getResponseCode();
                InputStream inputStream;
                if (httpResponse == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "HTTP_OK");
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    Log.d(TAG, "HTTP_ERROR");
                    inputStream = httpURLConnection.getErrorStream();
                }

                String response = IOUtils.toString(inputStream);
                inputStream.close();
                Log.d(TAG, "resp: " + response);
                return response;
            }
            catch (IOException e)
            {
                Log.d(TAG, "my err " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBarContainer.setVisibility(View.INVISIBLE);
            if (result == null) {
                Toast.makeText(ActivityAuth.this, "Connection error", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityAuth.this);
                        sharedPreferences.edit().putString(C.ACCESS_TOKEN, accessToken).putString(C.REFRESH_TOKEN, refreshToken).apply();
                        Intent intent = new Intent(ActivityAuth.this, ActivityMain.class);
                        startActivity(intent);

                    } else if (res.equals("error")) {
                        String param = jsonResponse.getString("param");
                        if (param.equals("password")){
                            Toast.makeText(ActivityAuth.this, "password", Toast.LENGTH_SHORT).show();
                        } else if (param.equals("phone")) {
                            Toast.makeText(ActivityAuth.this, "phone exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private boolean getInstanceIDToken()
    {
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "GCM Registration Token: " + token);
        return token != null;
    }
















    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    public static class FragmentLogIn extends Fragment
    {
        private OnLogInListener logInListener;

        public FragmentLogIn() {
            // Required empty public constructor
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            logInListener = (OnLogInListener) context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_log_in, container, false);

            View inputPhoneLayout = view.findViewById(R.id.input_phone_layout);
            final TextInputEditText phoneET = (TextInputEditText) inputPhoneLayout.findViewById(R.id.edit_text);
            phoneET.setText(getPhoneNumber(getContext()));
            final TextInputLayout tilPhone = (TextInputLayout) inputPhoneLayout.findViewById(R.id.text_input_layout);
            tilPhone.setHint(getString(R.string.phone_number_hint));
            inputPhoneLayout.findViewById(R.id.edit_text_clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    phoneET.setText("");
                }
            });

            View inputPassLayout = view.findViewById(R.id.input_pass_layout);
            final TextInputEditText passET = (TextInputEditText) inputPassLayout.findViewById(R.id.edit_text);
            final TextInputLayout tilPass = (TextInputLayout) inputPassLayout.findViewById(R.id.text_input_layout);
            tilPass.setHint(getString(R.string.pass_hint));
            inputPassLayout.findViewById(R.id.edit_text_clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passET.setText("");
                }
            });


            Button logInBtn = (Button) view.findViewById(R.id.log_in_button);
            logInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = phoneET.getText().toString();
                    String password = passET.getText().toString();
                    logInListener.onLogIn(phoneNumber, password);
                }
            });



            return view;
        }

    }

    private static String getPhoneNumber(Context context)
    {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
        String myPhoneNumber = tm.getLine1Number();
        if (myPhoneNumber == null)
        {
            Toast.makeText(context, "phone number is unavailable", Toast.LENGTH_SHORT).show();
        }
        //myPhoneNumber = "+38066751470";
        //boolean isValid = Patterns.PHONE.matcher(myPhoneNumber).matches();
        //Log.i(TAG, "isValid: "+ myPhoneNumber + " " + isValid);
        return myPhoneNumber;
    }

    public static class FragmentSingUp extends Fragment
    {
        private OnSignUpListener onSignUpListener;

        public FragmentSingUp() {
            // Required empty public constructor
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            onSignUpListener = (OnSignUpListener) context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
            final TextInputEditText phoneET = (TextInputEditText) view.findViewById(R.id.phone_number_et);
            phoneET.setText(getPhoneNumber(getContext()));
            final TextInputEditText passET = (TextInputEditText) view.findViewById(R.id.password_et);
            final TextInputEditText confPassET = (TextInputEditText) view.findViewById(R.id.conf_password_et);
            Button singUpBtn = (Button) view.findViewById(R.id.sign_up_button);
            singUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = phoneET.getText().toString();
                    String password = passET.getText().toString();
                    String confPassword = confPassET.getText().toString();

                    if (password.equals(confPassword))
                    {
                        onSignUpListener.onSignUp(phoneNumber, password);
                    }
                    else Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();

                }
            });
            view.findViewById(R.id.phone_number_et_clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    phoneET.setText("");
                }
            });
            view.findViewById(R.id.password_et_clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passET.setText("");
                }
            });
            view.findViewById(R.id.conf_password_et_clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confPassET.setText("");
                }
            });
            return view;
        }

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
