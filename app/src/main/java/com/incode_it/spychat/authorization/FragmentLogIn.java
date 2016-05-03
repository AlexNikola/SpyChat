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

public class FragmentLogIn extends Fragment
{
    private static final String TAG = "myhttp";
    private Context context;
    private View view;

    private OnFragmentInteractionListener fragmentListener;
    private TextInputEditText phoneET;
    private TextInputEditText passET;

    private TextView errorPhoneTextView;
    private TextView errorPassTextView;

    private View logInBtnText;
    private View progressBarView;
    private View logInBtn;


    public FragmentLogIn() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null)
        {
            Log.d("qaz", "onCreateView " + view.hashCode());
            return view;
        }
        else Log.d("qaz", "onCreateView view = null");

        view = inflater.inflate(R.layout.fragment_log_in, container, false);

        initPhoneInputLayout(view);
        initPassInputLayout(view);

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
        errorPassTextView = (TextView) view.findViewById(R.id.error_pass);

        logInBtnText = view.findViewById(R.id.log_in_button_text);
        progressBarView = view.findViewById(R.id.progressBar);

        logInBtn = view.findViewById(R.id.log_in_button);
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneET.getText().toString();
                String password = passET.getText().toString();
                boolean isValid = true;
                if (phoneNumber.length() < 1)
                {
                    errorPhoneTextView.setText(R.string.enter_phone_number);
                    isValid = false;
                }
                else errorPhoneTextView.setText("");
                if (password.length() < 6)
                {
                    Log.d("qaz", "Password is too short " + password.length());
                    if (password.length() == 0) errorPassTextView.setText(R.string.enter_password);
                    else errorPassTextView.setText(R.string.short_password);
                    isValid = false;
                }
                else errorPassTextView.setText("");
                if (!isValid) return;

                fragmentListener.onLogIn();
                new LogInTask().execute(phoneNumber, password);
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

    private class LogInTask extends AsyncTask<String, Void, String>
    {

        public LogInTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarView.setVisibility(View.VISIBLE);
            logInBtnText.setVisibility(View.INVISIBLE);
            logInBtn.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];
            String password = params[1];
            String regToken;
            try
            {
                regToken = MyConnection.getRegToken(context);

                phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
                String urlParameters = "phone=" + phoneNumber + "&" +
                        "password=" + password + "&" +
                        "regToken=" + regToken;

                URL url = new URL(C.BASE_URL + "api/v1/auth/getAccessToke/");

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
            logInBtn.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            logInBtnText.setVisibility(View.VISIBLE);

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
                        startActivity(intent);

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

}