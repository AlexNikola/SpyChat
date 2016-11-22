package com.incode_it.spychat.authorization;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class ForgotPasswordFragment extends FragmentLoader {

    private TextInputEditText emailET;

    private TextView errorEmailTextView;

    private View sendEmailBtnText;
    private View progressBarViewEmail;
    private View sendEmailBtn;

    private String email = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailET = (TextInputEditText) view.findViewById(R.id.edit_text_email);
        emailET.setText(email);
        view.findViewById(R.id.edit_text_clear_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailET.setText("");
            }
        });
        errorEmailTextView = (TextView) view.findViewById(R.id.error_email);
        sendEmailBtnText = view.findViewById(R.id.send_email_btn_text);
        progressBarViewEmail = view.findViewById(R.id.progressBarEmail);

        sendEmailBtn = view.findViewById(R.id.send_email_btn);
        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendEmailClicked();
            }
        });

        return view;
    }


    private void onSendEmailClicked()
    {
        boolean isValid = true;

        email = emailET.getText().toString();
        if (email.length() == 0) {
            errorEmailTextView.setText(R.string.enter_email);
            isValid = false;
        } else if (!validEmail(email)) {
            errorEmailTextView.setText(R.string.incorrect_input);
            isValid = false;
        } else {
            errorEmailTextView.setText("");
        }

        if (!isValid) return;

        hideKeyBoard();

        startTask(email);
    }


    @Override
    protected void onLoadingStateChanged(boolean isLoading) {
        if (isLoading) {
            progressBarViewEmail.setVisibility(View.VISIBLE);
            sendEmailBtnText.setVisibility(View.INVISIBLE);
            sendEmailBtn.setEnabled(false);
        } else {
            sendEmailBtn.setEnabled(true);
            progressBarViewEmail.setVisibility(View.INVISIBLE);
            sendEmailBtnText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String doInBackground(String... params) {
        String email = params[0];

        try {
            email = URLEncoder.encode(email, "UTF-8");
            String urlParameters = "email=" + email;
            URL url = new URL(C.BASE_URL + "api/v1/users/reset-password/");

            return MyConnection.post(url, urlParameters, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String res = jsonResponse.getString("result");
                if (res.equals("success")) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                } else if (res.equals("error")) {
                    String message = jsonResponse.getString("message");
                    if (message.equals("User not found")) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
