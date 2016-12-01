package com.incode_it.spychat.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.incode_it.spychat.BaseActivity;
import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.OrientationUtils;
import com.incode_it.spychat.R;
import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.pin.FragmentPin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class ActivityChangePassword extends BaseActivity implements View.OnClickListener {

    private CoordinatorLayout coordinatorLayout;
    private SharedPreferences sharedPreferences;

    private TextView errorOldPassTextView;
    private TextView errorNewPassTextView;
    private TextView errorNewPassConfTextView;

    private TextInputEditText oldPassET;
    private TextInputEditText newPassET;
    private TextInputEditText newPassConfET;

    private View changePassBtn;

    private View clearOldPass;
    private View clearNewPass;
    private View clearNewPassConf;

    private ProgressDialog pd;

    @Override
    protected void onPause() {
        super.onPause();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        errorOldPassTextView = (TextView)findViewById(R.id.error_old_pass);
        errorNewPassTextView = (TextView)findViewById(R.id.error_new_pass);
        errorNewPassConfTextView = (TextView)findViewById(R.id.error_new_pass_conf);

        oldPassET = (TextInputEditText)findViewById(R.id.et_old_pass);
        newPassET = (TextInputEditText)findViewById(R.id.et_new_pass);
        newPassConfET = (TextInputEditText)findViewById(R.id.et_new_pass_conf);

        changePassBtn = findViewById(R.id.change_password_button);
        assert changePassBtn != null;
        changePassBtn.setOnClickListener(this);

        clearOldPass = findViewById(R.id.clear_old_pass);
        assert clearOldPass != null;
        clearOldPass.setOnClickListener(this);
        clearNewPass = findViewById(R.id.clear_new_pass);
        assert clearNewPass != null;
        clearNewPass.setOnClickListener(this);
        clearNewPassConf = findViewById(R.id.clear_new_pass_conf);
        assert clearNewPassConf != null;
        clearNewPassConf.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        hideKeyBoard();
        switch (v.getId())
        {
            case R.id.clear_old_pass:
                oldPassET.setText("");
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
        String oldPassword = oldPassET.getText().toString();
        String newPassword = newPassET.getText().toString();
        String newPasswordConf = newPassConfET.getText().toString();
        boolean isValid = true;
        if (oldPassword.length() == 0) {
            errorOldPassTextView.setText(R.string.enter_password);
            isValid = false;
        } else {
            errorOldPassTextView.setText("");
        }

        if (!newPassword.equals(newPasswordConf)) {
            errorNewPassTextView.setText(R.string.passwords_not_match);
            errorNewPassConfTextView.setText(R.string.passwords_not_match);
            isValid = false;
        } else {
            if (newPassword.length() == 0) {
                errorNewPassTextView.setText(R.string.enter_password);
                errorNewPassConfTextView.setText(R.string.enter_password);
                isValid = false;
            } else if (newPassword.length() < 6) {
                errorNewPassTextView.setText(R.string.short_password);
                errorNewPassConfTextView.setText(R.string.short_password);
                isValid = false;
            } else {
                errorNewPassTextView.setText("");
                errorNewPassConfTextView.setText("");
            }
        }

        if (!isValid) return;

        new ChangePasswordTask().execute(oldPassword, newPassword);
    }

    private class ChangePasswordTask extends AsyncTask<String, Void, String> {

        public ChangePasswordTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            OrientationUtils.lockOrientation(ActivityChangePassword.this);
            pd = ProgressDialog.show(ActivityChangePassword.this, "Changing password", "Wait...", true, false);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String oldPassword = params[0];
            String newPassword = params[1];

            String result = null;
            try {
                result = tryChangePassword(oldPassword, newPassword);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            OrientationUtils.unlockOrientation(ActivityChangePassword.this);
            pd.dismiss();
            if (result == null) {
                showError("Connection error", Color.RED, Color.YELLOW);
            } else {
                Log.d("myreg", "changepass: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        showError("Password has been changed", Color.GREEN, Color.GREEN);
                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("Wrong password.")) {
                            errorOldPassTextView.setText(R.string.incorrect_password);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private String tryChangePassword(String oldPassword, String newPassword) throws IOException, JSONException {
            String urlParameters = "newPass=" + URLEncoder.encode(newPassword, "UTF-8") + "&" +
                    "oldPass=" + URLEncoder.encode(oldPassword, "UTF-8") + "&" +
                    "confirm=" + URLEncoder.encode(newPassword, "UTF-8");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityChangePassword.this);
            String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
            URL url = new URL(C.BASE_URL + "api/v1/usersJob/updatePassword/");
            String header = "Bearer "+accessToken;

            String response = MyConnection.post(url, urlParameters, header);

            if (response.equals("Access token is expired")) {
                if (MyConnection.sendRefreshToken(ActivityChangePassword.this))
                    response = tryChangePassword(oldPassword, newPassword);
            }
            return response;
        }
    }

    public void showError(String error, int actionColor, int textColor) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(actionColor)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(textColor);
        snackbar.show();
    }

    protected void hideKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
