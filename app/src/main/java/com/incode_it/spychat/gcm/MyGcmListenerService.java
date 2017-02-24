package com.incode_it.spychat.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;
import com.incode_it.spychat.amazon.DownloadService;
import com.incode_it.spychat.contacts.ActivityMain;
import com.incode_it.spychat.data_base.MyDbHelper;
import com.incode_it.spychat.effects.TextStyle;
import com.incode_it.spychat.utils.Cypher;

import org.json.JSONException;
import org.json.JSONObject;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String receivedTextMessage = null;
        int receivedColor = 0;
        float receivedTextSize = 0f;
        boolean receivedIsAnimated = false;
        String receivedFont = "";
        String receiverPhoneNumber = null;
        int animationType = TextStyle.ANIMATION_NONE;
        int effect = 0;
        if (data != null) {
            for (String key : data.keySet()) {
                Object value = data.get(key);
                Log.d("mytest", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }

        try {
            JSONObject jsonObject = new JSONObject(data.getString("message"));
            Log.d("mytest", "onMessageReceived: " + jsonObject.getString("type"));
            if (jsonObject.getString("type").equals("typeText")) {
                receivedTextMessage = jsonObject.getString("textmessage");
                receivedColor = jsonObject.getInt("color");
                receivedTextSize = (float) jsonObject.getDouble("size");
                receivedIsAnimated = jsonObject.getBoolean("animation");
                receivedFont = jsonObject.getString("font");
                receiverPhoneNumber = jsonObject.getString("receiverPhoneNumber");

                if (jsonObject.has("animationType")) {
                    animationType = jsonObject.getInt("animationType");
                } else {
                    if (receivedIsAnimated) {
                        animationType = TextStyle.ANIMATION_BLINK;
                    }
                }
                //Log.d("rfddffdfg", "onMessageReceived: " + receiverPhoneNumber);
            } else if (jsonObject.getString("type").equals("typeMedia")) {
                receiverPhoneNumber = jsonObject.getString("receiverPhoneNumber");
                receivedTextMessage = jsonObject.getString("url");
                //Log.d(TAG, "onMessageReceived: url: " + receivedTextMessage);
            } else if (jsonObject.getString("type").equals("typeNotification")) {
                Log.e("mytest", "onMessageReceived: ");
                resolveAdminNotification(jsonObject);
            }
            if (jsonObject.has("effect")) {
                effect = jsonObject.getInt("effect");
            }

        } catch (JSONException e) {

            e.printStackTrace();
            return;
        }

        if (receivedTextMessage == null) return;
        String phone = data.getString("phone");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        Message message;
        if (receivedTextMessage.startsWith(C.MEDIA_TYPE_IMAGE + "/+"))
        {
            Log.d("mytest", "MEDIA_TYPE_IMAGE");
            message = new Message(receivedTextMessage, phone, receiverPhoneNumber, Message.STATE_ADDED, Message.NOT_MY_MESSAGE_IMAGE, receiverPhoneNumber);
        }
        else if (receivedTextMessage.startsWith(C.MEDIA_TYPE_VIDEO + "/+"))
        {
            Log.d("mytest", "MEDIA_TYPE_VIDEO");
            message = new Message(receivedTextMessage, phone, receiverPhoneNumber, Message.STATE_ADDED, Message.NOT_MY_MESSAGE_VIDEO, receiverPhoneNumber);
        }
        else if (receivedTextMessage.startsWith(C.MEDIA_TYPE_AUDIO + "/+"))
        {
            Log.d("mytest", "MEDIA_TYPE_AUDIO");
            message = new Message(receivedTextMessage, phone, receiverPhoneNumber, Message.STATE_DOWNLOADING, Message.NOT_MY_MESSAGE_AUDIO, receiverPhoneNumber);
        }
        else
        {
            Log.d("mytest", "MEDIA_TYPE_TEXT");
            message = new Message(receivedTextMessage, phone, receiverPhoneNumber, Message.STATE_SUCCESS, Message.NOT_MY_MESSAGE_TEXT, receiverPhoneNumber);
            message.setColor(receivedColor);
            message.setTextSize(receivedTextSize);
            message.setAnimationType(animationType);
            if (receivedFont.equals("default")) {
                message.setFont(null);
            } else {
                message.setFont(receivedFont);
            }
        }

        message.setEffect(effect);

        MyDbHelper.insertMessage(new MyDbHelper(this).getWritableDatabase(), message, MyGcmListenerService.this);

        if (receivedTextMessage.startsWith(C.MEDIA_TYPE_AUDIO + "/+"))
        {
            String remotePath = message.getMessage();
            Intent serviceIntent = new Intent(this, DownloadService.class);
            serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, remotePath);
            serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_VIDEO);
            startService(serviceIntent);
            MyDbHelper.updateMessageState(new MyDbHelper(this).getWritableDatabase(), message.state, message.getMessageId(), MyGcmListenerService.this);
        }

        if (myPhoneNumber.equals(receiverPhoneNumber)) {
            sendNotification(message);

            Intent intent = new Intent(QuickstartPreferences.RECEIVE_MESSAGE);
            intent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, phone);
            intent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        }

        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(Message message) {

        Intent resultIntent = new Intent(this, ActivityMain.class);
        resultIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, message.getSenderPhoneNumber());
        resultIntent.putExtra(C.EXTRA_IS_FROM_NOTIFICATION, true);
        resultIntent.putExtra(C.EXTRA_REQUEST_PIN, false);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ActivityMain.class);
        stackBuilder.addNextIntent(resultIntent);
        /*Intent intentAuth = stackBuilder.editIntentAt(0);
        intentAuth.putExtra(C.EXTRA_REQUEST_PIN, false);*/

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chat_24dp)
                .setContentTitle("Spy Message")
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        if (message.messageType == Message.NOT_MY_MESSAGE_TEXT)
        {
            notificationBuilder.setContentText(Cypher.decrypt(message.getMessage()));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSoundOn = sharedPreferences.getBoolean(C.SETTING_SOUND, true);
        boolean isVibrateOn = sharedPreferences.getBoolean(C.SETTING_VIBRATE, true);
        if (isSoundOn) notificationBuilder.setSound(defaultSoundUri);
        if (isVibrateOn) notificationBuilder.setVibrate(new long[] {1000, 1000} );


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        long longId = Long.parseLong(message.getSenderPhoneNumber().substring(1));
        int id = (int) longId;
        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
    }

    private void resolveAdminNotification(JSONObject jsonObject) throws JSONException {
        String title = jsonObject.getString("title");
        String text = jsonObject.getString("text");

        Log.d("mytest", "resolveAdminNotification: " + title + " " + text);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(text);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSoundOn = sharedPreferences.getBoolean(C.SETTING_SOUND, true);
        boolean isVibrateOn = sharedPreferences.getBoolean(C.SETTING_VIBRATE, true);
        if (isSoundOn) notificationBuilder.setSound(defaultSoundUri);
        if (isVibrateOn) notificationBuilder.setVibrate(new long[] {1000, 1000} );

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(500, notificationBuilder.build());
    }
}