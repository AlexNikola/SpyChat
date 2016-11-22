package com.incode_it.spychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.pin.FragmentPin;

public abstract class BaseActivity extends AppCompatActivity implements FragmentPin.FragmentPinListener {
    private boolean requestPin;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("BaseActivity", "onActivityResult: ");
        requestPin = false;
        if (resultCode == C.RESULT_EXIT)
        {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPin = getIntent().getBooleanExtra(C.EXTRA_REQUEST_PIN, false);
        Log.d(TAG, "onCreate: " + requestPin);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        requestPin = false;
        Log.d(TAG, "onRestoreInstanceState: " + requestPin);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: " + requestPin);
        showPinDialog();
        super.onResume();
    }

    @Override
    protected void onPause() {
        requestPin = true;
        Log.d(TAG, "onPause: " + requestPin);
        super.onPause();
    }

    private void showPinDialog()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isPinOn = sharedPreferences.getBoolean(C.SETTING_PIN, false);
        if (isPinOn && requestPin)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            DialogFragment prev = (DialogFragment) getSupportFragmentManager().findFragmentByTag(FragmentPin.TAG);
            Log.d(TAG, "showPinDialog: " + prev);
            if (prev != null) {
                prev.dismiss();
                /*ft.remove(prev);
                ft.addToBackStack(null);
                ft.commit();*/
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
