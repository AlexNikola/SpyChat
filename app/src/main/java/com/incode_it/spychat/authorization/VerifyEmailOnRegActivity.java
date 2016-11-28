package com.incode_it.spychat.authorization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.FragmentLoader;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class VerifyEmailOnRegActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email_on_reg);
        setResult(RESULT_CANCELED);
    }

    public static class VerifyEmailOnRegFragment extends FragmentLoader {

        private String phone;
        private String email;
        private String password;
        private String code = "";

        private TextInputEditText codeET;

        private TextView errorCodeTextView;

        private View signUpBtnText;
        private View progressBarView;
        private View signUpBtn;

        public VerifyEmailOnRegFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Intent intent = getActivity().getIntent();
            phone = intent.getStringExtra(FragmentSingUp.EXTRA_PONE_NUMBER);
            email = intent.getStringExtra(FragmentSingUp.EXTRA_EMAIL);
            password = intent.getStringExtra(FragmentSingUp.EXTRA_PASSWORD);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_verify_email, container, false);

            codeET = (TextInputEditText) view.findViewById(R.id.edit_text_code);
            view.findViewById(R.id.edit_text_clear_code).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    codeET.setText("");
                }
            });
            errorCodeTextView = (TextView) view.findViewById(R.id.error_code);
            signUpBtnText = view.findViewById(R.id.verify_button_text);
            progressBarView = view.findViewById(R.id.progressBar);

            signUpBtn = view.findViewById(R.id.verify_button);
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

            code = codeET.getText().toString();
            if (code.length() == 0) {
                errorCodeTextView.setText(R.string.enter_code);
                isValid = false;
            } else {
                errorCodeTextView.setText("");
            }

            if (!isValid) return;

            hideKeyBoard();
            startTask(phone, email, password, code);
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
            String phone = params[0];
            String email = params[1];
            String password = params[2];
            String code = params[3];

            try {
                phone = URLEncoder.encode(phone, "UTF-8");
                email = URLEncoder.encode(email, "UTF-8");
                password = URLEncoder.encode(password, "UTF-8");
                code = URLEncoder.encode(code, "UTF-8");

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
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            /*Intent intent = new Intent();
            intent.putExtra(FragmentSingUp.EXTRA_PONE_NUMBER, phone);
            intent.putExtra(FragmentSingUp.EXTRA_EMAIL, email);
            intent.putExtra(FragmentSingUp.EXTRA_ACCESS_TOKEN, "");
            intent.putExtra(FragmentSingUp.EXTRA_REFRESH_TOKEN, "");
            Activity activity = getActivity();
            if (activity != null) {
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }*/
            Log.d("myreg", "regcode: " + result);
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
