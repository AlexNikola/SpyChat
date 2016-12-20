package com.incode_it.spychat;


import android.database.Cursor;
import android.graphics.Color;

import com.incode_it.spychat.data_base.MReaderContract;

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
    private int color = Color.BLACK;
    private float textSize;
    private String font;
    public String ownerPhoneNumber;
    private int effect;
    private int animationType;

    public Message(String message, String senderPhoneNumber, String receiverPhoneNumber, int state, int messageType, String ownerPhoneNumber)
    {
        this.message = message;
        this.senderPhoneNumber = senderPhoneNumber;
        this.receiverPhoneNumber = receiverPhoneNumber;
        date = getDateTime();
        this.state = state;
        messageId = C.getMyId();
        removalTime = 0;
        this.messageType = messageType;
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public Message(Cursor cursor)
    {
        this.message = cursor.getString(cursor.getColumnIndex(MReaderContract.Chat.MESSAGE));
        this.senderPhoneNumber = cursor.getString(cursor.getColumnIndex(MReaderContract.Chat.SENDER_PHONE_NUMBER));
        this.receiverPhoneNumber = cursor.getString(cursor.getColumnIndex(MReaderContract.Chat.RECEIVER_PHONE_NUMBER));
        this.date = cursor.getString(cursor.getColumnIndex(MReaderContract.Chat.DATE));
        this.state = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.STATE));
        this.messageId = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.MESSAGE_ID));
        this.removalTime = cursor.getLong(cursor.getColumnIndex(MReaderContract.Chat.REMOVAL_TIME));
        this.messageType = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.MESSAGE_TYPE));
        this.ownerPhoneNumber = cursor.getString(cursor.getColumnIndex(MReaderContract.Chat.OWNER));
        this.isViewed = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.IS_VIEWED));
        this.audioDuration = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.AUDIO_DURATION));
        this.color = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.COLOR));
        this.textSize = cursor.getFloat(cursor.getColumnIndex(MReaderContract.Chat.SIZE));
        this.font = cursor.getString(cursor.getColumnIndex(MReaderContract.Chat.FONT));
        this.effect = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.EFFECT));
        this.animationType = cursor.getInt(cursor.getColumnIndex(MReaderContract.Chat.ANIMATION_TYPE));
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getAnimationType() {
        return animationType;
    }

    public void setAnimationType(int animationType) {
        this.animationType = animationType;
    }
}
