package com.incode_it.spychat.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "myhttp";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]

            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]

            //Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(String regToken) {
        try {
            sendRegToken(regToken);
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendRegToken(String regToken) throws IOException, JSONException
    {
        //Log.i(TAG, "sendRegistrationToServer: " + regToken);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
        String urlParameters = "regToken=" + regToken;
        String header = "Bearer "+accessToken;

        URL url = new URL(C.BASE_URL + "api/v1/usersJob/regTokenChange/");

        String response = MyConnection.post(url, urlParameters, header);


        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(this))
            sendRegToken(regToken);
        }
    }




}