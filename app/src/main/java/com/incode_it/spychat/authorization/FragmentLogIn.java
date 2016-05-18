package com.incode_it.spychat.authorization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.ActivityForgotPassword;
import com.incode_it.spychat.country_selection.ActivitySelectCountry;
import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;
import com.incode_it.spychat.interfaces.OnFragmentsAuthorizationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class FragmentLogIn extends Fragment
{
    private static final String TAG = "myhttp";
    private Context context;

    private OnFragmentsAuthorizationListener fragmentListener;
    private TextInputEditText phoneET;
    private TextInputEditText passET;

    private TextView errorPhoneTextView;
    private TextView errorPassTextView;

    private View logInBtnText;
    private View progressBarView;
    private View logInBtnView;
    private View selectCountryBtnView;

    private TextView countryCodeTextView;

    private String myPhoneNumber = "";
    private String countryCode = "+....";
    private String countryISO = "";

    private View forgotPassView;

    public FragmentLogIn() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fragmentListener = (OnFragmentsAuthorizationListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log_in, container, false);

        if (countryCode.equals("+...."))
        {
            if (ActivityAuth.myCountryCode != null) countryCode = ActivityAuth.myCountryCode;
        }
        if (countryISO.equals(""))
        {
            if (ActivityAuth.myCountryISO != null) countryISO = ActivityAuth.myCountryISO;
        }
        if (ActivityAuth.myPhoneNumber != null) myPhoneNumber = ActivityAuth.myPhoneNumber;

        initPhoneInputLayout(view);
        initPassInputLayout(view);
        initSelectCountryView(view);

        forgotPassView = view.findViewById(R.id.forgot);
        forgotPassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((myPhoneNumber.length() < 1 && countryCode.equals("+....")) || myPhoneNumber.length() < 1)
                {
                    errorPhoneTextView.setText(R.string.enter_phone_number);
                }
                else
                {
                    errorPhoneTextView.setText("");
                    Intent intent = new Intent(getContext(), ActivityForgotPassword.class);
                    intent.putExtra(C.EXTRA_MY_PHONE_NUMBER, myPhoneNumber);
                    startActivity(intent);
                }

            }
        });

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
        errorPassTextView = (TextView) view.findViewById(R.id.error_pass);

        logInBtnText = view.findViewById(R.id.log_in_button_text);
        progressBarView = view.findViewById(R.id.progressBar);

        logInBtnView = view.findViewById(R.id.log_in_button);
        logInBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPhoneNumber = phoneET.getText().toString();
                String password = passET.getText().toString();
                boolean isValid = true;
                if ((myPhoneNumber.length() < 1 && countryCode.equals("+....")) || myPhoneNumber.length() < 1)
                {
                    errorPhoneTextView.setText(R.string.enter_phone_number);
                    isValid = false;
                }
                else if (countryCode.equals("+...."))
                {
                    errorPhoneTextView.setText(R.string.enter_country_code);
                    isValid = false;
                }
                else if (!countryCode.startsWith("+"))
                {
                    errorPhoneTextView.setText(R.string.enter_country_code);
                    isValid = false;
                }
                else errorPhoneTextView.setText("");
                if (password.length() < 6)
                {
                    Log.d("qaz", "Password is too short " + password.length());
                    if (password.length() == 0) errorPassTextView.setText(R.string.enter_password);
                    else errorPassTextView.setText(R.string.short_password);
                    isValid = false;
                }
                else errorPassTextView.setText("");
                if (!isValid) return;

                fragmentListener.onLogIn(countryCode + myPhoneNumber);
                new LogInTask().execute(countryCode + myPhoneNumber, password);
            }
        });

        return view;
    }

    private void initSelectCountryView(View view)
    {
        selectCountryBtnView = view.findViewById(R.id.select_country_btn_view);
        selectCountryBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivitySelectCountry.class);
                intent.putExtra(C.EXTRA_COUNTRY_ISO, countryISO);
                startActivityForResult(intent, C.REQUEST_CODE_SELECT_COUNTRY);
            }
        });

        countryCodeTextView = (TextView) view.findViewById(R.id.country_code);
        countryCodeTextView.setText(countryCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("qqqqq", "FL onActivityResult resultCode "+resultCode);
        if (requestCode == C.REQUEST_CODE_SELECT_COUNTRY) {
            if (resultCode == Activity.RESULT_OK) {
                countryCode = data.getStringExtra(C.EXTRA_COUNTRY_CODE);
                countryISO = data.getStringExtra(C.EXTRA_COUNTRY_ISO);
                countryCodeTextView.setText(countryCode);
            }
        }
    }

    private void initPhoneInputLayout(View view)
    {
        phoneET = (TextInputEditText) view.findViewById(R.id.edit_text_phone);
        if (ActivityAuth.myPhoneNumber != null)
        {
            if (myPhoneNumber.startsWith(countryCode))
            {
                String ph = myPhoneNumber.substring(countryCode.length());
                phoneET.setText(ph);
            }
        }

        view.findViewById(R.id.edit_text_clear_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneET.setText("");
            }
        });
    }

    private void initPassInputLayout(View view)
    {
        passET = (TextInputEditText) view.findViewById(R.id.edit_text_pass);
        view.findViewById(R.id.edit_text_clear_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passET.setText("");
            }
        });
    }

    private class LogInTask extends AsyncTask<String, Void, String>
    {
        public LogInTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarView.setVisibility(View.VISIBLE);
            logInBtnText.setVisibility(View.INVISIBLE);
            logInBtnView.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];
            String regToken;
            try
            {
                regToken = MyConnection.getRegToken(context);

                phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
                password = URLEncoder.encode(password, "UTF-8");
                String urlParameters = "phone=" + phoneNumber + "&" +
                        "password=" + password + "&" +
                        "regToken=" + regToken;

                URL url = new URL(C.BASE_URL + "api/v1/auth/getAccessToke/");

                return MyConnection.post(url, urlParameters, null);
            }
            catch (IOException e)
            {
                Log.e(TAG, "my err " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            logInBtnView.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            logInBtnText.setVisibility(View.VISIBLE);

            if (result == null) {
                fragmentListener.onError("Connection error");
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");

                        fragmentListener.onAuthorizationSuccess(accessToken, refreshToken, countryCode + myPhoneNumber);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("Incorrect password")){
                            errorPassTextView.setText(R.string.incorrect_password);
                        } else if (message.equals("User not found")) {
                            errorPhoneTextView.setText(R.string.user_not_found);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}