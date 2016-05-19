package com.incode_it.spychat.authorization;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class ActivityForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private CoordinatorLayout coordinatorLayout;

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
    private TextView questionTextView;

    private TextView phoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        phoneNumber = getIntent().getStringExtra(C.EXTRA_MY_PHONE_NUMBER);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        errorQuestionTextView = (TextView)findViewById(R.id.error_key_word);
        errorNewPassTextView = (TextView)findViewById(R.id.error_new_pass);
        errorNewPassConfTextView = (TextView)findViewById(R.id.error_new_pass_conf);

        questionET = (TextInputEditText)findViewById(R.id.et_key_word);
        newPassET = (TextInputEditText)findViewById(R.id.et_new_pass);
        newPassConfET = (TextInputEditText)findViewById(R.id.et_new_pass_conf);

        changePassBtn = findViewById(R.id.change_password_button);
        assert changePassBtn != null;
        changePassBtn.setOnClickListener(this);
        changePassBtnText = findViewById(R.id.change_password_button_text);
        progressBarView = findViewById(R.id.progressBar);

        clearQuestion = findViewById(R.id.clear_key_word);
        clearQuestion.setOnClickListener(this);
        clearNewPass = findViewById(R.id.clear_new_pass);
        clearNewPass.setOnClickListener(this);
        clearNewPassConf = findViewById(R.id.clear_new_pass_conf);
        clearNewPassConf.setOnClickListener(this);

        questionBg = findViewById(R.id.question_bg);
        progressBarQuestion = findViewById(R.id.progressBar_question);

        phoneTextView = (TextView) findViewById(R.id.phone_tv);
        assert phoneTextView != null;
        phoneTextView.setText(phoneNumber);
        questionTextView = (TextView) findViewById(R.id.question_tv);

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


    private class GetQuestionTask extends AsyncTask<String, Void, String>
    {

        public GetQuestionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarQuestion.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String phoneNumber = params[0];

            try {
                phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");

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

            if (result == null) {
                showError("Connection error");

            } else {
                Log.e("forg", "result "+ result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {

                        String secret = jsonResponse.getString("secret");
                        questionTextView.setText(secret);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("User not found")) {
                            //errorPhoneTextView.setText(R.string.existing_user);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

            try {
                answer = URLEncoder.encode(answer, "UTF-8");
                newPassword = URLEncoder.encode(newPassword, "UTF-8");

                String urlParameters =  "phone="    + URLEncoder.encode(phoneNumber, "UTF-8")   + "&" +
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
                showError("Connection error");
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        finish();

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("User not found")) {
                            showError("User not found");
                        }
                        else if (message.equals("Wrong answer to the secret question"))
                        {
                            errorQuestionTextView.setText(R.string.wrong_answer);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void showError(String error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_INDEFINITE)
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
}
