package com.incode_it.spychat;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message
{
    public static final int TYPE_TIMER_GLOBAL = 0;
    public static final int TYPE_TIMER_INDIVIDUAL = 1;

    public static final int STATE_ADDED = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_UNREAD = 3;
    public static final int STATE_DOWNLOADING = 4;
    public static final int STATE_PLAYING = 5;

    public static final int NOT_MY_MESSAGE_TEXT = 0;
    public static final int MY_MESSAGE_TEXT = 1;
    public static final int NOT_MY_MESSAGE_IMAGE = 2;
    public static final int MY_MESSAGE_IMAGE = 3;
    public static final int NOT_MY_MESSAGE_VIDEO = 4;
    public static final int MY_MESSAGE_VIDEO = 5;
    public static final int NOT_MY_MESSAGE_AUDIO = 6;
    public static final int MY_MESSAGE_AUDIO = 7;
    private String message;
    private String senderPhoneNumber;
    private String receiverPhoneNumber;
    private String date;
    public int state;
    private int messageId;
    private long removalTime;
    public int messageType;
    public int isViewed;
    public int audioDuration;

    public Message(String message, String senderPhoneNumber, String receiverPhoneNumber, int state, int messageType)
    {
        this.message = message;
        this.senderPhoneNumber = senderPhoneNumber;
        this.receiverPhoneNumber = receiverPhoneNumber;
        date = getDateTime();
        this.state = state;
        messageId = C.getMyId();
        removalTime = 0;
        this.messageType = messageType;
    }

    public Message(String message, String senderPhoneNumber, String receiverPhoneNumber, String date, int state, int messageId, long removalTime, int messageType)
    {
        this.message = message;
        this.senderPhoneNumber = senderPhoneNumber;
        this.receiverPhoneNumber = receiverPhoneNumber;
        this.date = date;
        this.state = state;
        this.messageId = messageId;
        this.removalTime = removalTime;
        this.messageType = messageType;
    }

    public void setRemovalTime(long removalTime) {
        this.removalTime = removalTime;
    }

    public long getRemovalTime() {
        return removalTime;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
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
