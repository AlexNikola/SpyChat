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
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.contacts.ActivityMain;
import com.incode_it.spychat.data_base.MyDbHelper;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;

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
        String textMessage = data.getString("message");
        String phone = data.getString("phone");
        Log.d(TAG, "phone: " + phone);
        Log.d(TAG, "message: " + textMessage);

        // [START_EXCLUDE]
        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(textMessage, phone);
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = tm.getLine1Number();

        Message message = new Message(textMessage, phone, myPhoneNumber, Message.STATE_UNREAD, Message.NOT_MY_MESSAGE_TEXT);
        MyDbHelper.insertMessage(new MyDbHelper(this).getWritableDatabase(), message);
        Intent intent = new Intent(QuickstartPreferences.RECEIVE_MESSAGE);
        intent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, phone);
        intent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String phone) {

        Intent resultIntent = new Intent(this, ActivityMain.class);
        resultIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, phone);
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
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSoundOn = sharedPreferences.getBoolean(C.SETTING_SOUND, true);
        boolean isVibrateOn = sharedPreferences.getBoolean(C.SETTING_VIBRATE, true);
        if (isSoundOn) notificationBuilder.setSound(defaultSoundUri);
        if (isVibrateOn) notificationBuilder.setVibrate(new long[] {1000, 1000} );


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        long longId = Long.parseLong(phone.substring(1));
        Log.d(TAG, "longId: " + longId);
        int id = (int) longId;
        Log.d(TAG, "id: " + id);
        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
    }
}