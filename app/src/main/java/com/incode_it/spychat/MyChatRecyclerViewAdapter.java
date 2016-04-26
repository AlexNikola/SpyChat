package com.incode_it.spychat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
    Context context;

    public MyChatRecyclerViewAdapter(ArrayList<Message> messages, MyContacts.Contact contact, Bitmap contactBitmap, Context context) {
        this.messages = messages;
        this.contact = contact;
        this.contactBitmap = contactBitmap;
        this.context = context;
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
        messageHolder.timeText.setText(messages.get(position).getDate());

        if (!messages.get(position).getSenderPhoneNumber().equals(ActivityMain.myPhoneNumber))
        {
            if (contactBitmap == null) messageHolder.imageView.setImageBitmap(MyContactRecyclerViewAdapter.noPhotoBitmap);
            else messageHolder.imageView.setImageBitmap(contactBitmap);

            messageHolder.textContainer.setBackgroundResource(R.drawable.bg_not_my_message);
            messageHolder.iconSent.setVisibility(View.INVISIBLE);
            messageHolder.progressBar.setVisibility(View.INVISIBLE);
            messageHolder.text.setTextColor(Color.parseColor("#000000"));
        }
        else
        {
            messageHolder.imageView.setImageBitmap(MyContactRecyclerViewAdapter.noPhotoBitmap);
            if (messages.get(position).state == Message.STATE_ADDED)
            {
                messageHolder.textContainer.setBackgroundResource(R.drawable.bg_my_message_added);
                messageHolder.iconSent.setVisibility(View.INVISIBLE);
                messageHolder.progressBar.setVisibility(View.VISIBLE);
                messageHolder.text.setTextColor(Color.parseColor("#55000000"));
            }
            else if (messages.get(position).state == Message.STATE_SUCCESS)
            {
                messageHolder.textContainer.setBackgroundResource(R.drawable.bg_my_message_success);
                messageHolder.iconSent.setVisibility(View.VISIBLE);
                messageHolder.progressBar.setVisibility(View.INVISIBLE);
                messageHolder.text.setTextColor(Color.parseColor("#000000"));
            }
            else if (messages.get(position).state == Message.STATE_ERROR)
            {
                messageHolder.textContainer.setBackgroundResource(R.drawable.bg_my_message_error);
                messageHolder.iconSent.setVisibility(View.INVISIBLE);
                messageHolder.progressBar.setVisibility(View.INVISIBLE);
                messageHolder.text.setTextColor(Color.parseColor("#000000"));
            }
        }


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

        public View progressBar;
        public View iconSent;
        public TextView timeText;

        public MessageViewHolder(View view) {
            super(view);
            mView = view;
            text = (TextView) view.findViewById(R.id.text_message);
            imageView = (ImageView) view.findViewById(R.id.image);
            progressBar = view.findViewById(R.id.progressBar);
            iconSent = view.findViewById(R.id.icon_sent);
            timeText = (TextView) view.findViewById(R.id.time_tv);
            textContainer = view.findViewById(R.id.text_container);
            textContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(context.getSupportFragmentManager(), "datePicker");*/
                    /*DialogFragment newFragment = new TimePickerFragment();
                    newFragment.show(context.getSupportFragmentManager(), "timePicker");*/
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

