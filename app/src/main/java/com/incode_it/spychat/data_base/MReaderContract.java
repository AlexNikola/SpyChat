package com.incode_it.spychat.data_base;

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
        public static final String DATE = "date";
        public static final String STATE = "state";
        public static final String MY_ID = "mId";
        public static final String REMOVAL_TIME = "removal_time";
    }

    public static abstract class RegisteredContact implements BaseColumns {
        public static final String TABLE_NAME = "registered_contacts";
        public static final String PHONE_NUMBER = "phone_number";
    }

    public static abstract class Countries implements BaseColumns {
        public static final String TABLE_NAME = "countries";
        public static final String CODE_ISO = "code";
        public static final String NAME = "name";
        public static final String NATIVE = "native";
        public static final String CODE_PHONE = "phone";
    }

}
