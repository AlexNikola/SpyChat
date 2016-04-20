package com.incode_it.spychat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<MyChatRecyclerViewAdapter.MessageViewHolder>
{
    ArrayList<Message> messages;
    MyContacts.Contact contact;
    Bitmap contactBitmap;
    AppCompatActivity activity;

    public MyChatRecyclerViewAdapter(ArrayList<Message> messages, MyContacts.Contact contact, Bitmap contactBitmap, AppCompatActivity activity) {
        this.messages = messages;
        this.contact = contact;
        this.contactBitmap = contactBitmap;
        this.activity = activity;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == Message.MY_MESSAGE)
        {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message_item, parent, false);
        }
        else
        {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.not_my_message_item, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder messageHolder, int position) {
        messageHolder.text.setText(messages.get(position).getMessage());
        if (!messages.get(position).getSenderPhoneNumber().equals(ActivityMain.myPhoneNumber))
        {
            if (contactBitmap == null) messageHolder.imageView.setImageBitmap(MyContactRecyclerViewAdapter.noPhotoBitmap);
            else messageHolder.imageView.setImageBitmap(contactBitmap);

        }
        else messageHolder.imageView.setImageBitmap(MyContactRecyclerViewAdapter.noPhotoBitmap);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageView;
        public final TextView text;
        public View textContainer;

        public MessageViewHolder(View view) {
            super(view);
            mView = view;
            text = (TextView) view.findViewById(R.id.text_message);
            imageView = (ImageView) view.findViewById(R.id.image);
            textContainer = view.findViewById(R.id.text_container);
            textContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(activity.getSupportFragmentManager(), "datePicker");*/
                    DialogFragment newFragment = new TimePickerFragment();
                    newFragment.show(activity.getSupportFragmentManager(), "timePicker");
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderPhoneNumber().equals(ActivityMain.myPhoneNumber))
        {
            return Message.MY_MESSAGE;
        }
        else return Message.NOT_MY_MESSAGE;
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

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
            // Do something with the time chosen by the user
            AlarmManager alarmMgr;
            PendingIntent pendingIntent;

            alarmMgr = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
        }
    }

}

