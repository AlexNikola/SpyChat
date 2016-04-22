package com.incode_it.spychat;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FragmentChat extends Fragment {

    private static final String TAG = "chatm";
    public static final String ARG_POSITION = "position";
    private int position;
    MyContacts.Contact contact;
    RecyclerView recyclerView;
    public View sendMessageView;
    private EditText editText;
    MyChatRecyclerViewAdapter adapter;
    Bitmap contactBitmap;
    private Context context;
    private View view;

    public FragmentChat() {
        // Required empty public constructor
    }

    public static FragmentChat newInstance(int position) {
        FragmentChat fragment = new FragmentChat();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
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
            position = getArguments().getInt(ARG_POSITION, 0);
        }
        Log.e(TAG, "onCreate position: " + position);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        contact = MyContacts.getContactsList(context).get(position);

        loadContactBitmap();

        MyDbHelper myDbHelper = new MyDbHelper(context);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        final ArrayList<Message> messageArrayList = MyDbHelper.readContactMessages(db, contact);

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
                    Message message = new Message(textMessage, ActivityMain.myPhoneNumber, contact.phoneNumber);
                    messageArrayList.add(message);
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                    MyDbHelper.insertMessage(new MyDbHelper(context).getWritableDatabase(), message);

                    new SendMessageTask().execute(message);
                }
            }
        });

        return view;
    }

    private class SendMessageTask extends AsyncTask<Message, Void, String>
    {
        Message message;

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
            }
            else
            {
                if (result.equals("success"))
                {
                    message.state = Message.STATE_SUCCESS;
                    adapter.notifyDataSetChanged();
                }
                else if (result.equals("error"))
                {
                    message.state = Message.STATE_ERROR;
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String trySendMessage(String message) throws IOException, JSONException
    {
        StringBuilder sbParams = new StringBuilder();
        sbParams.append("message=").append(message).append("&").append("destination=").append(contact.phoneNumber);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = sharedPreferences.getString(C.ACCESS_TOKEN, "");

        URL url = new URL(C.BASE_URL + "api/v1/message/sendMessage/");
        Log.e(TAG, "URL: " + url.toString() + sbParams.toString());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.addRequestProperty("Authorization", "Bearer "+accessToken);
        httpURLConnection.connect();

        OutputStreamWriter outputWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
        outputWriter.write(sbParams.toString());
        outputWriter.flush();
        outputWriter.close();

        int httpResponse = httpURLConnection.getResponseCode();
        Log.e(TAG, "HTTP RESP CODE "+httpResponse);
        InputStream inputStream;

        if (httpResponse == HttpURLConnection.HTTP_OK) inputStream = httpURLConnection.getInputStream();
        else inputStream = httpURLConnection.getErrorStream();

        String response = IOUtils.toString(inputStream);
        String result = null;
        inputStream.close();
        Log.e(TAG, "resp: " + response);

        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(context, TAG))
                response = trySendMessage(message);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            result = jsonResponse.getString("result");
        }

        return result;
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
