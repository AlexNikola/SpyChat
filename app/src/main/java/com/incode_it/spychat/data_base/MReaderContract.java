package com.incode_it.spychat.data_base;

import android.provider.BaseColumns;

public final class MReaderContract
{
    public MReaderContract() {
    }

    public static abstract class Chat implements BaseColumns {
        public static final String TABLE_NAME = "chat";
        public static final String MESSAGE = "message";
        public static final String SENDER_PHONE_NUMBER = "sender_phone_number";
        public static final String RECEIVER_PHONE_NUMBER = "receiver_phone_number";
        public static final String DATE = "date";
        public static final String STATE = "state";
        public static final String MESSAGE_ID = "message_id";
        public static final String REMOVAL_TIME = "removal_time";
        public static final String MESSAGE_TYPE = "message_time";
        public static final String IS_VIEWED = "is_viewed";
        public static final String AUDIO_DURATION = "audio_duration";
        public static final String COLOR = "color";
        public static final String SIZE = "size";
        public static final String ANIMATION = "animation";
        public static final String FONT = "font";
    }

    public static abstract class RegisteredContact implements BaseColumns {
        public static final String TABLE_NAME = "registered_contacts";
        public static final String PHONE_NUMBER = "phone_number";
    }

    public static abstract class Countries implements BaseColumns {
        public static final String TABLE_NAME = "countries";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String NATIVE = "native";
        public static final String PHONE = "phone";
        public static final String CONTINENT = "continent";
        public static final String CAPITAL = "capital";
        public static final String CURRENCY = "currency";
        public static final String LANGUAGES = "languages";

    }

}
