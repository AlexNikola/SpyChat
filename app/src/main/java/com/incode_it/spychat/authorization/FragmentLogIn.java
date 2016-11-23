package com.incode_it.spychat.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.FragmentLoader;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;
import com.incode_it.spychat.interfaces.OnFragmentsAuthorizationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLEncoder;

public class FragmentLogIn extends FragmentLoader
{


    private OnFragmentsAuthorizationListener fragmentListener;
    private TextInputEditText phoneET;
    private TextInputEditText passET;

    private TextView errorPhoneTextView;
    private TextView errorPassTextView;

    private View logInBtnText;
    private View progressBarView;
    private View logInBtnView;

    private String myPhoneNumber = "";
    private String email = "";
    private String password = "";

    public FragmentLogIn() {
        // Required empty public constructor
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentListener = (OnFragmentsAuthorizationListener) context;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log_in, container, false);

        if (ActivityAuth.myPhoneNumber != null) myPhoneNumber = ActivityAuth.myPhoneNumber;

        initPhoneInputLayout(view);
        initPassInputLayout(view);

        View forgotPassView = view.findViewById(R.id.forgot);
        forgotPassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onForgotPassClicked();
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
                onLogInClicked();
            }
        });


        return view;
    }

    private void onForgotPassClicked()
    {
        errorPhoneTextView.setText("");
        Intent intent = new Intent(getContext(), ActivityForgotPassword.class);
        startActivity(intent);
    }

    private void onLogInClicked()
    {
        String str = phoneET.getText().toString();
        password = passET.getText().toString();
        boolean isValid = true;

        if (str.length() == 0) {
            errorPhoneTextView.setText(R.string.enter_phone_or_email);
            isValid = false;
        } else if (!validCellPhone(str) && !validEmail(str)) {
            errorPhoneTextView.setText(R.string.incorrect_input);
            isValid = false;
        } else if (validCellPhone(str)) {
            myPhoneNumber = str;
            email = "";
            errorPhoneTextView.setText("");
        } else if (validEmail(str)) {
            myPhoneNumber = "";
            email = str;
            errorPhoneTextView.setText("");
        }

        if (password.length() < 6) {
            if (password.length() == 0) errorPassTextView.setText(R.string.enter_password);
            else errorPassTextView.setText(R.string.short_password);
            isValid = false;
        } else {
            errorPassTextView.setText("");
        }

        if (!isValid) {
            return;
        }

        hideKeyBoard();
        startTask(myPhoneNumber, email, password);
    }



    private void initPhoneInputLayout(View view) {
        phoneET = (TextInputEditText) view.findViewById(R.id.edit_text_phone);
        if (ActivityAuth.myPhoneNumber != null) {
            phoneET.setText(myPhoneNumber);
        }

        TextInputLayout til = (TextInputLayout) view.findViewById(R.id.text_input_layout_phone);
        if (ActivityAuth.myCountryCode != null){
            til.setHint(ActivityAuth.myCountryCode + "... or email");
        } else {
            til.setHint("Phone or email");
        }

        view.findViewById(R.id.edit_text_clear_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneET.setText("");
            }
        });
    }

    private void initPassInputLayout(View view) {
        passET = (TextInputEditText) view.findViewById(R.id.edit_text_pass);
        view.findViewById(R.id.edit_text_clear_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passET.setText("");
            }
        });
    }






    @Override
    protected void onLoadingStateChanged(boolean isLoading) {
        Log.d(TAG, "onLoadingStateChanged: " + isLoading);
        if (isLoading) {
            progressBarView.setVisibility(View.VISIBLE);
            logInBtnText.setVisibility(View.INVISIBLE);
            logInBtnView.setEnabled(false);
        } else {
            logInBtnView.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            logInBtnText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String doInBackground(String... params) {
        Log.d(TAG, "doInBackground: ");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String phoneNumber = params[0];
        String email = params[1];
        String password = params[2];
        String regToken;

        try {
            regToken = MyConnection.getRegToken(getContext());

            phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
            email = URLEncoder.encode(email, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");
            String urlParameters =
                    "phone="    + phoneNumber   + "&" +
                            "email="    + email         + "&" +
                            "password=" + password      + "&" +
                            "regToken=" + regToken;

            URL url = new URL(C.BASE_URL + "api/v1/auth/getAccessToke/");

            return MyConnection.post(url, urlParameters, null);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) {
            fragmentListener.onError("Error");
        } else {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String res = jsonResponse.getString("result");
                if (res.equals("success")) {
                    String accessToken = jsonResponse.getString("accessToken");
                    String refreshToken = jsonResponse.getString("refreshToken");
                    //String phone = jsonResponse.getString("phone");
                    //String email = jsonResponse.getString("email");
                    String phone = myPhoneNumber;
                    String email = "";

                    fragmentListener.onLogInSuccess(accessToken, refreshToken, phone, email);

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