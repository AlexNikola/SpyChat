package com.incode_it.spychat.authorization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.FragmentLoader;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;
import com.incode_it.spychat.country_selection.ActivitySelectCountry;
import com.incode_it.spychat.interfaces.OnFragmentsAuthorizationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import static com.incode_it.spychat.C.REQUEST_CODE_CHECK_EMAIL;

public class FragmentSingUp extends FragmentLoader {
    private static final String TAG = "myhttp";
    private Context context;

    public static final String EXTRA_PONE_NUMBER = "EXTRA_PONE_NUMBER";
    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";
    public static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";
    public static final String EXTRA_ACCESS_TOKEN = "EXTRA_ACCESS_TOKEN";
    public static final String EXTRA_REFRESH_TOKEN = "EXTRA_REFRESH_TOKEN";

    private OnFragmentsAuthorizationListener fragmentListener;
    private TextInputEditText phoneET;
    private TextInputEditText emailET;
    private TextInputEditText passET;
    private TextInputEditText passConfET;

    private TextView errorPhoneTextView;
    private TextView errorEmailTextView;
    private TextView errorPassTextView;
    private TextView errorPassConfTextView;

    private View signUpBtnText;
    private View progressBarView;
    private View signUpBtn;
    private View selectCountryBtnView;
    private TextView countryCodeTextView;

    private String phoneNumber;
    private String email;
    private String password;
    private String countryCode = "+....";
    private String countryISO = "";

