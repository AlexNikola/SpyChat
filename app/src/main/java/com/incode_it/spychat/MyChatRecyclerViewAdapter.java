package com.incode_it.spychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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
        messageHolder.text.setText(messages.get(position).getMessage());
        messageHolder.timeText.setText(messages.get(position).getDate());

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

        public MessageViewHolder(View view) {
            super(view);
            mView = view;
            text = (TextView) view.findViewById(R.id.text_message);
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
        public void onApplyTime(int hour, int minute) {

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

