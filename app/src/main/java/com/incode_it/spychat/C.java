package com.incode_it.spychat;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.amazonaws.regions.Regions;

public class C
{
    public static final String play_market_url = "https://play.google.com/store/apps/details?id=com.incode_it.spychat";

    public static final String amazonBucket = "spy-chat";
    public static final String amazonIdentityPoolID = "us-east-1:3bc44367-78a8-47e8-b689-1f05f72f74e5";
    public static final Regions amazonRegion = Regions.US_EAST_1;

    public static final String SHARED_ACCESS_TOKEN = "access_token";
    public static final String SHARED_REFRESH_TOKEN = "refresh_token";
    public static final String SHARED_MY_PHONE_NUMBER = "my_phone_number";
    //public static final String SHARED_MY_EMAIL = "my_email";
    public static final String SHARED_PIN = "my_pin";
    public static final String SHARED_NEW_PHOTO_PATH = "photo_path";
    public static final String SHARED_NEW_VIDEO_PATH = "video_path";

    public static final String EXTRA_COUNTRY_CODE = "country_code";
    public static final String EXTRA_COUNTRY_ISO = "country_iso";
    public static final String EXTRA_OPPONENT_PHONE_NUMBER = "opponent_phone_number";
    public static final String EXTRA_MY_PHONE_NUMBER = "my_phone_number";
    public static final String EXTRA_REQUEST_PIN = "request_pin";
    public static final String EXTRA_IS_FROM_NOTIFICATION = "is_from_notification";
    public static final String EXTRA_MESSAGE_ID = "message_id";
    public static final String EXTRA_MEDIA_FILE_PATH = "media_file_path";
    public static final String EXTRA_MEDIA_TYPE = "media_type";
    public static final String EXTRA_MEDIA_LOCAL_PATH = "media_local_path";

    public static final String EXTRA_ID_TO_UPDATE_MEDIA = "id_to_update_media";
    public static final String EXTRA_MEDIA_STATE = "media_state";
    public static final String EXTRA_MEDIA_PROGRESS_TOTAL = "media_progress_total";
    public static final String EXTRA_MEDIA_PROGRESS_CURRENT = "media_progress_current";

    public static final String MEDIA_TYPE_IMAGE = "images";
    public static final String MEDIA_TYPE_VIDEO = "videos";
    public static final String MEDIA_TYPE_AUDIO = "audios";

    public static final String BASE_URL = "http://radiant-brushlands-47862.herokuapp.com/";

    public static final String REMOVAL_GLOBAL_TIME = "removal_global_time";
    public static final String GLOBAL_TIMER = "added_global_time";

    public static final String ID_TO_DELETE = "id_to_delete";

    public static final String SETTING_SOUND = "is_sound_on";
    public static final String SETTING_VIBRATE = "is_vibrate_on";
    public static final String SETTING_PIN = "is_pin_on";





    public static final int REQUEST_CODE_SECURITY_EXIT = 2;
    public static final int REQUEST_CODE_ACTIVITY_CONTACTS = 3;
    public static final int REQUEST_CODE_SELECT_COUNTRY = 4;
    public static final int REQUEST_CODE_ACTIVITY_CHAT = 5;
    public static final int REQUEST_CODE_ACTIVITY_SETTINGS = 6;
    public static final int REQUEST_CODE_ACTIVITY_CHANGE_PASSWORD = 7;
    public static final int REQUEST_CODE_ACTIVITY_ADD_EMAIL = 8;
    public static final int REQUEST_CODE_CHECK_EMAIL = 9;

    public static final int RESULT_EXIT = 2;
    public static final int RESULT_LOG_OUT = 3;

    public static final int READ_SMS_CODE = 0;
    public static final int READ_CONTACTS_CODE = 1;

    public static int getMyId()
    {
        return (int) System.currentTimeMillis();
    }

    public static Bitmap noPhotoBitmap;

    public static Bitmap getNoPhotoBitmap(Context context)
    {
        if (noPhotoBitmap == null)
        {
            Drawable drawable = context.getResources().getDrawable(R.drawable.profile);
            Canvas canvas = new Canvas();
            noPhotoBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(noPhotoBitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }

        return noPhotoBitmap;
    }

    public static Bitmap emptyImageBitmap;

    public static Bitmap getEmptyImageMessageBitmap(Context context)
    {
        if (emptyImageBitmap == null)
        {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_photo_size_select_actual_black_24dp);
            Canvas canvas = new Canvas();
            emptyImageBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(emptyImageBitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }

        return emptyImageBitmap;
    }

    public static Bitmap emptyVideoBitmap;

    public static Bitmap getEmptyVideoMessageBitmap(Context context)
    {
        if (emptyVideoBitmap == null)
        {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_video);
            Canvas canvas = new Canvas();
            emptyVideoBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(emptyVideoBitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }

        return emptyVideoBitmap;
    }




}
