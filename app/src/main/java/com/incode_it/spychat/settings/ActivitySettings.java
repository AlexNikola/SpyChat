package com.incode_it.spychat.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.OrientationUtils;
import com.incode_it.spychat.R;
import com.incode_it.spychat.authorization.ActivityAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class ActivitySettings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Switch switchSound, switchVibration, switchPin;
    private SharedPreferences sharedPreferences;
    private EditText pinInput_0, pinInput_1, pinInput_2, pinInput_3;
    private Button pinSave, pinClear;
    private View container;
    private Toolbar toolbar;
    private Button deleteAccountBtn;
    private ProgressDialog pd;
    private CoordinatorLayout coordinatorLayout;
    private Button changePasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        container = findViewById(R.id.container);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        initToolbar();

        initSoundSettings();
        initVibrationSettings();
        initPinSettings();
        initDeleteAccountBtn();

        changePasswordBtn = (Button) findViewById(R.id.change_password);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySettings.this, ActivityChangePassword.class);
                startActivity(intent);
            }
        });
    }

    private void initDeleteAccountBtn()
    {
        deleteAccountBtn = (Button) findViewById(R.id.delete_account);
        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteAccountTask().execute();
            }
        });
    }

    private void initSoundSettings()
    {
        boolean isSoundOn = sharedPreferences.getBoolean(C.SETTING_SOUND, true);

        switchSound = (Switch) findViewById(R.id.sett_sound);
        assert switchSound != null;
        switchSound.setOnCheckedChangeListener(this);
        switchSound.setChecked(isSoundOn);
    }

    private void initVibrationSettings()
    {
        boolean isVibrateOn = sharedPreferences.getBoolean(C.SETTING_VIBRATE, true);

        switchVibration = (Switch) findViewById(R.id.sett_vibrate);
        assert switchVibration != null;
        switchVibration.setOnCheckedChangeListener(this);
        switchVibration.setChecked(isVibrateOn);
    }

    private void initPinSettings()
    {
        boolean isPinSecured = sharedPreferences.getBoolean(C.SETTING_PIN, false);

        switchPin = (Switch) findViewById(R.id.sett_pin);
        assert switchPin != null;
        switchPin.setOnCheckedChangeListener(this);

        pinInput_0 = (EditText) findViewById(R.id.pin_editText_0);
        pinInput_1 = (EditText) findViewById(R.id.pin_editText_1);
        pinInput_2 = (EditText) findViewById(R.id.pin_editText_2);
        pinInput_3 = (EditText) findViewById(R.id.pin_editText_3);

        pinInput_0.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pinInput_1.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pinInput_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pinInput_2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pinInput_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pinInput_3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pinSave = (Button) findViewById(R.id.pin_save);
        assert pinSave != null;
        pinSave.setOnClickListener(this);
        pinClear = (Button) findViewById(R.id.pin_clear);
        assert pinClear != null;
        pinClear.setOnClickListener(this);

        String pinCode = sharedPreferences.getString(C.SHARED_PIN, "0000");
        if (pinCode.equals("0000"))
        {
            pinInput_0.setText("0");
            pinInput_1.setText("0");
            pinInput_2.setText("0");
            pinInput_3.setText("0");
        }
        else
        {
            pinInput_0.setText("*");
            pinInput_1.setText("*");
            pinInput_2.setText("*");
            pinInput_3.setText("*");
        }

        switchPin.setChecked(isPinSecured);

        pinInput_0.setEnabled(isPinSecured);
        pinInput_1.setEnabled(isPinSecured);
        pinInput_2.setEnabled(isPinSecured);
        pinInput_3.setEnabled(isPinSecured);
        pinSave.setEnabled(isPinSecured);
        pinClear.setEnabled(isPinSecured);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId())
        {
            case R.id.sett_sound:
                sharedPreferences.edit().putBoolean(C.SETTING_SOUND, isChecked).apply();
                break;
            case R.id.sett_vibrate:
                sharedPreferences.edit().putBoolean(C.SETTING_VIBRATE, isChecked).apply();
                break;
            case R.id.sett_pin:
                sharedPreferences.edit().putBoolean(C.SETTING_PIN, isChecked).apply();
                setEnabledDisabled(isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.pin_save:
                hideKeyBoard();
                savePin();
                break;

            case R.id.pin_clear:
                clearPin();
                break;
        }
    }

    private void savePin()
    {
        String pin_0 = pinInput_0.getText().toString();
        String pin_1 = pinInput_1.getText().toString();
        String pin_2 = pinInput_2.getText().toString();
        String pin_3 = pinInput_3.getText().toString();

        String pinCode = pin_0 + pin_1 + pin_2 + pin_3;
        if (pinCode.length() == 4)
        {
            sharedPreferences.edit().putString(C.SHARED_PIN, pinCode).apply();
            showSnackBar(R.string.pin_saved, Color.GREEN);
        }
        else
        {
            showSnackBar(R.string.pin_error, Color.RED);
        }
    }

    private void showSnackBar(int text, int color)
    {
        Snackbar snackbar = Snackbar.make(container, text, Snackbar.LENGTH_SHORT);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void clearPin()
    {
        pinInput_0.setText("");
        pinInput_1.setText("");
        pinInput_2.setText("");
        pinInput_3.setText("");
        pinInput_0.requestFocus();
    }

    private void setEnabledDisabled(boolean isEnabled)
    {
        pinInput_0.setEnabled(isEnabled);
        pinInput_1.setEnabled(isEnabled);
        pinInput_2.setEnabled(isEnabled);
        pinInput_3.setEnabled(isEnabled);
        pinSave.setEnabled(isEnabled);
        pinClear.setEnabled(isEnabled);
    }

    private void initToolbar()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void hideKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class DeleteAccountTask extends AsyncTask<String, Void, String>
    {
        public DeleteAccountTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            OrientationUtils.lockOrientation(ActivitySettings.this);

            pd = ProgressDialog.show(ActivitySettings.this, "Deleting account", "Wait...", true, false);
            /*pd = new ProgressDialog(ActivitySettings.this);
            pd.setTitle("Deleting account");
            pd.setMessage("Wait...");
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String result = null;
            try
            {
                result = tryDeleteAccount();
            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            OrientationUtils.unlockOrientation(ActivitySettings.this);
            pd.dismiss();

            if (result == null) {
                shoeError("Connection error");
            } else if (result.equals("success")){
                sharedPreferences.edit().remove(C.SHARED_ACCESS_TOKEN).remove(C.SHARED_REFRESH_TOKEN).apply();
                Intent intent = new Intent(ActivitySettings.this, ActivityAuth.class);
                startActivity(intent);
                setResult(C.RESULT_EXIT);
                finish();
            }

        }

        private String tryDeleteAccount() throws IOException, JSONException
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivitySettings.this);
            String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
            URL url = new URL(C.BASE_URL + "api/v1/usersJob/dropAccount/");
            String header = "Bearer "+accessToken;

            String response = MyConnection.post(url, null, header);

            String result = null;
            if (response.equals("Access token is expired"))
            {
                if (MyConnection.sendRefreshToken(ActivitySettings.this))
                    result = tryDeleteAccount();
            }
            else
            {
                JSONObject jsonResponse = new JSONObject(response);
                result = jsonResponse.getString("result");
            }

            return result;
        }
    }

    public void shoeError(String error) {
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
