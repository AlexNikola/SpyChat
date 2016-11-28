package com.incode_it.spychat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.pin.FragmentPin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class AddEmailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public static class AddEmailFragment extends FragmentLoader {

        public static final String EXTRA_EMAIL = "EXTRA_EMAIL";

        private TextInputEditText emailET;
        private TextInputEditText passET;

        private TextView errorEmailTextView;
        private TextView errorPassTextView;

        private View addEmailBtnText;
        private View progressBarView;
        private View addEmailBtnView;

        private String email = "";
        private String password = "";

        public AddEmailFragment() {
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
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_add_email, container, false);

            initPassInputLayout(view);
            initEmailInputLayout(view);

            errorEmailTextView = (TextView) view.findViewById(R.id.error_email);
            errorPassTextView = (TextView) view.findViewById(R.id.error_password);

            addEmailBtnText = view.findViewById(R.id.add_email_button_text);
            progressBarView = view.findViewById(R.id.progressBar);

            addEmailBtnView = view.findViewById(R.id.add_email_button);
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
                result = tryAddEmail(params);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        private String tryAddEmail(String... params) throws IOException, JSONException {
            String email = params[0];
            String password = params[1];
            email = URLEncoder.encode(email, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");

            String urlParameters =
                            "password="      + password      + "&" +
                            "newEmail="      + email;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");

            URL url = new URL(C.BASE_URL + "api/v1/usersJob/update-email/");
            String header = "Bearer "+accessToken;
            String response = MyConnection.post(url, urlParameters, header);

            if (response.equals("Access token is expired"))
            {
                if (MyConnection.sendRefreshToken(getContext()))
                    response = tryAddEmail(params);
            }

            return response;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("myreg", "addemail: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        Intent intent = new Intent(getContext(), VerifyEmailActivity.class);
                        intent.putExtra(EXTRA_EMAIL, email);
                        startActivityForResult(intent, C.REQUEST_CODE_CHECK_EMAIL);

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("Wrong password.")){
                            errorPassTextView.setText("Wrong password");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
