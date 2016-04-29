package com.incode_it.spychat;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.incode_it.spychat.MReaderContract.*;
import com.incode_it.spychat.alarm.TimeHolder;

import java.util.ArrayList;

public class MyDbHelper extends SQLiteOpenHelper
{
    public static final String LOG_TAG = "curs";
    ArrayList<Message> messageArrayList;

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INT = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CONTACT_TABLE =
            "CREATE TABLE " + RegisteredContact.TABLE_NAME + " (" +
                    RegisteredContact._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RegisteredContact.PHONE_NUMBER + TYPE_TEXT+ " )";

    private static final String SQL_DELETE_CONTACT_TABLE =
            "DROP TABLE IF EXISTS " + RegisteredContact.TABLE_NAME;

    private static final String SQL_CREATE_CHAT_TABLE =
            "CREATE TABLE " + Chat.TABLE_NAME + " (" +
                    Chat._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Chat.MESSAGE + TYPE_TEXT + COMMA_SEP +
                    Chat.SENDER_PHONE_NUMBER + TYPE_TEXT + COMMA_SEP +
                    Chat.RECEIVER_PHONE_NUMBER + TYPE_TEXT + COMMA_SEP +
                    Chat.DATE + TYPE_TEXT + COMMA_SEP +
                    Chat.STATE + TYPE_INT + COMMA_SEP +
                    Chat.MY_ID + TYPE_INT + COMMA_SEP +
                    Chat.TIMER_TYPE + TYPE_INT + COMMA_SEP +
                    Chat.TIMER_ADDED + TYPE_INT + COMMA_SEP +
                    Chat.MESSAGE_LIFE_TIME + TYPE_INT +" )";

    private static final String SQL_DELETE_CHAT_TABLE =
            "DROP TABLE IF EXISTS " + Chat.TABLE_NAME;

