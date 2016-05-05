package com.incode_it.spychat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.incode_it.spychat.authorization.ActivityAuth;

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent intent = new Intent(ActivitySplash.this, ActivityAuth.class);
                    startActivity(intent);
                    finish();
                }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
