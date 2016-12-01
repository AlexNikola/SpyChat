package com.incode_it.spychat.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import static com.incode_it.spychat.C.REQUEST_CODE_CHECK_EMAIL;

public class ActivityChangeForgottenPass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_change_forgotten_pass);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public static class ChangeForgottenPassFragment extends FragmentLoader {

        private String email = "";

        private TextInputEditText codeET;
        private TextInputEditText passET;
        private TextInputEditText passConfET;

        private TextView errorCodeTextView;
        private TextView errorPassTextView;
        private TextView errorPassConfTextView;

        private View okBtnText;
        private View progressBarView;
        private View okBtn;

        public ChangeForgottenPassFragment() {
        }


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            email = getActivity().getIntent().getStringExtra(FragmentLogIn.EXTRA_EMAIL);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_change_forgotten_pass, container, false);
            initCodeInputLayout(view);
            initPassInputLayout(view);
            initPassConfInputLayout(view);

            errorCodeTextView = (TextView) view.findViewById(R.id.error_code);
            errorPassTextView = (TextView) view.findViewById(R.id.error_password);
            errorPassConfTextView = (TextView) view.findViewById(R.id.error_confirm);

            okBtnText = view.findViewById(R.id.ok_btn_text);
            progressBarView = view.findViewById(R.id.progressBar);

            okBtn = view.findViewById(R.id.ok_btn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ok();
                }
            });

            return view;
        }

        private void initCodeInputLayout(View view)
        {
            codeET = (TextInputEditText) view.findViewById(R.id.code);
            view.findViewById(R.id.clear_code).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    codeET.setText("");
                }
            });
        }

        private void initPassInputLayout(View view)
        {
            passET = (TextInputEditText) view.findViewById(R.id.password);

            view.findViewById(R.id.clear_password).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passET.setText("");
                }
            });
        }

        private void initPassConfInputLayout(View view)
        {
            passConfET = (TextInputEditText) view.findViewById(R.id.confirm);

            view.findViewById(R.id.clear_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passConfET.setText("");
                }
            });
        }


        private void ok()
        {
            String code = codeET.getText().toString();
            String password = passET.getText().toString();
            String passwordConf = passConfET.getText().toString();

            boolean isValid = true;

            if (code.length() == 0) {
                errorCodeTextView.setText(R.string.enter_code);
                isValid = false;
            } else {
                errorCodeTextView.setText("");
            }

            if (!password.equals(passwordConf)) {
                errorPassTextView.setText(R.string.passwords_not_match);
                errorPassConfTextView.setText(R.string.passwords_not_match);
                isValid = false;
            } else {
                if (password.length() == 0) {
                    errorPassTextView.setText(R.string.enter_password);
                    errorPassConfTextView.setText(R.string.enter_password);
                    isValid = false;
                } else if (password.length() < 6) {
                    errorPassTextView.setText(R.string.short_password);
                    errorPassConfTextView.setText(R.string.short_password);
                    isValid = false;
                } else {
                    errorPassTextView.setText("");
                    errorPassConfTextView.setText("");
                }
            }

            if (!isValid) return;

            hideKeyBoard();
            startTask(email, password, passwordConf, code);
        }




        @Override
        protected void onLoadingStateChanged(boolean isLoading) {
            if (isLoading) {
                progressBarView.setVisibility(View.VISIBLE);
                okBtnText.setVisibility(View.INVISIBLE);
                okBtn.setEnabled(false);
            } else {
                okBtn.setEnabled(true);
                progressBarView.setVisibility(View.INVISIBLE);
                okBtnText.setVisibility(View.VISIBLE);
            }
        }


        @Override
        public String doInBackground(String... params) {
            String email = params[0];
            String password = params[1];
            String confirm = params[2];
            String code = params[3];
            String regToken;

            try {
                email = URLEncoder.encode(email, "UTF-8");
                password = URLEncoder.encode(password, "UTF-8");
                confirm = URLEncoder.encode(confirm, "UTF-8");
                code = URLEncoder.encode(code, "UTF-8");

                regToken = MyConnection.getRegToken(getContext());

                String urlParameters =
                        "email="    + email         + "&" +
                        "password=" + password      + "&" +
                        "confirm="  + confirm       + "&" +
                        "code="     + code          + "&" +
                        "regToken=" + regToken;

                URL url = new URL(C.BASE_URL + "api/v1/users/check-pwd-code/");
                return MyConnection.post(url, urlParameters, null);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("myreg", "changeforgottenpass: " + result);
            if (result == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String res = jsonResponse.getString("result");
                    if (res.equals("success")) {
                        if (getContext() == null) return;
                        String accessToken = jsonResponse.getString("accessToken");
                        String refreshToken = jsonResponse.getString("refreshToken");

                        JSONObject jsonUserData = jsonResponse.getJSONObject("userData");

                        String phone = jsonUserData.getString("phone");
                        String email = jsonUserData.getString("email");
                        Intent intent = new Intent();
                        intent.putExtra(FragmentLogIn.EXTRA_PONE_NUMBER, phone);
                        intent.putExtra(FragmentLogIn.EXTRA_EMAIL, email);
                        intent.putExtra(FragmentLogIn.EXTRA_ACCESS_TOKEN, accessToken);
                        intent.putExtra(FragmentLogIn.EXTRA_REFRESH_TOKEN, refreshToken);
                        getActivity().setResult(RESULT_OK, intent);
                        getActivity().finish();

                    } else if (res.equals("error")) {
                        String message = jsonResponse.getString("message");
                        if (message.equals("Code vertification fail")) {
                            errorCodeTextView.setText("Code verification fail");
                        } else if (message.equals("User is not activated")) {
                            errorCodeTextView.setText("User is not activated");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }



    }




}
