package com.incode_it.spychat.authorization;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.OrientationUtils;
import com.incode_it.spychat.R;
import com.incode_it.spychat.country_selection.ActivitySelectCountry;
import com.incode_it.spychat.interfaces.OnFragmentsAuthorizationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class FragmentSingUp extends Fragment implements OnDialogListener {
    private static final String TAG = "myhttp";
    private Context context;

    private OnFragmentsAuthorizationListener fragmentListener;
    private TextInputEditText phoneET;
    private TextInputEditText passET;
    private TextInputEditText passConfET;

    private TextView errorPhoneTextView;
    private TextView errorPassTextView;
    private TextView errorPassConfTextView;

    private View signUpBtnText;
    private View progressBarView;
    private View signUpBtn;
    private View selectCountryBtnView;
    private TextView countryCodeTextView;

    private String myPhoneNumber;
    private String countryCode = "+....";
    private String countryISO = "";

    private TextView questionTextView;
    private TextInputEditText answerET;
    private View clearAnswer;
    private TextView errorAnswer;
    private String [] questions;
    private String currentQuestion;
    private View selectQuestion;
    private int questionPosition;


    public FragmentSingUp() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        fragmentListener = (OnFragmentsAuthorizationListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == C.REQUEST_CODE_SELECT_COUNTRY) {
            if (resultCode == Activity.RESULT_OK) {
                countryCode = data.getStringExtra(C.EXTRA_COUNTRY_CODE);
                countryISO = data.getStringExtra(C.EXTRA_COUNTRY_ISO);
                countryCodeTextView.setText(countryCode);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        if (countryCode.equals("+...."))
        {
            if (ActivityAuth.myCountryCode != null) countryCode = ActivityAuth.myCountryCode;
        }
        if (countryISO.equals(""))
        {
            if (ActivityAuth.myCountryISO != null) countryISO = ActivityAuth.myCountryISO;
        }
        if (ActivityAuth.myPhoneNumber != null) myPhoneNumber = ActivityAuth.myPhoneNumber;

        initPhoneInputLayout(view);
        initPassInputLayout(view);
        initPassConfInputLayout(view);
        initSelectCountryView(view);

        errorPhoneTextView = (TextView) view.findViewById(R.id.error_phone);
        errorPassTextView = (TextView) view.findViewById(R.id.error_pass);
        errorPassConfTextView = (TextView) view.findViewById(R.id.error_pass_conf);

        signUpBtnText = view.findViewById(R.id.sign_up_button_text);
        progressBarView = view.findViewById(R.id.progressBar);

        signUpBtn = view.findViewById(R.id.sign_up_button);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPhoneNumber = phoneET.getText().toString();
                String password = passET.getText().toString();
                String passwordConf = passConfET.getText().toString();
                String answer = answerET.getText().toString();
                boolean isValid = true;
                if (myPhoneNumber.length() < 1) {
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

                if (answer.length() < 1)
                {
                    errorAnswer.setText(R.string.enter_answer);
                    isValid = false;
                }
                else
                {
                    errorAnswer.setText("");
                }

                if (!isValid) return;

                fragmentListener.onLogIn(countryCode + myPhoneNumber);
                new SignUpTask().execute(countryCode + myPhoneNumber, password, currentQuestion, answer);
            }
        });

        questionTextView = (TextView) view.findViewById(R.id.question_tv);
        answerET = (TextInputEditText) view.findViewById(R.id.edit_text_answer);
        clearAnswer = view.findViewById(R.id.edit_text_clear_answer);
        clearAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerET.setText("");
            }
        });
        errorAnswer = (TextView) view.findViewById(R.id.error_answer);

        questions = getResources().getStringArray(R.array.questions);
        if (currentQuestion == null) currentQuestion = questions[0];
        questionTextView.setText(currentQuestion);

        selectQuestion = view.findViewById(R.id.select_question);
        selectQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrientationUtils.lockOrientation(getActivity());
                FragmentManager fm = getActivity().getSupportFragmentManager();
                SelectQuestionDialogFragment fragment = SelectQuestionDialogFragment.newInstance(questionPosition, FragmentSingUp.this);

                fragment.show(fm, "fragment_select_question");
            }
        });

        return view;
    }

    private void initSelectCountryView(View view)
    {
        selectCountryBtnView = view.findViewById(R.id.select_country_btn_view);
        selectCountryBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivitySelectCountry.class);
                intent.putExtra(C.EXTRA_COUNTRY_ISO, countryISO);
                startActivityForResult(intent, C.REQUEST_CODE_SELECT_COUNTRY);
            }
        });

        countryCodeTextView = (TextView) view.findViewById(R.id.country_code);
        countryCodeTextView.setText(countryCode);
    }

    private void initPhoneInputLayout(View view)
    {
        phoneET = (TextInputEditText) view.findViewById(R.id.edit_text_phone);
        if (ActivityAuth.myPhoneNumber != null)
        {
            if (myPhoneNumber.startsWith(countryCode))
            {
                String ph = myPhoneNumber.substring(countryCode.length());
                phoneET.setText(ph);
            }
        }

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

    @Override
    public void onSelect(int position) {
        questionPosition = position;
        currentQuestion = questions[position];
        questionTextView.setText(currentQuestion);
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
            String question = params[2];
            String answer = params[3];
            String regToken;

            try {
                phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
                password = URLEncoder.encode(password, "UTF-8");
                question = URLEncoder.encode(question, "UTF-8");
                answer = URLEncoder.encode(answer, "UTF-8");

                regToken = MyConnection.getRegToken(context);

                String urlParameters =  "phone="    + phoneNumber   + "&" +
                                        "password=" + password      + "&" +
                                        "confirm="  + password      + "&" +
                                        "regToken=" + regToken      + "&" +
                                        "secret="   + question      + "&" +
                                        "answer="    + answer;

                URL url = new URL(C.BASE_URL + "api/v1/users/register/");

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

                        fragmentListener.onAuthorizationSuccess(accessToken, refreshToken, countryCode + myPhoneNumber);

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


    public static class SelectQuestionDialogFragment extends DialogFragment {

        public static final String TAG = "SelectQuestionDialogFragment";
        private OnDialogListener listener;
        private int position;

        public static SelectQuestionDialogFragment newInstance(int position, OnDialogListener listener)
        {
            SelectQuestionDialogFragment newFragment = new SelectQuestionDialogFragment();
            newFragment.setListener(listener);
            newFragment.setPosition(position);
            return newFragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_question)
                    .setSingleChoiceItems(R.array.questions, position, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            listener.onSelect(which);
                        }
                    });

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    OrientationUtils.unlockOrientation(getActivity());
                }
            });
            return builder.create();
        }

        public void setListener(OnDialogListener listener) {
            this.listener = listener;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }



}