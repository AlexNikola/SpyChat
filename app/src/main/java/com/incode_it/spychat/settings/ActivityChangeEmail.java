package com.incode_it.spychat.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.BaseActivity;
import com.incode_it.spychat.C;
import com.incode_it.spychat.FragmentLoader;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;
import com.incode_it.spychat.VerifyEmailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class ActivityChangeEmail extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public static class FragmentChangeEmail extends FragmentLoader {

        public static final String EXTRA_EMAIL = "EXTRA_EMAIL";
        public static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";

        private TextInputEditText emailET;
        private TextInputEditText passET;

        private TextView errorEmailTextView;
        private TextView errorPassTextView;

        private View addEmailBtnText;
        private View progressBarView;
        private View addEmailBtnView;

        private String email = "";
        private String password = "";

        private CoordinatorLayout coordinatorLayout;

        public FragmentChangeEmail() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == C.REQUEST_CODE_CHECK_EMAIL) {
                if (resultCode == Activity.RESULT_OK) {
                    showMessage("Email has been changed", Color.GREEN, Color.GREEN);
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_change_email, container, false);

            coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator);

            initPassInputLayout(view);
            initEmailInputLayout(view);

            errorPassTextView = (TextView) view.findViewById(R.id.error_password);
            errorEmailTextView = (TextView) view.findViewById(R.id.error_email);

            addEmailBtnText = view.findViewById(R.id.change_email_button_text);
            progressBarView = view.findViewById(R.id.progressBar);

            addEmailBtnView = view.findViewById(R.id.change_email_button);
            addEmailBtnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddEmailClicked();
                }
            });

            return view;
        }

        private void onAddEmailClicked()
        {
            email = emailET.getText().toString();
            password = passET.getText().toString();
            boolean isValid = true;

            if (password.length() < 6) {
                if (password.length() == 0) errorPassTextView.setText(R.string.enter_password);
                else errorPassTextView.setText(R.string.short_password);
                isValid = false;
            } else {
                errorPassTextView.setText("");
            }

            if (email.length() == 0) {
                errorEmailTextView.setText(R.string.enter_email);
                isValid = false;
            } else if (!validEmail(email)) {
                errorEmailTextView.setText(R.string.incorrect_input);
                isValid = false;
            } else {
                errorEmailTextView.setText("");
            }

            if (!isValid) {
                return;
            }

            hideKeyBoard();
            startTask(email, password);
        }



        private void initPassInputLayout(View view) {
            passET = (TextInputEditText) view.findViewById(R.id.et_password);

            view.findViewById(R.id.clear_password).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passET.setText("");
                }
            });
        }

        private void initEmailInputLayout(View view) {
            emailET = (TextInputEditText) view.findViewById(R.id.et_email);
            view.findViewById(R.id.clear_email).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emailET.setText("");
                }
            });
        }



        @Override
        protected void onLoadingStateChanged(boolean isLoading) {
            if (isLoading) {
                progressBarView.setVisibility(View.VISIBLE);
                addEmailBtnText.setVisibility(View.INVISIBLE);
                addEmailBtnView.setEnabled(false);
            } else {
                addEmailBtnView.setEnabled(true);
                progressBarView.setVisibility(View.INVISIBLE);
                addEmailBtnText.setVisibility(View.VISIBLE);
            }
        }


        @Override
        public String doInBackground(String... params) {
            String result = null;
            try {
                result = tryChangeEmail(params);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        private String tryChangeEmail(String... params) throws IOException, JSONException {
            String email = params[0];
            String password = params[1];
            email = URLEncoder.encode(email, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");

            String urlParameters =
                    "password="      + password      + "&" +
                    "newEmail="      + email;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
            String header = "Bearer "+accessToken;
            URL url = new URL(C.BASE_URL + "api/v1/usersJob/update-email/");

            String response = MyConnection.post(url, urlParameters, header);

            if (response.equals("Access token is expired"))
            {
                if (MyConnection.sendRefreshToken(getContext()))
                    response = tryChangeEmail(params);
            }

            return response;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);

            Intent intent = new Intent(getContext(), VerifyEmailActivity.class);
            intent.putExtra(EXTRA_EMAIL, email);
            startActivityForResult(intent, C.REQUEST_CODE_CHECK_EMAIL);

            /*if (result == null) {
                showMessage("response: null", Color.RED, Color.RED);
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        Intent intent = new Intent(getContext(), VerifyEmailActivity.class);
                        intent.putExtra(EXTRA_EMAIL, email);
                        startActivityForResult(intent, C.REQUEST_CODE_CHECK_EMAIL);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        showMessage("response: error", Color.RED, Color.RED);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }      */
        }

        public void showMessage(String error, int actionColor, int textColor) {
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
    }

}
