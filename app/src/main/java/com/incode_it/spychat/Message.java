package com.incode_it.spychat;


import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message
{
    public static final int STATE_ADDED = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_ERROR = 2;

    public static final int NOT_MY_MESSAGE = 0;
    public static final int MY_MESSAGE = 1;
    private String message;
    private String senderPhoneNumber;
    private String receiverPhoneNumber;
    private String date;
    public int state;
    private long myId;

    public Message(String message, String senderPhoneNumber, String receiverPhoneNumber)
    {
        this.message = message;
        this.senderPhoneNumber = senderPhoneNumber;
        this.receiverPhoneNumber = receiverPhoneNumber;
        date = getDateTime();
        state = STATE_ADDED;
        myId = System.currentTimeMillis();
    }

    public Message(String message, String senderPhoneNumber, String receiverPhoneNumber, String date, int state, long myId)
    {
        this.message = message;
        this.senderPhoneNumber = senderPhoneNumber;
        this.receiverPhoneNumber = receiverPhoneNumber;
        this.date = date;
        this.state = state;
        this.myId = myId;
    }

    public long getMyId() {
        return myId;
    }

    public int getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public String getReceiverPhoneNumber() {
        return receiverPhoneNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
