package com.incode_it.spychat.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.FragmentLoader;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.R;
import com.incode_it.spychat.VerifyEmailActivity;
import com.incode_it.spychat.authorization.ActivityAuth;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class FragmentChangeEmail extends FragmentLoader {

    public static final String EXTRA_PONE_NUMBER = "EXTRA_PONE_NUMBER";
    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";
    public static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";

    private TextInputEditText phoneET;
    private TextInputEditText emailET;
    private TextInputEditText passET;

    private TextView errorPhoneTextView;
    private TextView errorEmailTextView;
    private TextView errorPassTextView;

    private View addEmailBtnText;
    private View progressBarView;
    private View addEmailBtnView;

    private String phone = "";
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

        initPhoneInputLayout(view);
        initPassInputLayout(view);
        initEmailInputLayout(view);

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
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
        phone = phoneET.getText().toString();
        email = emailET.getText().toString();
        password = passET.getText().toString();
        boolean isValid = true;

        if (phone.length() == 0) {
            errorPhoneTextView.setText(R.string.enter_phone_number);
            isValid = false;
        } else if (!validCellPhone(phone)) {
            errorPhoneTextView.setText(R.string.incorrect_input);
            isValid = false;
        } else {
            errorPhoneTextView.setText("");
        }

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
        startTask(phone, email, password);
    }




    private void initPhoneInputLayout(View view) {
        phoneET = (TextInputEditText) view.findViewById(R.id.et_phone);
        if (ActivityAuth.myPhoneNumber != null) {
            phoneET.setText(ActivityAuth.getPhoneNumber(getContext()));
        }

        view.findViewById(R.id.clear_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneET.setText("");
            }
        });
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
        String phoneNumber = params[0];
        String email = params[1];
        String password = params[2];
        try
        {
            phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
            email = URLEncoder.encode(email, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");
            String urlParameters =
                    "phone="     + phoneNumber   + "&" +
                            "password="  + password      + "&" +
                            "email="     + email;

            URL url = new URL(C.BASE_URL + "api/v1/usersJob/update-email/");

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

        Intent intent = new Intent(getContext(), VerifyEmailActivity.class);
        intent.putExtra(EXTRA_PONE_NUMBER, phone);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);

        startActivityForResult(intent, C.REQUEST_CODE_CHECK_EMAIL);
        /*if (result == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String res = jsonResponse.getString("result");
                if (res.equals("success")) {


                } else if (res.equals("error")) {
                    String message = jsonResponse.getString("message");
                    if (message.equals("User not found")) {
                        errorPhoneTextView.setText(R.string.user_not_found);
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
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
