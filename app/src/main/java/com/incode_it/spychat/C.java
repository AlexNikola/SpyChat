package com.incode_it.spychat;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class C
{
    public static final String SHARED_ACCESS_TOKEN = "access_token";
    public static final String SHARED_REFRESH_TOKEN = "refresh_token";
    public static final String SHARED_MY_PHONE_NUMBER = "my_phone_number";

    public static final String EXTRA_COUNTRY_CODE = "country_code";
    public static final String EXTRA_COUNTRY_ISO = "country_iso";
    public static final String EXTRA_OPPONENT_PHONE_NUMBER = "phone_number";

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
    public static final int REQUEST_CODE_ACTIVITY_CHAT = 5;

    public static final int RESULT_EXIT = 2;
    public static final int RESULT_LOG_OUT = 3;

    public static final int READ_SMS_CODE = 0;
    public static final int READ_CONTACTS_CODE = 1;

    public static int getMyId()
    {
        return (int) System.currentTimeMillis();
    }

    public static Bitmap getNoPhotoBitmap(Context context)
    {
        Drawable drawable = context.getResources().getDrawable(R.drawable.profile);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
