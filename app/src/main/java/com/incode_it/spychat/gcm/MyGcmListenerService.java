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
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;
import com.incode_it.spychat.amazon.DownloadService;
import com.incode_it.spychat.contacts.ActivityMain;
import com.incode_it.spychat.data_base.MyDbHelper;
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
        String receivedTextMessage;
        int receivedColor;
        float receivetTextSize;
        boolean receivedIsAnimated;

        try {
            JSONObject jsonObject = new JSONObject(data.getString("message"));
            receivedTextMessage = jsonObject.getString("textmessage");
            receivedColor = jsonObject.getInt("color");
            receivetTextSize = (float) jsonObject.getDouble("size");
            receivedIsAnimated = jsonObject.getBoolean("animation");
            Log.d(TAG, "received is Animated: " + receivedIsAnimated);

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
            message = new Message(receivedTextMessage, phone, myPhoneNumber, Message.STATE_ADDED, Message.NOT_MY_MESSAGE_IMAGE);
        }
        else if (receivedTextMessage.startsWith(C.MEDIA_TYPE_VIDEO + "/+"))
        {
            message = new Message(receivedTextMessage, phone, myPhoneNumber, Message.STATE_ADDED, Message.NOT_MY_MESSAGE_VIDEO);
        }
        else if (receivedTextMessage.startsWith(C.MEDIA_TYPE_AUDIO + "/+"))
        {
            message = new Message(receivedTextMessage, phone, myPhoneNumber, Message.STATE_DOWNLOADING, Message.NOT_MY_MESSAGE_AUDIO);
        }
        else
        {
            message = new Message(receivedTextMessage, phone, myPhoneNumber, Message.STATE_SUCCESS, Message.NOT_MY_MESSAGE_TEXT);
            message.setColor(receivedColor);
            message.setTextSize(receivetTextSize);
            message.setAnimated(receivedIsAnimated);
        }

        sendNotification(message);

        MyDbHelper.insertMessage(new MyDbHelper(this).getWritableDatabase(), message);
        Intent intent = new Intent(QuickstartPreferences.RECEIVE_MESSAGE);
        intent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, phone);
        intent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        if (receivedTextMessage.startsWith(C.MEDIA_TYPE_AUDIO + "/+"))
        {
            String remotePath = message.getMessage();
            Intent serviceIntent = new Intent(this, DownloadService.class);
            serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, remotePath);
            serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_VIDEO);
            startService(serviceIntent);
            MyDbHelper.updateMessageState(new MyDbHelper(this).getWritableDatabase(), message.state, message.getMessageId());
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
}