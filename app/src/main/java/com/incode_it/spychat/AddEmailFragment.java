package com.incode_it.spychat;

import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.authorization.FragmentLogIn;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class AddEmailFragment extends Fragment {

    private TextInputEditText phoneET;
    private TextInputEditText emailET;

    private TextView errorPhoneTextView;
    private TextView errorEmailTextView;

    private View addEmailBtnText;
    private View progressBarView;
    private View addEmailBtnView;

    public AddEmailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_email, container, false);

        initPhoneInputLayout(view);
        initEmailInputLayout(view);

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
        errorEmailTextView = (TextView) view.findViewById(R.id.error_email);

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
        String phone = phoneET.getText().toString();
        String email = emailET.getText().toString();
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

        new AddEmailTask().execute(phone, email);
    }


    public boolean validCellPhone(String number) {
        return Patterns.PHONE.matcher(number).matches();
    }

    public boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    private void initEmailInputLayout(View view) {
        emailET = (TextInputEditText) view.findViewById(R.id.et_email);
        view.findViewById(R.id.clear_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailET.setText("");
            }
        });
    }





    private class AddEmailTask extends AsyncTask<String, Void, String>
    {
        public AddEmailTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarView.setVisibility(View.VISIBLE);
            addEmailBtnText.setVisibility(View.INVISIBLE);
            addEmailBtnView.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];
            String email = params[1];
            try
            {
                phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
                email = URLEncoder.encode(email, "UTF-8");
                String urlParameters =
                                "phone="    + phoneNumber   + "&" +
                                "email="    + email;

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
        protected void onPostExecute(String result) {
            Log.d("chatm", "trySendMessage: " + result);
            addEmailBtnView.setEnabled(true);
            progressBarView.setVisibility(View.INVISIBLE);
            addEmailBtnText.setVisibility(View.VISIBLE);

            if (result == null) {
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
            }

        }
    }

}
