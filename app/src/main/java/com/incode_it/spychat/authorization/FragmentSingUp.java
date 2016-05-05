package com.incode_it.spychat.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.contacts.ActivityMain;
import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;
import com.incode_it.spychat.interfaces.OnFragmentInteractionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class FragmentSingUp extends Fragment
{
    private View view;
    private static final String TAG = "myhttp";
    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private TextInputEditText phoneET;
    private TextInputEditText passET;
    private TextInputEditText passConfET;

    private TextView errorPhoneTextView;
    private TextView errorPassTextView;
    private TextView errorPassConfTextView;

    private View signUpBtnText;
    private View progressBarView;
    private View signUpBtn;


    public FragmentSingUp() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fragmentListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        initPhoneInputLayout(view);
        initPassInputLayout(view);
        initPassConfInputLayout(view);

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
        errorPassTextView = (TextView) view.findViewById(R.id.error_pass);
        errorPassConfTextView = (TextView) view.findViewById(R.id.error_pass_conf);

        signUpBtnText = view.findViewById(R.id.sign_up_button_text);
        progressBarView = view.findViewById(R.id.progressBar);

        signUpBtn = view.findViewById(R.id.sign_up_button);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneET.getText().toString();
                String password = passET.getText().toString();
                String passwordConf = passConfET.getText().toString();
                boolean isValid = true;
                if (phoneNumber.length() < 1) {
                    errorPhoneTextView.setText(R.string.enter_phone_number);
                    isValid = false;
                } else errorPhoneTextView.setText("");

                if (!password.equals(passwordConf))
                {
                    errorPassTextView.setText(R.string.passwords_not_match);
                    errorPassConfTextView.setText(R.string.passwords_not_match);
                    isValid = false;
                }
                else
                {
                    if (password.length() == 0)
                    {
                        errorPassTextView.setText(R.string.enter_password);
                        errorPassConfTextView.setText(R.string.enter_password);
                        isValid = false;
                    }
                    else if (password.length() < 6)
                    {
                        errorPassTextView.setText(R.string.short_password);
                        errorPassConfTextView.setText(R.string.short_password);
                        isValid = false;
                    }
                    else
                    {
                        errorPassTextView.setText("");
                        errorPassConfTextView.setText("");
                    }
                }

                if (!isValid) return;

                fragmentListener.onLogIn();
                new SignUpTask().execute(phoneNumber, password);
            }
        });

        return view;
    }

    private void initPhoneInputLayout(View view)
    {
        phoneET = (TextInputEditText) view.findViewById(R.id.edit_text_phone);
        phoneET.setText(ActivityAuth.getPhoneNumber(getContext()));

        view.findViewById(R.id.edit_text_clear_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneET.setText("");
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
            String phoneNumber = params[0];
            String password = params[1];
            String regToken;

            try {
                phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
                password = URLEncoder.encode(password, "UTF-8");
                /*String urlParameters = "phone=" +
                        phoneNumber +
                        "&" +
                        "password=" +
                        password +
                        "&" +
                        "confirm=" +
                        password;

                URL url = new URL(C.BASE_URL + "api/v1/users/restorePassword/");*/
                regToken = MyConnection.getRegToken(context);

                String urlParameters =  "phone="    + phoneNumber   + "&" +
                                        "password=" + password      + "&" +
                                        "confirm="  + password      + "&" +
                                        "regToken=" + regToken;

                URL url = new URL(C.BASE_URL + "api/v1/users/register/");

                return MyConnection.post(url, urlParameters, null);
            }
            catch (IOException e)
            {
                Log.e(TAG, "my err " + e.getLocalizedMessage());
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
                fragmentListener.onError("Connection error");
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPreferences.edit().putString(C.ACCESS_TOKEN, accessToken).putString(C.REFRESH_TOKEN, refreshToken).apply();
                        Intent intent = new Intent(context, ActivityMain.class);
                        intent.putExtra(C.REQUEST_PIN, false);
                        startActivity(intent);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("There is an existing user connected to this phone number.")) {
                            errorPhoneTextView.setText(R.string.existing_user);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}