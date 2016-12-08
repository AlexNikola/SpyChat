package com.incode_it.spychat;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.incode_it.spychat.interfaces.TaskCallback;

import java.lang.ref.WeakReference;

public abstract class FragmentLoader extends Fragment implements TaskCallback {
    protected static final String TAG = "FragmentLoader";
    private Task task;

    public FragmentLoader() {
        // Required empty public constructor
    }

    protected void startTask(String... params)
    {
        /*if (task != null) {
            task.cancel(true);
        }*/
        task = new Task();
        task.setCallback(this, getContext());
        task.execute(params);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        attachTaskCallback();
    }

    protected void attachTaskCallback() {
        Log.d(TAG, "attachTaskCallback: ");
        if (task != null && task.isRunning) {
            task.setCallback(this, getContext());
            onLoadingStateChanged(true);
        } else {
            onLoadingStateChanged(false);
        }
    }

    @Override
    public void onPreExecute() {
        Log.d(TAG, "onPreExecute: ");
        onLoadingStateChanged(true);
    }

    @Override
    public String doInBackground(String... params) {
        return null;
    }

    @Override
    public void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: ");
        onLoadingStateChanged(false);
    }

    @Override
    public void onCancelled(String result) {
        Log.d(TAG, "onCancelled: ");
        onLoadingStateChanged(false);
    }

    protected abstract void onLoadingStateChanged(boolean isLoading);


    private static class Task extends AsyncTask<String, Void, String>
    {
        WeakReference<TaskCallback> weekCallback;
        WeakReference<Context> weekContext;
        boolean isRunning = false;

        Task() {
        }

        void setCallback(TaskCallback asyncTaskCallback, Context context) {
            weekCallback = new WeakReference<>(asyncTaskCallback);
            weekContext = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRunning = true;
            TaskCallback callback = weekCallback.get();
            Context context = weekContext.get();
            if (callback != null && context != null) {
                callback.onPreExecute();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            TaskCallback callback = weekCallback.get();
            Context context = weekContext.get();
            if (callback != null && context != null) {
                return callback.doInBackground(params);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isRunning = false;
            TaskCallback callback = weekCallback.get();
            Context context = weekContext.get();
            if (callback != null && context != null) {
                callback.onPostExecute(result);
            }
        }

        @Override
        protected void onCancelled(String result) {
            isRunning = false;
            TaskCallback callback = weekCallback.get();
            Context context = weekContext.get();
            if (callback != null && context != null) {
                callback.onCancelled(result);
            }
        }
    }

    protected boolean isOnlyContainsNumbers(String number) {
        return number.matches("[0-9]+");
    }

    protected boolean validCellPhone(String number) {
        return Patterns.PHONE.matcher(number).matches();
    }


    protected boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    protected void hideKeyBoard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
