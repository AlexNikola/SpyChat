package com.incode_it.spychat;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class FragmentChat extends Fragment {

    private static final int SEND_MESSAGE_DELAY = 500;
    private static final String TAG = "chatm";
    public static final String TAG_FRAGMENT = "FragmentChat";
    private String phone;
    MyContacts.Contact contact;
    RecyclerView recyclerView;
    public View sendMessageView;
    private EditText editText;
    MyChatRecyclerViewAdapter adapter;
    Bitmap contactBitmap;
    private Context context;
    private View view;

    ArrayList<Message> messageArrayList;

    private BroadcastReceiver mBroadcastReceiver;
    private boolean isReceiverRegistered;

    public FragmentChat() {
        // Required empty public constructor
    }

    public static FragmentChat newInstance(String phone) {
        FragmentChat fragment = new FragmentChat();
        Bundle args = new Bundle();
        args.putString(C.PHONE_NUMBER, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            phone = getArguments().getString(C.PHONE_NUMBER);
        }
        Log.e(TAG, "onCreate phone: " + phone);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(TAG, "onCreateView: " + view);
        if (view != null)
        {
            initBroadcastReceiver();
            return view;
        }
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        ArrayList<MyContacts.Contact> myContactsArrayList = MyContacts.getContactsList(context);
        for (MyContacts.Contact contact: myContactsArrayList)
        {
            if (contact.phoneNumber.equals(phone))
            {
                this.contact = contact;
                break;
            }
        }

        loadContactBitmap();

        MyDbHelper myDbHelper = new MyDbHelper(context);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        messageArrayList = MyDbHelper.readContactMessages(db, contact);

        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        adapter = new MyChatRecyclerViewAdapter(messageArrayList, contact, contactBitmap, context);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        layoutManager.setStackFromEnd(true);

        editText = (EditText) view.findViewById(R.id.edit_text);

        sendMessageView = view.findViewById(R.id.send_view);
        sendMessageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textMessage = editText.getText().toString();
                editText.setText("");
                if (textMessage.length() > 0)
                {
                    final Message message = new Message(textMessage, ActivityMain.myPhoneNumber, contact.phoneNumber);
                    messageArrayList.add(message);
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                    MyDbHelper.insertMessage(new MyDbHelper(context).getWritableDatabase(), message);

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
                        }
                    }, SEND_MESSAGE_DELAY);

                }
            }
        });

        initBroadcastReceiver();
        return view;
    }

    private class SendMessageTask extends AsyncTask<Message, Void, String>
    {
        private Message message;

        public SendMessageTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Message... params) {
            message = params[0];
            String textMessage = message.getMessage();
            String result = null;
            try
            {
                result = trySendMessage(textMessage);

            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null)
            {
                Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show();
                message.state = Message.STATE_ERROR;
                MyDbHelper.insertMessageState(new MyDbHelper(context).getWritableDatabase(), message);
                adapter.notifyDataSetChanged();
            }
            else
            {
                if (result.equals("success"))
                {
                    message.state = Message.STATE_SUCCESS;
                    MyDbHelper.insertMessageState(new MyDbHelper(context).getWritableDatabase(), message);
                    adapter.notifyDataSetChanged();
                }
                else if (result.equals("error"))
                {
                    message.state = Message.STATE_ERROR;
                    MyDbHelper.insertMessageState(new MyDbHelper(context).getWritableDatabase(), message);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String trySendMessage(String message) throws IOException, JSONException
    {
        StringBuilder sbParams = new StringBuilder();
        sbParams.append("message=").append(message).append("&").append("destination=").append(URLEncoder.encode(contact.phoneNumber, "UTF-8"));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = sharedPreferences.getString(C.ACCESS_TOKEN, "");
        URL url = new URL(C.BASE_URL + "api/v1/message/sendMessage/");
        String header = "Bearer "+accessToken;

        String response = MyConnection.post(url, sbParams.toString(), header);

        String result = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(context))
                result = trySendMessage(message);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            result = jsonResponse.getString("result");
        }

        return result;
    }

    private void initBroadcastReceiver()
    {
        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String textMessage = intent.getStringExtra(C.MESSAGE);
                String phone = intent.getStringExtra(C.PHONE_NUMBER);
                if (!phone.equals(contact.phoneNumber)) return;
                Message message = new Message(textMessage, phone, ActivityMain.myPhoneNumber);
                messageArrayList.add(message);
                adapter.notifyItemInserted(messageArrayList.size() - 1);
                recyclerView.scrollToPosition(messageArrayList.size() - 1);
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.RECEIVE_MESSAGE));
            isReceiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
        isReceiverRegistered = false;
        Log.e(TAG, "onPause Fragment Chat");
        super.onPause();
    }

    private void loadContactBitmap()
    {
        if (contact.photoURI != null)
        {
            InputStream image_stream = null;
            try {
                image_stream = context.getContentResolver().openInputStream(contact.photoURI);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                contactBitmap = BitmapFactory.decodeStream(image_stream, null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                if (image_stream != null) try {
                    image_stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
