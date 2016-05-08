package com.incode_it.spychat;


import com.incode_it.spychat.country_selection.Country;

import java.util.ArrayList;

public class C
{
    public static final String SHARED_ACCESS_TOKEN = "access_token";
    public static final String SHARED_REFRESH_TOKEN = "refresh_token";
    public static final String SHARED_MY_PHONE_NUMBER = "my_phone_number";

    public static final String EXTRA_COUNTRY_CODE = "country_code";
    public static final String EXTRA_COUNTRY_ISO = "country_iso";

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

    public static final int REQUEST_CODE_SECURITY_EXIT = 2;
    public static final int REQUEST_CODE_ACTIVITY_CONTACTS = 3;
    public static final int REQUEST_CODE_SELECT_COUNTRY = 4;

    public static final int READ_SMS_CODE = 0;
    public static final int READ_CONTACTS_CODE = 1;

    public static int getMyId()
    {
        return (int) System.currentTimeMillis();
    }

    /*public static ArrayList<Country> getCountryList()
    {
        ArrayList<Country> arr = new ArrayList<>();
        arr.add(new Country("India", "India", "+91", "IN"));
        arr.add(new Country("Brasil", "Brazil", "+55", "BR"));
        arr.add(new Country("España", "Spain", "+34", "ES"));
        arr.add(new Country("Deutschland", "Germany", "+49", "DE"));
        arr.add(new Country("Mexico", "Mexico", "+52", "MX"));
        arr.add(new Country("Italia", "Italy", "+39", "IT"));
        arr.add(new Country("Aland", "Aland Islands", "+358", "AX"));
        arr.add(new Country("American Samoa", "American Samoa", "+1", "AS"));
        arr.add(new Country("Andorra", "Andorra", "+376", "AD"));
        arr.add(new Country("Angola", "Angola", "+244", "AO"));
        arr.add(new Country("Anguilla", "Anguilla", "+1", "AI"));
        arr.add(new Country("Antarctica", "Antarctica", "+672", "AQ"));
        arr.add(new Country("Antigua and Barbuda", "Antigua and Barbuda", "+1", "AG"));
        arr.add(new Country("Argentina", "Argentina", "+54", "AR"));
        arr.add(new Country("Aruba", "Aruba", "+297", "AW"));
        arr.add(new Country("Ascension Island", "Ascension Island", "+247", "SH-AC"));
        arr.add(new Country("Australia", "Australia", "+61", "AU"));
        arr.add(new Country("Україна", "Ukraine", "+380", "UA"));

        return arr;
    }*/
}
