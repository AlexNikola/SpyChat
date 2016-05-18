package com.incode_it.spychat;

import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class ActivityForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private TextView errorQuestionTextView;
    private TextView errorNewPassTextView;
    private TextView errorNewPassConfTextView;

    private TextInputEditText questionET;
    private TextInputEditText newPassET;
    private TextInputEditText newPassConfET;

    private View changePassBtn;
    private View changePassBtnText;
    private View progressBarView;

    private View clearQuestion;
    private View clearNewPass;
    private View clearNewPassConf;

    private String phoneNumber;

    private View questionBg;
    private View progressBarQuestion;

    private TextView phoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        phoneNumber = getIntent().getStringExtra(C.EXTRA_MY_PHONE_NUMBER);


        errorQuestionTextView = (TextView)findViewById(R.id.error_key_word);
        errorNewPassTextView = (TextView)findViewById(R.id.error_new_pass);
        errorNewPassConfTextView = (TextView)findViewById(R.id.error_new_pass_conf);

        questionET = (TextInputEditText)findViewById(R.id.et_key_word);
        newPassET = (TextInputEditText)findViewById(R.id.et_new_pass);
        newPassConfET = (TextInputEditText)findViewById(R.id.et_new_pass_conf);

        changePassBtn = findViewById(R.id.change_password_button);
        changePassBtnText = findViewById(R.id.change_password_button_text);
        progressBarView = findViewById(R.id.progressBar);

        clearQuestion = findViewById(R.id.clear_key_word);
        clearNewPass = findViewById(R.id.clear_new_pass);
        clearNewPassConf = findViewById(R.id.clear_new_pass_conf);

        questionBg = findViewById(R.id.question_bg);
        progressBarQuestion = findViewById(R.id.progressBar_question);

        phoneTextView = (TextView) findViewById(R.id.phone_tv);
        assert phoneTextView != null;
        phoneTextView.setText(phoneNumber);

        new GetQuestionTask().execute(phoneNumber);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.clear_key_word:
                questionET.setText("");
                break;
            case R.id.clear_new_pass:
                newPassET.setText("");
                break;
            case R.id.clear_new_pass_conf:
                newPassConfET.setText("");
                break;
            case R.id.change_password_button:
                changePassword();
                break;
        }
    }

    private void changePassword()
    {
        String answer = questionET.getText().toString();
        String newPassword = newPassET.getText().toString();
        String newPasswordConf = newPassConfET.getText().toString();
        boolean isValid = true;
        if (answer.length() < 1) {
            errorQuestionTextView.setText(R.string.enter_answer);
            isValid = false;
        } else errorQuestionTextView.setText("");

        if (!newPassword.equals(newPasswordConf))
        {
            errorNewPassTextView.setText(R.string.passwords_not_match);
            errorNewPassConfTextView.setText(R.string.passwords_not_match);
            isValid = false;
        }
        else
        {
            if (newPassword.length() == 0)
            {
                errorNewPassTextView.setText(R.string.enter_password);
                errorNewPassConfTextView.setText(R.string.enter_password);
                isValid = false;
            }
            else if (newPassword.length() < 6)
            {
                errorNewPassTextView.setText(R.string.short_password);
                errorNewPassConfTextView.setText(R.string.short_password);
                isValid = false;
            }
            else
            {
                errorNewPassTextView.setText("");
                errorNewPassConfTextView.setText("");
            }
        }

        if (!isValid) return;

        new ChangePasswordTask().execute(answer, newPassword);
    }


    /*get secret question
    curl 'http://localhost:7777/api/v1/users/getSecretQuestion' -X POST -d "phone=0635491921"
    */

    private class GetQuestionTask extends AsyncTask<String, Void, String>
    {

        public GetQuestionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarQuestion.setVisibility(View.VISIBLE);
            questionET.setEnabled(false);
            questionBg.setBackgroundResource(R.drawable.bg_question_disabled);
        }

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];

            try {
                phoneNumber = URLEncoder.encode(phoneNumber+"8", "UTF-8");

                String urlParameters =  "phone=" + phoneNumber;

                URL url = new URL(C.BASE_URL + "api/v1/users/getSecretQuestion/");

                return MyConnection.post(url, urlParameters, null);
            }
            catch (IOException e)
            {
                Log.e("q", "my err " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBarQuestion.setVisibility(View.INVISIBLE);
            questionET.setEnabled(true);
            questionBg.setBackgroundResource(R.drawable.bg_phone_input);

            if (result == null) {
                Log.e("forg", "result == null");
                //fragmentListener.onError("Connection error");
            } else {
                Log.e("forg", "result "+ result);
                /*try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");

                        //fragmentListener.onAuthorizationSuccess(accessToken, refreshToken, countryCode + myPhoneNumber);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("There is an existing user connected to this phone number.")) {
                            //errorPhoneTextView.setText(R.string.existing_user);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }

        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class ChangePasswordTask extends AsyncTask<String, Void, String>
    {

        public ChangePasswordTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarView.setVisibility(View.VISIBLE);
            changePassBtnText.setVisibility(View.INVISIBLE);
            changePassBtn.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String answer = params[0];
            String newPassword = params[1];
            String regToken;

            try {
                answer = URLEncoder.encode(answer, "UTF-8");
                newPassword = URLEncoder.encode(newPassword, "UTF-8");

                String urlParameters =  "phone="    + phoneNumber   + "&" +
                        "password=" + newPassword      + "&" +
                        "confirm="  + newPassword      + "&" +
                        "answer=" + answer;

                URL url = new URL(C.BASE_URL + "api/v1/users/restorePassword/");

                return MyConnection.post(url, urlParameters, null);
            }
            catch (IOException e)
            {
                Log.e("q", "my err " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            changePassBtn.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            changePassBtnText.setVisibility(View.VISIBLE);

            if (result == null) {
                //fragmentListener.onError("Connection error");
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");

                        //fragmentListener.onAuthorizationSuccess(accessToken, refreshToken, countryCode + myPhoneNumber);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("There is an existing user connected to this phone number.")) {
                            //errorPhoneTextView.setText(R.string.existing_user);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
