package com.incode_it.spychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<MyChatRecyclerViewAdapter.MessageViewHolder>
{
    ArrayList<Message> messages;
    MyContacts.Contact contact;
    Bitmap contactBitmap;
    FragmentChat.OnFragmentChatInteractionListener listener;
    OnChatAdapterListener chatAdapterListener;
    String myPhoneNumber;

    public MyChatRecyclerViewAdapter(ArrayList<Message> messages, MyContacts.Contact contact, Bitmap contactBitmap, FragmentChat.OnFragmentChatInteractionListener listener, OnChatAdapterListener chatAdapterListener) {
        this.messages = messages;
        this.contact = contact;
        this.contactBitmap = contactBitmap;
        this.listener = listener;
        this.chatAdapterListener = chatAdapterListener;

        TelephonyManager tm = (TelephonyManager)((Context)listener).getSystemService(Context.TELEPHONY_SERVICE);
        myPhoneNumber = tm.getLine1Number();
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
        Message message = messages.get(position);
        messageHolder.text.setText(message.getMessage());
        messageHolder.timeText.setText(message.getDate());

        messageHolder.timerTextView.setText("");
        messageHolder.startTimer();


        if (!messages.get(position).getSenderPhoneNumber().equals(myPhoneNumber))
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

    public class MessageViewHolder extends RecyclerView.ViewHolder implements OnMessageDialogListener {
        public final View mView;
        public final ImageView imageView;
        public final TextView text;
        public View textContainer;

        public View progressBar;
        public View iconSent;
        public TextView timeText;

        public TextView timerTextView;
        public MyTimerTask timerTask;

        public MessageViewHolder(View view) {
            super(view);
            mView = view;
            text = (TextView) view.findViewById(R.id.text_message);
            timerTextView = (TextView) view.findViewById(R.id.timer_message_tv);
            imageView = (ImageView) view.findViewById(R.id.image);
            progressBar = view.findViewById(R.id.progressBar);
            iconSent = view.findViewById(R.id.icon_sent);
            timeText = (TextView) view.findViewById(R.id.time_tv);
            textContainer = view.findViewById(R.id.text_container);
            textContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("merr", "onLongClick state "+messages.get(getAdapterPosition()).state);
                    if (messages.get(getAdapterPosition()).state == Message.STATE_SUCCESS)
                    {
                        listener.onCreateSuccessMessageDialog(MessageViewHolder.this);
                    }
                    else if (messages.get(getAdapterPosition()).state == Message.STATE_ERROR)
                    {
                        listener.onCreateErrorMessageDialog(MessageViewHolder.this);
                    }
                    return true;
                }
            });
        }

        @Override
        public void onDeleteMessage() {
            MyDbHelper.removeMessage(new MyDbHelper((Context) listener).getWritableDatabase(), messages.get(getAdapterPosition()).getmId());
            messages.remove(getAdapterPosition());
            if (timerTask != null && timerTask.isRunning)
            {
                timerTask.cancel();
            }
            notifyItemRemoved(getAdapterPosition());
        }

        @Override
        public void onReSendMessage() {
            chatAdapterListener.onReSendMessage(messages.get(getAdapterPosition()));
        }

        @Override
        public void onSetTime() {
            listener.onCreateTimeDialog(this);
        }

        @Override
        public void onApplyTime(long timer) {
            Message message = messages.get(getAdapterPosition());
            message.setTimerAdded(System.currentTimeMillis());
            message.setMessageTime(timer);
            startTimer();
            if (timer == 0)
            {
                MyDbHelper.updateMessageTimer(new MyDbHelper((Context) listener)
                        .getWritableDatabase(), message.getmId(), timer, Message.TYPE_TIMER_GLOBAL, (Context) listener);
            }
            else MyDbHelper.updateMessageTimer(new MyDbHelper((Context) listener)
                    .getWritableDatabase(), message.getmId(), timer, Message.TYPE_TIMER_INDIVIDUAL, (Context) listener);

        }

        public void startTimer()
        {
            Message message = messages.get(getAdapterPosition());
            if (timerTask != null && timerTask.isRunning)
            {
                timerTask.cancel();
            }
            if (message.getMessageTimer() > 0)
            {
                timerTask = new MyTimerTask(message.getTimerAdded(), message.getMessageTimer(), timerTextView);
                timerTask.isRunning = true;
                Timer myTimer = new Timer();
                myTimer.schedule(timerTask, 0, 1000);
            }
        }


    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String senderPhone = message.getSenderPhoneNumber();
        if (senderPhone.equals(myPhoneNumber))
        {
            return Message.MY_MESSAGE;
        }
        else return Message.NOT_MY_MESSAGE;
    }



    /*public static class DatePickerFragment extends DialogFragment
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
    }*/


    public interface OnChatAdapterListener
    {
        void onReSendMessage(Message message);
    }

}

