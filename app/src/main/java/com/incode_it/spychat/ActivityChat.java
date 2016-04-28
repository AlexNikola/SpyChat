package com.incode_it.spychat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class ActivityChat extends AppCompatActivity implements FragmentChat.OnFragmentChatInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String phone = getIntent().getStringExtra(C.PHONE_NUMBER);


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
    public void onCreateTimeDialog(OnMessageDialogListener listener) {
        DialogFragment newFragment = TimePickerFragment.newInstance(listener);
        newFragment.show(getSupportFragmentManager(), TimePickerFragment.TAG);
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

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        public static final String TAG = "TimePickerFragment";

        private OnMessageDialogListener listener;

        public static TimePickerFragment newInstance(OnMessageDialogListener listener)
        {
            TimePickerFragment fragment = new TimePickerFragment();
            fragment.setListener(listener);
            return fragment;
        }

        public void setListener(OnMessageDialogListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, 0, 0,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            listener.onApplyTime(hourOfDay, minute);
            // Do something with the time chosen by the user
            /*AlarmManager alarmMgr;
            PendingIntent pendingIntent;

            alarmMgr = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);*/
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
}
