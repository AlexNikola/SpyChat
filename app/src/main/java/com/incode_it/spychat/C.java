package com.incode_it.spychat;


public class C
{
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String PHONE_NUMBER = "phone_number";
    public static final String MESSAGE = "massage";

    public static final String BASE_URL = "http://radiant-brushlands-47862.herokuapp.com/";

    public static final String REMOVAL_GLOBAL_TIME = "removal_global_time";

    public static final String ID_TO_DELETE = "id_to_delete";

    public static final String SETTING_SOUND = "is_sound_on";
    public static final String SETTING_VIBRATE = "is_vibrate_on";
    public static final String SETTING_PIN = "is_pin_on";

    public static final String PIN = "my_pin";

    public static final String REQUEST_PIN = "request_pin";

    public static final int SECURITY_EXIT = 1;

    public static final int READ_SMS_CODE = 0;
    public static final int READ_CONTACTS_CODE = 1;

    public static int getMyId()
    {
        return (int) System.currentTimeMillis();
    }
}
