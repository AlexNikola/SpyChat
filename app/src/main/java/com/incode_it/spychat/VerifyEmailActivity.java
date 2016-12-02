package com.incode_it.spychat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.authorization.FragmentSingUp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class VerifyEmailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        setResult(RESULT_CANCELED);
    }

    public static class VerifyEmailFragment extends FragmentLoader {

        private String email;
        private String code = "";

        private TextInputEditText codeET;

        private TextView errorCodeTextView;

        private View signUpBtnText;
        private View progressBarView;
        private View signUpBtn;

        public VerifyEmailFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Intent intent = getActivity().getIntent();
            email = intent.getStringExtra(FragmentSingUp.EXTRA_EMAIL);
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
            startTask(code);
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
            String code = params[0];

            String result = null;
            try {
                result = tryVerifyEmail(code);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        private String tryVerifyEmail(String param) throws IOException, JSONException {
            String code = param;
            code = URLEncoder.encode(code, "UTF-8");

            String urlParameters =  "code=" + code;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
            URL url = new URL(C.BASE_URL + "api/v1/usersJob/checkUpdateCode/");
            String header = "Bearer "+accessToken;

            String response = MyConnection.post(url, urlParameters, header);

            if (response.equals("Access token is expired")) {
                if (MyConnection.sendRefreshToken(getContext()))
                    response = tryVerifyEmail(param);
            }
            Log.d("myreg", "verifyemail: " + response);
            return response;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                }
            } else {

                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        sharedPreferences.edit().putString(C.SHARED_MY_EMAIL, email).apply();
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.setResult(Activity.RESULT_OK);
                            activity.finish();
                        }
                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("Code vertification fail")){
                            if (errorCodeTextView != null) errorCodeTextView.setText("Code verification fail");
                        } else {
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
