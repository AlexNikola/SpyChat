package com.incode_it.spychat.interfaces;

public interface TaskCallback
{
    void onPreExecute();
    String doInBackground(String... params);
    void onPostExecute(String result);
    void onCancelled(String result);
}