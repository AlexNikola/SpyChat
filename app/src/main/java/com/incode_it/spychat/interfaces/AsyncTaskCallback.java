package com.incode_it.spychat.interfaces;

public interface AsyncTaskCallback
{
    void onPreExecute();
    void onPostExecute(String result);
}