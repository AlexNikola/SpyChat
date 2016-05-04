package com.incode_it.spychat.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.incode_it.spychat.chat.ActivityChat;
import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
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
        String message = data.getString("message");
        String phone = data.getString("phone");
        Log.d(TAG, "phone: " + phone);
        Log.d(TAG, "message: " + message);

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = tm.getLine1Number();

        MyDbHelper.insertMessage(new MyDbHelper(this).getWritableDatabase(), new Message(message, phone, myPhoneNumber, Message.STATE_SUCCESS));
        Intent intent = new Intent(QuickstartPreferences.RECEIVE_MESSAGE);
        intent.putExtra(C.PHONE_NUMBER, phone);
        intent.putExtra(C.MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message, phone);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String phone) {
        Intent intent = new Intent(this, ActivityChat.class);
        intent.putExtra(C.PHONE_NUMBER, phone);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chat_24dp)
                .setContentTitle("Spy Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[] {1000, 1000} )
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}