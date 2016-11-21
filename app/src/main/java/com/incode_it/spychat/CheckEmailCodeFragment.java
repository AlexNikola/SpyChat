package com.incode_it.spychat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.authorization.FragmentSingUp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class CheckEmailCodeFragment extends Fragment {

    private String phone;
    private String email;
    private String password;

    private TextInputEditText codeET;

    private TextView errorCodeTextView;

    private View signUpBtnText;
    private View progressBarView;
    private View signUpBtn;

    public CheckEmailCodeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Intent intent = getActivity().getIntent();
        phone = intent.getStringExtra(FragmentSingUp.EXTRA_PONE_NUMBER);
        email = intent.getStringExtra(FragmentSingUp.EXTRA_EMAIL);
        password = intent.getStringExtra(FragmentSingUp.EXTRA_PASSWORD);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_email_code, container, false);

        codeET = (TextInputEditText) view.findViewById(R.id.edit_text_code);
        view.findViewById(R.id.edit_text_clear_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeET.setText("");
            }
        });
        errorCodeTextView = (TextView) view.findViewById(R.id.error_code);
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
        boolean isValid = true;

        String code = codeET.getText().toString();
        if (code.length() == 0) {
            errorCodeTextView.setText(R.string.enter_code);
            isValid = false;
        } else {
            errorCodeTextView.setText("");
        }

        if (!isValid) return;

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        new SignUpTask().execute(code);
    }


    private class SignUpTask extends AsyncTask<String, Void, String>
    {

        public SignUpTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarView.setVisibility(View.VISIBLE);
            signUpBtnText.setVisibility(View.INVISIBLE);
            signUpBtn.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String code = params[0];
            if (getContext() == null) return null;

            try {
                phone = URLEncoder.encode(phone, "UTF-8");
                email = URLEncoder.encode(email, "UTF-8");
                password = URLEncoder.encode(password, "UTF-8");

                String regToken = MyConnection.getRegToken(getContext());

                String urlParameters =  "phone="    + phone      + "&" +
                                        "email="    + email      + "&" +
                                        "password=" + password   + "&" +
                                        "confirm="  + password   + "&" +
                                        "regToken=" + regToken   + "&" +
                                        "code="     + code;

                URL url = new URL(C.BASE_URL + "api/v1/users/checkRegCode/");

                return MyConnection.post(url, urlParameters, null);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            signUpBtn.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            signUpBtnText.setVisibility(View.VISIBLE);

            if (result == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");

                        Intent intent = new Intent();
                        intent.putExtra(FragmentSingUp.EXTRA_PONE_NUMBER, phone);
                        intent.putExtra(FragmentSingUp.EXTRA_EMAIL, email);
                        intent.putExtra(FragmentSingUp.EXTRA_ACCESS_TOKEN, accessToken);
                        intent.putExtra(FragmentSingUp.EXTRA_REFRESH_TOKEN, refreshToken);
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.setResult(Activity.RESULT_OK, intent);
                            activity.finish();
                        }


                    } else if (res.equals("error")) {
                        if (errorCodeTextView != null) errorCodeTextView.setText("Invalid code");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