    public static final int DATABASE_VERSION = 11;
    public static final String DATABASE_NAME = "SpyChat.db";

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHAT_TABLE);
        db.execSQL(SQL_CREATE_CONTACT_TABLE);

        ArrayList<String> c = new ArrayList<>();
        c.add("+380664431954");
        c.add("+380665713467");
        c.add("+380669713043");
        c.add("+380991514768");
        c.add("+380669997588");

        insertRegisteredContacts(db, c);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_CHAT_TABLE);
        db.execSQL(SQL_DELETE_CONTACT_TABLE);
        onCreate(db);
    }

    public static synchronized void insertMessage(SQLiteDatabase db, Message message, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long globalMessageLifeTime = sharedPreferences.getLong(C.GLOBAL_TIMER, 0);

        ContentValues values = new ContentValues();
        values.put(Chat.MESSAGE, message.getMessage());
        values.put(Chat.SENDER_PHONE_NUMBER, message.getSenderPhoneNumber());
        values.put(Chat.RECEIVER_PHONE_NUMBER, message.getReceiverPhoneNumber());
        values.put(Chat.DATE, message.getDate());
        values.put(Chat.STATE, message.getState());
        values.put(Chat.MY_ID, message.getmId());

        values.put(Chat.TIMER_ADDED, System.currentTimeMillis());
        values.put(Chat.TIMER_TYPE, Message.TYPE_TIMER_GLOBAL);
        values.put(Chat.MESSAGE_LIFE_TIME, globalMessageLifeTime);

        long newRowId;
        newRowId = db.insert(Chat.TABLE_NAME, null, values);

        db.close();
    }

    public static synchronized ArrayList<TimeHolder> readTime(SQLiteDatabase db)
    {
        ArrayList<TimeHolder> arrayList = new ArrayList<>();

        String sql = "SELECT " +
                Chat.MY_ID + COMMA_SEP +
                Chat.TIMER_ADDED + COMMA_SEP +
                Chat.MESSAGE_LIFE_TIME + COMMA_SEP +
                Chat.TIMER_TYPE +
                " FROM Chat";

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst())
        {
            do
            {
                TimeHolder timeHolder = new TimeHolder();

                timeHolder.mId = cursor.getLong(0);
                timeHolder.timerAdded = cursor.getLong(1);
                timeHolder.messageLifeTime = cursor.getLong(2);
                timeHolder.timerType = cursor.getInt(3);

                arrayList.add(timeHolder);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return arrayList;
    }

    public static synchronized void removeMessage(SQLiteDatabase db, long mId)
    {
        String whereClause = Chat.MY_ID + "=" + mId;
        db.delete(Chat.TABLE_NAME, whereClause, null);
        db.close();
    }

    public static synchronized ArrayList<Message> readContactMessages(SQLiteDatabase db, MyContacts.Contact contact)
    {
        String[] columnNames = { Chat.MESSAGE, Chat.SENDER_PHONE_NUMBER,
                Chat.RECEIVER_PHONE_NUMBER, Chat.DATE };
        String whereClause = contact.phoneNumber + " = ? OR " + contact.phoneNumber + " = ?";
        String[] selectionArgs = {Chat.SENDER_PHONE_NUMBER, Chat.RECEIVER_PHONE_NUMBER};
        String groupBy = null;
        String having = null;
        String orderBy = "datetime("+Chat.DATE+")" + " ASC";

        /*Cursor cursor = db.query(Chat.TABLE_NAME, columnNames, whereClause, selectionArgs,
                groupBy, having, orderBy);*/

        String sql = "SELECT * FROM Chat WHERE " + Chat.SENDER_PHONE_NUMBER + " LIKE '%"+contact.phoneNumber+"%' OR " + Chat.RECEIVER_PHONE_NUMBER + " LIKE '%"+contact.phoneNumber+"%' ORDER BY " +
                orderBy;
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<Message> messagesArr = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do
            {
                String textMessage = cursor.getString(1);
                String senderPhoneNumber = cursor.getString(2);
                String receiverPhoneNumber = cursor.getString(3);
                String date = cursor.getString(4);
                int state = cursor.getInt(5);
                long myId = cursor.getLong(6);
                long timerAdded = cursor.getLong(8);
                long messageTime = cursor.getLong(9);
                Message message = new Message(textMessage, senderPhoneNumber, receiverPhoneNumber, date, state, myId);
                message.setTimerAdded(timerAdded);
                message.setMessageTime(messageTime);
                messagesArr.add(message);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messagesArr;
    }

    public static synchronized ArrayList<String> readRegisteredContacts(SQLiteDatabase db)
    {
        Log.d(LOG_TAG, "readRegisteredContacts");
        String sql = "SELECT * FROM "+ RegisteredContact.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<String> contactsArr = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do
            {
                String phoneNumber = cursor.getString(1);
                contactsArr.add(phoneNumber);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactsArr;
    }

    public static synchronized void insertRegisteredContacts(SQLiteDatabase db, ArrayList<String> registeredContacts)
    {
        db.execSQL(SQL_DELETE_CONTACT_TABLE);
        db.execSQL(SQL_CREATE_CONTACT_TABLE);
        Log.d(LOG_TAG, "insertRegisteredContacts " + registeredContacts.size());
        ContentValues values = new ContentValues();

        for (String phone: registeredContacts)
        {
            values.put(RegisteredContact.PHONE_NUMBER, phone);
            long newRowId;
            newRowId = db.insert(RegisteredContact.TABLE_NAME, null, values);
            values.clear();
        }
        //db.close();
    }

    public static synchronized void insertMessageState(SQLiteDatabase db, Message message)
    {
        ContentValues values = new ContentValues();

        String whereClause = Chat.MY_ID + "=" + message.getmId();

        values.put(Chat.STATE, message.state);
        int num = db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();

        Log.d(LOG_TAG, "insertMessageState numbers: "+num);
    }

    public static synchronized void updateMessageTimer(SQLiteDatabase db, long mId, long timer, int type, Context context)
    {
        ContentValues values = new ContentValues();
        String whereClause = Chat.MY_ID + "=" + mId;

        if (type == Message.TYPE_TIMER_INDIVIDUAL)
        {
            values.put(Chat.TIMER_ADDED, System.currentTimeMillis());
            values.put(Chat.TIMER_TYPE, Message.TYPE_TIMER_INDIVIDUAL);
            values.put(Chat.MESSAGE_LIFE_TIME, timer);
        }
        else if (type == Message.TYPE_TIMER_GLOBAL)
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            long globalMessageLifeTime = sharedPreferences.getLong(C.GLOBAL_TIMER, 0);
            long globalTimerAdded = sharedPreferences.getLong(C.GLOBAL_TIMER_ADDED, 0);

            values.put(Chat.TIMER_ADDED, globalTimerAdded);
            values.put(Chat.TIMER_TYPE, Message.TYPE_TIMER_INDIVIDUAL);
            values.put(Chat.MESSAGE_LIFE_TIME, globalMessageLifeTime);
        }


        db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();
    }
}


