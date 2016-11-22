package com.incode_it.spychat.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.incode_it.spychat.C;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.data_base.MyDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

class UpdateContactsTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>>
{
    private WeakReference<Callback> weekCallback;
    private WeakReference<Context> weekContext;
    boolean isRunning = false;

    UpdateContactsTask() {
    }

    void setCallback(Callback callback, Context context)
    {
        weekCallback = new WeakReference<>(callback);
        weekContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        isRunning = true;
    }

    @Override
    protected ArrayList<String> doInBackground(ArrayList<String>... params) {
        ArrayList<String> contactsNumbers = params[0];
        ArrayList<String> registeredContacts = new ArrayList<>();
        try
        {
            JSONArray jsonArray = tryUpdateContacts(contactsNumbers);
            if (jsonArray != null)
            {
                registeredContacts = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject contact = (JSONObject) jsonArray.get(i);
                    String phoneNumber = contact.getString("phone");
                    boolean isRegistered = contact.getBoolean("isRegistered");

                    if (isRegistered)
                    {
                        registeredContacts.add(phoneNumber);
                    }
                }
                MyDbHelper.insertRegisteredContacts(new MyDbHelper(weekContext.get()).getWritableDatabase(), registeredContacts);
            }
        }
        catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }

        return registeredContacts;
    }

    @Override
    protected void onPostExecute(ArrayList<String> regContacts) {
        isRunning = false;
        Callback callback = weekCallback.get();
        Context context = weekContext.get();
        if (callback != null && context != null) {
            callback.onContactsDownloaded(regContacts);
        }
    }

    @Override
    protected void onCancelled(ArrayList<String> strings) {
        isRunning = false;
    }








    private JSONArray tryUpdateContacts(ArrayList<String> contactsNumbers) throws IOException, JSONException {
        StringBuilder sbParams = new StringBuilder();
        for (String number: contactsNumbers)
        {
            sbParams.append("contacts=").append(URLEncoder.encode(number, "UTF-8")).append("&");
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(weekContext.get());
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
        URL url = new URL(C.BASE_URL + "api/v1/usersJob/inSystem/");
        String header = "Bearer "+accessToken;

        String response = MyConnection.post(url, sbParams.toString(), header);

        JSONArray jsonArray = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(weekContext.get()))
                jsonArray = tryUpdateContacts(contactsNumbers);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            String res = jsonResponse.getString("result");
            if (res.equals("success"))
                jsonArray = jsonResponse.getJSONArray("contacts");
        }

        return jsonArray;
    }


    interface Callback
    {
        void onContactsDownloaded(ArrayList<String> contacts);
    }
}