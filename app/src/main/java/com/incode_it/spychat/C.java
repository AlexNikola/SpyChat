package com.incode_it.spychat;


public class C
{
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String PHONE_NUMBER = "phone_number";
    public static final String MESSAGE = "massage";

    public static final String BASE_URL = "http://192.168.0.102:7777/";

    public static final String REMOVAL_GLOBAL_TIME = "removal_global_time";

    public static final String ID_TO_DELETE = "id_to_delete";

    public static int getMyId()
    {
        return (int) System.currentTimeMillis();
    }
}