    public FragmentSingUp() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fragmentListener = (OnFragmentsAuthorizationListener) context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == C.REQUEST_CODE_SELECT_COUNTRY) {
            if (resultCode == Activity.RESULT_OK) {
                countryCode = data.getStringExtra(C.EXTRA_COUNTRY_CODE);
                countryISO = data.getStringExtra(C.EXTRA_COUNTRY_ISO);
                countryCodeTextView.setText(countryCode);
            }
        } else if (requestCode == REQUEST_CODE_CHECK_EMAIL) {
            if (resultCode == Activity.RESULT_OK) {

                String accessToken = data.getStringExtra(EXTRA_ACCESS_TOKEN);
                String refreshToken = data.getStringExtra(EXTRA_REFRESH_TOKEN);
                String myPhoneNumber = data.getStringExtra(EXTRA_PONE_NUMBER);
                String email = data.getStringExtra(EXTRA_EMAIL);
                fragmentListener.onSignUpSuccess(accessToken, refreshToken, myPhoneNumber, email);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        if (countryCode.equals("+....")) {
            if (ActivityAuth.myCountryCode != null) countryCode = ActivityAuth.myCountryCode;
        }

        if (countryISO.equals("")) {
            if (ActivityAuth.myCountryISO != null) countryISO = ActivityAuth.myCountryISO;
        }

        if (ActivityAuth.myPhoneNumber != null){
            phoneNumber = ActivityAuth.myPhoneNumber;
        }

        initPhoneInputLayout(view);
        initEmailInputLayout(view);
        initPassInputLayout(view);
        initPassConfInputLayout(view);
        initSelectCountryView(view);

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
        errorEmailTextView = (TextView) view.findViewById(R.id.error_email);
        errorPassTextView = (TextView) view.findViewById(R.id.error_pass);
        errorPassConfTextView = (TextView) view.findViewById(R.id.error_pass_conf);

        signUpBtnText = view.findViewById(R.id.sign_up_button_text);
        progressBarView = view.findViewById(R.id.progressBar);

        signUpBtn = view.findViewById(R.id.sign_up_button);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpClicked();
            }
        });

        return view;
    }

    private void onSignUpClicked()
    {
        phoneNumber = countryCode + phoneET.getText().toString();
        email = emailET.getText().toString();
        password = passET.getText().toString();
        String passwordConf = passConfET.getText().toString();

        boolean isValid = true;

        if (phoneNumber.equals("+....")) {
            errorPhoneTextView.setText(R.string.enter_phone_number);
            isValid = false;
        } else if (!validCellPhone(phoneNumber)) {
            errorPhoneTextView.setText(R.string.incorrect_input);
            isValid = false;
        } else {
            errorPhoneTextView.setText("");
        }

        if (email.length() == 0) {
            errorEmailTextView.setText(R.string.enter_email);
            isValid = false;
        } else if (!validEmail(email)) {
            errorEmailTextView.setText(R.string.email_not_valid);
            isValid = false;
        } else {
            errorEmailTextView.setText("");
        }

        if (!password.equals(passwordConf)) {
            errorPassTextView.setText(R.string.passwords_not_match);
            errorPassConfTextView.setText(R.string.passwords_not_match);
            isValid = false;
        } else {
            if (password.length() == 0) {
                errorPassTextView.setText(R.string.enter_password);
                errorPassConfTextView.setText(R.string.enter_password);
                isValid = false;
            } else if (password.length() < 6) {
                errorPassTextView.setText(R.string.short_password);
                errorPassConfTextView.setText(R.string.short_password);
                isValid = false;
            } else {
                errorPassTextView.setText("");
                errorPassConfTextView.setText("");
            }
        }

        if (!isValid) return;

        hideKeyBoard();
        startTask(phoneNumber, email, password);
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

    private void initPhoneInputLayout(View view) {
        phoneET = (TextInputEditText) view.findViewById(R.id.edit_text_phone);
        if (ActivityAuth.myPhoneNumber != null) {
            if (phoneNumber.startsWith(countryCode)) {
                String ph = phoneNumber.substring(countryCode.length());
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

    private void initEmailInputLayout(View view)
    {
        emailET = (TextInputEditText) view.findViewById(R.id.edit_text_email);
        view.findViewById(R.id.edit_text_clear_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailET.setText("");
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

    private void initPassConfInputLayout(View view)
    {
        passConfET = (TextInputEditText) view.findViewById(R.id.edit_text_pass_conf);

        view.findViewById(R.id.edit_text_clear_pass_conf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passConfET.setText("");
            }
        });
    }




    @Override
    protected void onLoadingStateChanged(boolean isLoading) {
        if (isLoading) {
            progressBarView.setVisibility(View.VISIBLE);
            signUpBtnText.setVisibility(View.INVISIBLE);
            signUpBtn.setEnabled(false);
        } else {
            signUpBtn.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            signUpBtnText.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public String doInBackground(String... params) {
        String phoneNumber = params[0];
        String email = params[1];
        String password = params[2];
        String regToken;

        try {
            phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
            email = URLEncoder.encode(email, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");

            regToken = MyConnection.getRegToken(getContext());

            String urlParameters =  "phone="    + phoneNumber   + "&" +
                    "email="    + email         + "&" +
                    "password=" + password      + "&" +
                    "confirm="  + password      + "&" +
                    "regToken=" + regToken;

            URL url = new URL(C.BASE_URL + "api/v1/users/register/");

            return MyConnection.post(url, urlParameters, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onPostExecute(String result) {
        super.onPostExecute(result);
        /*Intent intent = new Intent(getContext(), VerifyEmailOnRegActivity.class);
        intent.putExtra(EXTRA_PONE_NUMBER, phoneNumber);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);

        startActivityForResult(intent, C.REQUEST_CODE_CHECK_EMAIL);*/
        //Log.d("myreg", "onPostExecute: " + result);
        if (result == null) {
            fragmentListener.onError("Connection error");
        } else {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String res = jsonResponse.getString("result");
                if (res.equals("success")) {
                    if (getContext() == null) return;
                    Intent intent = new Intent(getContext(), VerifyEmailOnRegActivity.class);
                    intent.putExtra(EXTRA_PONE_NUMBER, phoneNumber);
                    intent.putExtra(EXTRA_EMAIL, email);
                    intent.putExtra(EXTRA_PASSWORD, password);

                    startActivityForResult(intent, REQUEST_CODE_CHECK_EMAIL);

                } else if (res.equals("error")) {
                    String message = jsonResponse.getString("message");
                    if (message.equals("There is an existing user connected to this phone number or email.")) {
                        errorPhoneTextView.setText(R.string.existing_user);
                    } else
                    {
                        fragmentListener.onError("Error");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }




}