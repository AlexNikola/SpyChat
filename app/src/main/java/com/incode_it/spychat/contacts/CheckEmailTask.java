package com.incode_it.spychat.contacts;

import android.content.Context;
import android.os.AsyncTask;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLEncoder;

class CheckEmailTask extends AsyncTask<String, Void, String>
{
    private WeakReference<Callback> weekCallback;
    private WeakReference<Context> weekContext;
    boolean isRunning = false;

    CheckEmailTask() {
    }

    void setCallback(Callback callback, Context context) {
        weekCallback = new WeakReference<>(callback);
        weekContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        isRunning = true;
    }

    @Override
    protected String doInBackground(String... params) {
        String phoneNumber = params[0];
        try {
            phoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
            String urlParameters =
                    "phone=" + phoneNumber;

            URL url = new URL(C.BASE_URL + "api/v1/usersJob/check-email/");

            return MyConnection.post(url, urlParameters, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        isRunning = false;
        Callback callback = weekCallback.get();
        Context context = weekContext.get();
        if (callback != null && context != null) {
            callback.onEmailChecked(result);
        }
    }

    @Override
    protected void onCancelled(String s) {
        isRunning = false;
    }

    interface Callback {
        void onEmailChecked(String result);
    }
}