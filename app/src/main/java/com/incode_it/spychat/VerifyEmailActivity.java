package com.incode_it.spychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.pin.FragmentPin;

public class VerifyEmailActivity extends AppCompatActivity implements FragmentPin.FragmentPinListener {

    private boolean requestPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_email_code);
        setResult(RESULT_CANCELED);
        requestPin = getIntent().getBooleanExtra(C.EXTRA_REQUEST_PIN, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        requestPin = true;
    }

    @Override
    protected void onPause() {
        requestPin = true;
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        requestPin = false;
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        showPinDialog();
        super.onResume();
    }

    private void showPinDialog()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isPinOn = sharedPreferences.getBoolean(C.SETTING_PIN, false);
        if (isPinOn && requestPin)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(FragmentPin.TAG);
            if (prev != null) {
                ft.remove(prev);
                ft.addToBackStack(null);
                ft.commit();
            }

            ft = getSupportFragmentManager().beginTransaction();
            FragmentPin fragmentPin = FragmentPin.newInstance();
            fragmentPin.show(ft, FragmentPin.TAG);
        }
    }

    @Override
    public void onSecurityClose() {
        setResult(C.RESULT_EXIT);
        finish();
    }

    @Override
    public void onSecurityLogOut() {
        setResult(C.RESULT_EXIT);
        Intent intent = new Intent(this, ActivityAuth.class);
        startActivity(intent);
        finish();
    }
}
