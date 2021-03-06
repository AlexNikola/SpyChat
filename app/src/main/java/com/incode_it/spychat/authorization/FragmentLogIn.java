package com.incode_it.spychat.authorization;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.incode_it.spychat.interfaces.OnFragmentsAuthorizationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLEncoder;

public class FragmentLogIn extends FragmentLoader implements TextWatcher {
    public static final String EXTRA_PONE_NUMBER = "EXTRA_PONE_NUMBER";
    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";
    public static final String EXTRA_ACCESS_TOKEN = "EXTRA_ACCESS_TOKEN";
    public static final String EXTRA_REFRESH_TOKEN = "EXTRA_REFRESH_TOKEN";

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == C.REQUEST_CODE_FORGOT_PASSWORD && resultCode == Activity.RESULT_OK ||
                requestCode == C.REQUEST_CODE_CHANGE_FORGOTTEN_PASSWORD && resultCode == Activity.RESULT_OK) {
            String accessToken = data.getStringExtra(EXTRA_ACCESS_TOKEN);
            String refreshToken = data.getStringExtra(EXTRA_REFRESH_TOKEN);
            String phone = data.getStringExtra(EXTRA_PONE_NUMBER);
            String email = data.getStringExtra(EXTRA_EMAIL);

            fragmentListener.onLogInSuccess(accessToken, refreshToken, phone, email);
        }
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

        View logInByCodeView = view.findViewById(R.id.code_login);
        logInByCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogInByCodeClicked();
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
                /*JSONObject object = new JSONObject();
                try {
                    object.put("title", "Spy title");
                    object.put("text", "Spy text");
                    resolveAdminNotification(object);

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        });


        return view;
    }

    /*private void resolveAdminNotification(JSONObject jsonObject) throws JSONException {
        String title = jsonObject.getString("title");
        String text = jsonObject.getString("text");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(text);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isSoundOn = sharedPreferences.getBoolean(C.SETTING_SOUND, true);
        boolean isVibrateOn = sharedPreferences.getBoolean(C.SETTING_VIBRATE, true);
        if (isSoundOn) notificationBuilder.setSound(defaultSoundUri);
        if (isVibrateOn) notificationBuilder.setVibrate(new long[] {1000, 1000} );

        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(500, notificationBuilder.build());
    }*/

    private void onForgotPassClicked()
    {
        errorPhoneTextView.setText("");
        Intent intent = new Intent(getContext(), ActivityForgotPassword.class);
        startActivityForResult(intent, C.REQUEST_CODE_FORGOT_PASSWORD);
    }

    private void onLogInByCodeClicked()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String email = sharedPreferences.getString(C.SHARED_MY_EMAIL, "");
        Intent intent = new Intent(getContext(), ActivityChangeForgottenPass.class);
        intent.putExtra(FragmentLogIn.EXTRA_EMAIL, email);
        startActivityForResult(intent, C.REQUEST_CODE_CHANGE_FORGOTTEN_PASSWORD);
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
        phoneET.addTextChangedListener(this);
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
        String result = null;
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String phoneNumber = params[0];
        String email = params[1];
        String password = params[2];
        String regToken;
        String version = String.valueOf(C.getAppVersion(getContext()));
        Log.d("myreg", "doInBackground: phoneNumber " + phoneNumber);

        try {
            regToken = MyConnection.getRegToken(getContext());

            phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
            email = URLEncoder.encode(email, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");
            String urlParameters =
                            "phone="    + phoneNumber   + "&" +
                            "email="    + email         + "&" +
                            "password=" + password      + "&" +
                            "regToken=" + regToken      + "&" +
                            "version="  + version;

            URL url = new URL(C.BASE_URL + "api/v1/auth/getAccessToke/");
            result = MyConnection.post(url, urlParameters, null);
            return result;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }



    @Override
    public void onPostExecute(String result) {
        super.onPostExecute(result);
        /*fragmentListener.onLogInSuccess("", "", "", "");
        Log.d("myreg", "login: " + result);
        if (true)return;*/
        if (result == null) {
            fragmentListener.onError("Connection Error");
        } else {
            //Log.d("myreg", "login: " + result);
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String res = jsonResponse.getString("result");
                if (res.equals("success")) {
                    String accessToken = jsonResponse.getString("accessToken");
                    String refreshToken = jsonResponse.getString("refreshToken");
                    String phone = jsonResponse.getString("phone");

                    String email = "";
                    if (jsonResponse.has("email")) {
                        email = jsonResponse.getString("email");
                    }



                    fragmentListener.onLogInSuccess(accessToken, refreshToken, phone, email);

                } else if (res.equals("error")) {
                    String message = jsonResponse.getString("message");
                    if (message.equals("Incorrect password")){
                        errorPassTextView.setText(R.string.incorrect_password);
                    } else if (message.equals("User not found")) {
                        errorPhoneTextView.setText(R.string.user_not_found);
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("myreg", "afterTextChanged: " + s);

        if (s.toString().startsWith("+")) {
            if (s.length() > 1 && !isOnlyContainsNumbers(s.toString().substring(1, s.length()))) {
                s.delete(0, 1);
            }
        } else if (!s.toString().startsWith("+") && isOnlyContainsNumbers(s.toString())) {
            s.insert(0, "+");
        }


        /*Привет. В SpyLink в роуте inSystem вместо "Access token is expired" приходит "". Надо будет исправить*/
    }
}