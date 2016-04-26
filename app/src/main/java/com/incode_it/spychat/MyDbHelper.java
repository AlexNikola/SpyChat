package com.incode_it.spychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.incode_it.spychat.MReaderContract.*;

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
                    Chat.MY_ID + TYPE_INT +" )";

    private static final String SQL_DELETE_CHAT_TABLE =
            "DROP TABLE IF EXISTS " + Chat.TABLE_NAME;

    public static final int DATABASE_VERSION = 21;
    public static final String DATABASE_NAME = "Chat.db";

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHAT_TABLE);
        db.execSQL(SQL_CREATE_CONTACT_TABLE);

        /*ArrayList<String> c = new ArrayList<>();
        c.add("+380664431954");
        c.add("+380665713467");
        c.add("+380669713043");
        c.add("+380991514768");
        c.add("+380669997588");

        insertRegisteredContacts(db, c);*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_CHAT_TABLE);
        db.execSQL(SQL_DELETE_CONTACT_TABLE);
        onCreate(db);
    }

    public static synchronized void insertMessage(SQLiteDatabase db, Message message)
    {
        ContentValues values = new ContentValues();
        values.put(Chat.MESSAGE, message.getMessage());
        values.put(Chat.SENDER_PHONE_NUMBER, message.getSenderPhoneNumber());
        values.put(Chat.RECEIVER_PHONE_NUMBER, message.getReceiverPhoneNumber());
        values.put(Chat.DATE, message.getDate());
        values.put(Chat.STATE, message.getState());
        values.put(Chat.MY_ID, message.getMyId());

        long newRowId;
        newRowId = db.insert(Chat.TABLE_NAME, null, values);

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
                Message message = new Message(textMessage, senderPhoneNumber, receiverPhoneNumber, date, state, myId);
                messagesArr.add(message);
                Log.d(LOG_TAG, "id " + cursor.getString(0));
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
        db.close();
    }

    public static synchronized void insertMessageState(SQLiteDatabase db, Message message)
    {
        ContentValues values = new ContentValues();

        String whereClause = Chat.MY_ID + "=" + message.getMyId();

        values.put(Chat.STATE, message.state);
        int num = db.update(Chat.TABLE_NAME, values, whereClause, null);
        db.close();

        Log.d(LOG_TAG, "insertMessageState numbers: "+num);
    }

















    private void putTestData(SQLiteDatabase db)
    {
        for (Message message: messageArrayList)
        {
            ContentValues values = new ContentValues();
            values.put(Chat.MESSAGE, message.getMessage());
            values.put(Chat.SENDER_PHONE_NUMBER, message.getSenderPhoneNumber());
            values.put(Chat.RECEIVER_PHONE_NUMBER, message.getReceiverPhoneNumber());
            values.put(Chat.DATE, message.getDate());

            long newRowId;
            newRowId = db.insert(Chat.TABLE_NAME, null, values);
        }

    }

    private void initTestData()
    {
        messageArrayList = new ArrayList<>();
        messageArrayList.add(new Message("Андрей Message 1", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 2", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 3 Message 3 Message 3 Message 3 Message 3 Message 3 ", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 4 Message 4 Message 4", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 5", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 7", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 8 Message 8", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 9 Message 9 Message 9 Message 9 Message 9 Message 9", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 10 Message 10 Message 10", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 11", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 12", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 13", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 14", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 15", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 16 Message 16", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 17", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 18 Message 18", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 19", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 20", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 21 Message 21 Message 21 Message 21", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 22 Message 22 Message 22", ActivityMain.myPhoneNumber, "+380639461005"));
        messageArrayList.add(new Message("Андрей Message 23", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 24 Message 24 Message 24 Message 24 Message 24 Message 24", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 25", "+380639461005", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Андрей Message 26 Message 26 Message 26 Message 26 Message 26 Message 26 Message 26 Message 26", ActivityMain.myPhoneNumber, "+380639461005"));

        messageArrayList.add(new Message("Настя Message 1", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 2", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 3 Message 3 Message 3 Message 3 Message 3 Message 3 ", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 4 Message 4 Message 4", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 5", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 7", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 8 Message 8", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 9 Message 9 Message 9 Message 9 Message 9 Message 9", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 10 Message 10 Message 10", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 11", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 12", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 13", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 14", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 15", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 16 Message 16", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 17", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 18 Message 18", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 19", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 20", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 21 Message 21 Message 21 Message 21", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 22 Message 22 Message 22", ActivityMain.myPhoneNumber, "+380665713467"));
        messageArrayList.add(new Message("Настя Message 23", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 24 Message 24 Message 24 Message 24 Message 24 Message 24", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 25", "+380665713467", ActivityMain.myPhoneNumber));
        messageArrayList.add(new Message("Настя Message 26 Message 26 Message 26 Message 26 Message 26 Message 26 Message 26 Message 26", ActivityMain.myPhoneNumber, "+380665713467"));
    }
}
