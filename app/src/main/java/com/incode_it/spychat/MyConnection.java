package com.incode_it.spychat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyConnection
{

    public static boolean sendRefreshToken(Context context, String TAG) throws IOException, JSONException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String refreshToken = sharedPreferences.getString(C.REFRESH_TOKEN, "");
        Log.i(TAG, "sendRefreshToken: " + refreshToken);
        String urlParameters = "refreshToken=" + refreshToken;
        URL url = new URL(C.BASE_URL + "api/v1/auth/refreshAccessToke/");
        Log.i(TAG, "URL: " + url.toString() + urlParameters);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(20000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.connect();

        OutputStreamWriter outputWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
        outputWriter.write(urlParameters);
        outputWriter.flush();
        outputWriter.close();

        int httpResponse = httpURLConnection.getResponseCode();
        InputStream inputStream;
        if (httpResponse == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "HTTP_OK");
            inputStream = httpURLConnection.getInputStream();
        } else {
            Log.d(TAG, "HTTP_ERROR");
            inputStream = httpURLConnection.getErrorStream();
        }

        String response = IOUtils.toString(inputStream);
        Log.d(TAG, "resp: " + response);
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String res = jsonResponse.getString("result");
            if (res.equals("success"))
            {
                String accessToken = jsonResponse.getString("accessToken");
                refreshToken = jsonResponse.getString("refreshToken");
                sharedPreferences.edit().putString(C.ACCESS_TOKEN, accessToken).putString(C.REFRESH_TOKEN, refreshToken).apply();
                return true;
            }
        }
        catch (JSONException exc)
        {
            exc.printStackTrace();
        }
        return false;

    }
}
