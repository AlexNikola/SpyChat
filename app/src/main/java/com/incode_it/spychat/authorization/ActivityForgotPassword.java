package com.incode_it.spychat.authorization;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.incode_it.spychat.R;

public class ActivityForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setResult(RESULT_CANCELED);
    }
}
