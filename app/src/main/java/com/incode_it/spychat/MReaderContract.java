package com.incode_it.spychat;

import android.provider.BaseColumns;

public final class MReaderContract
{
    public MReaderContract() {
    }

    /*public static abstract class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String CHAT_ID = "chat_id";
    }*/

    public static abstract class Chat implements BaseColumns {
        public static final String TABLE_NAME = "chat";
        public static final String MESSAGE = "message";
        public static final String SENDER_PHONE_NUMBER = "sender_phone_number";
        public static final String RECEIVER_PHONE_NUMBER = "receiver_phone_number";
        public static final String DATE = "mdate";
        public static final String STATE = "mstate";
        public static final String MY_ID = "mid";
    }

    public static abstract class RegisteredContact implements BaseColumns {
        public static final String TABLE_NAME = "registeredContacts";
        public static final String PHONE_NUMBER = "phone_number";
    }

}
