package com.incode_it.spychat.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.R;
import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.interfaces.OnMessageDialogListener;
import com.incode_it.spychat.pin.FragmentPin;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class ActivityChat extends AppCompatActivity implements FragmentChat.OnFragmentChatInteractionListener, FragmentPin.FragmentPinListener {

    private SharedPreferences sharedPreferences;
    private boolean requestPin = false;
    String phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        phone = getIntent().getStringExtra(C.PHONE_NUMBER);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fr = fragmentManager.findFragmentByTag(FragmentChat.TAG_FRAGMENT);
        if (fr == null)
        {
            FragmentChat fragment = FragmentChat.newInstance(phone);
            fragmentTransaction.add(R.id.fragment_chat_container, fragment, FragmentChat.TAG_FRAGMENT);
            fragmentTransaction.commit();
        }
    }





    private void openVideoCamera()
    {

    }

    private void openMedia()
    {

    }










    @Override
    public void onCreateSuccessMessageDialog(OnMessageDialogListener listener) {
        DialogFragment newFragment = SuccessMessageDialogFragment.newInstance(listener);
        newFragment.show(getSupportFragmentManager(), SuccessMessageDialogFragment.TAG);
    }

    @Override
    public void onCreateErrorMessageDialog(OnMessageDialogListener listener) {
        DialogFragment newFragment = ErrorMessageDialogFragment.newInstance(listener);
        newFragment.show(getSupportFragmentManager(), ErrorMessageDialogFragment.TAG);
    }

    @Override
    public void onCreateTimeDialog(final OnMessageDialogListener listener) {
        TimePickerDialog tpd = TimePickerDialog.newInstance(null, 0, 0, true);

        tpd.vibrate(true);
        tpd.setAccentColor(getResources().getColor(R.color.colorPrimary));
        tpd.setTitle("Message timer");
        tpd.enableSeconds(true);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });
        tpd.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                long timer = (hourOfDay * 60 * 60 * 1000) + (minute * 60 * 1000) + (second * 1000);
                long removalTime = System.currentTimeMillis() + timer;
                listener.onApplyTime(removalTime, timer);
            }
        });
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onSecurityClose() {
        Intent intent = new Intent();
        setResult(C.SECURITY_EXIT, intent);
        finish();
    }

    @Override
    public void onSecurityLogOut() {
        sharedPreferences.edit().remove(C.SHARED_ACCESS_TOKEN).remove(C.SHARED_REFRESH_TOKEN).apply();
        Intent intent = new Intent(this, ActivityAuth.class);
        startActivity(intent);

        intent = new Intent();
        setResult(C.SECURITY_EXIT, intent);
        finish();
    }


    public static class SuccessMessageDialogFragment extends DialogFragment {

        public static final String TAG = "SuccessMessageDialogFragment";
        private OnMessageDialogListener listener;

        public static SuccessMessageDialogFragment newInstance(OnMessageDialogListener listener)
        {
            SuccessMessageDialogFragment newFragment = new SuccessMessageDialogFragment();
            newFragment.setListener(listener);
            return newFragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.success_dialog)
                    .setItems(R.array.success_massage_dialog, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                    listener.onSetTime();
                                    break;

                                case 1:
                                    listener.onDeleteMessage();
                                    break;
                            }
                        }
                    });

            return builder.create();
        }

        public void setListener(OnMessageDialogListener listener) {
            this.listener = listener;
        }
    }

    public static class ErrorMessageDialogFragment extends DialogFragment {

        public static final String TAG = "ErrorMessageDialogFragment";

        private OnMessageDialogListener listener;

        public static ErrorMessageDialogFragment newInstance(OnMessageDialogListener listener)
        {
            ErrorMessageDialogFragment newFragment = new ErrorMessageDialogFragment();
            newFragment.setListener(listener);
            return newFragment;
        }

        public void setListener(OnMessageDialogListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.error_dialog)
                    .setItems(R.array.error_massage_dialog, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                    listener.onReSendMessage();
                                    break;

                                case 1:
                                    listener.onDeleteMessage();
                                    break;
                            }
                        }
                    });

            return builder.create();
        }
    }



    public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // For our recurring task, we'll just display a message
            Log.d("myalarm", "onReceive "+context.hashCode() + " " + intent.hashCode());
            Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onPause() {
        /*LocalBroadcastManager.getInstance(this).unregisterReceiver(mPinReceiver);
        isPinReceiverRegistered = false;*/
        requestPin = true;
        Log.d("lifes", "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        //registerPinReceiver();
        Log.d("lifes", "onResume");
        showPinDialog();
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        requestPin = false;
        Log.d("lifes", "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void showPinDialog()
    {
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
