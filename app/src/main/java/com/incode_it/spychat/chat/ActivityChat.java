package com.incode_it.spychat.chat;

import android.app.AlertDialog;
import android.app.Dialog;
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

import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyTimePickerDialog;
import com.incode_it.spychat.OrientationUtils;
import com.incode_it.spychat.R;
import com.incode_it.spychat.authorization.ActivityAuth;
import com.incode_it.spychat.interfaces.OnMessageDialogListener;
import com.incode_it.spychat.pin.FragmentPin;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class ActivityChat extends AppCompatActivity implements
        FragmentChat.OnFragmentChatInteractionListener,
        FragmentPin.FragmentPinListener
{
    private SharedPreferences sharedPreferences;
    private boolean requestPin;
    private String phone;

    FragmentChat fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setResult(RESULT_OK);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        requestPin = getIntent().getBooleanExtra(C.EXTRA_REQUEST_PIN, false);
        phone = getIntent().getStringExtra(C.EXTRA_OPPONENT_PHONE_NUMBER);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragment = (FragmentChat) fragmentManager.findFragmentByTag(FragmentChat.TAG_FRAGMENT);
        Log.d("fdfsfs", "onCreate: " + fragment);
        if (fragment == null)
        {
            fragment = FragmentChat.newInstance(phone);
            fragmentTransaction.add(R.id.fragment_chat_container, fragment, FragmentChat.TAG_FRAGMENT);
            fragmentTransaction.commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("qwerty", "OnActivityResult");
        requestPin = true;
    }

    @Override
    public void onCreateSuccessMessageDialog(OnMessageDialogListener listener) {
        OrientationUtils.lockOrientation(this);
        DialogFragment newFragment = SuccessMessageDialogFragment.newInstance(listener);
        newFragment.show(getSupportFragmentManager(), SuccessMessageDialogFragment.TAG);
    }

    @Override
    public void onCreateErrorMessageDialog(OnMessageDialogListener listener, int type) {
        OrientationUtils.lockOrientation(this);
        DialogFragment newFragment = ErrorMessageDialogFragment.newInstance(listener, type);
        newFragment.show(getSupportFragmentManager(), ErrorMessageDialogFragment.TAG);
    }

    @Override
    public void onCreateTimeDialog(final OnMessageDialogListener listener) {

        TimePickerDialog tpd = MyTimePickerDialog.newInstance(null, 0, 0, true);

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
        tpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OrientationUtils.unlockOrientation(ActivityChat.this);
            }
        });
        tpd.show(getFragmentManager(), "Timepickerdialog");
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


    public static class SuccessMessageDialogFragment extends DialogFragment {

        public static final String TAG = "SuccessMessageDialogFragment";
        private OnMessageDialogListener listener;
        private boolean doUnlock = true;

        public static SuccessMessageDialogFragment newInstance(OnMessageDialogListener listener)
        {
            SuccessMessageDialogFragment newFragment = new SuccessMessageDialogFragment();
            newFragment.setListener(listener);
            return newFragment;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (doUnlock)
            OrientationUtils.unlockOrientation(getActivity());
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
                                    doUnlock = false;
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
        private int type;

        public static ErrorMessageDialogFragment newInstance(OnMessageDialogListener listener, int type)
        {
            ErrorMessageDialogFragment newFragment = new ErrorMessageDialogFragment();
            newFragment.setListener(listener);
            newFragment.setType(type);
            return newFragment;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            OrientationUtils.unlockOrientation(getActivity());
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setListener(OnMessageDialogListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int arr;
            if (type == Message.MY_MESSAGE_TEXT || type == Message.MY_MESSAGE_IMAGE || type == Message.MY_MESSAGE_VIDEO)
            {
                arr = R.array.error_my_message_dialog;
            }
            else arr = R.array.error_not_my_message_dialog;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.error_dialog)
                    .setItems(arr, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                    listener.onDeleteMessage();

                                    break;

                                case 1:

                                    listener.onReSendMessage();
                                    break;
                            }
                        }
                    });

            return builder.create();
        }
    }



    @Override
    protected void onPause() {
        requestPin = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        showPinDialog();
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        requestPin = false;
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
        if (fragment.backPressed()) {
            super.onBackPressed();
        }
    }
}
