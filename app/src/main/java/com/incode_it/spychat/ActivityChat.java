package com.incode_it.spychat;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ActivityChat extends AppCompatActivity {
    MyContacts.Contact contact;
    RecyclerView recyclerView;
    public View sendMessageView;
    private EditText editText;
    MyChatRecyclerViewAdapter adapter;
    Bitmap contactBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int position = getIntent().getIntExtra("position", 0);
        contact = MyContacts.getContactsList(this).get(position);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(contact.name);
        setSupportActionBar(toolbar);

        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });


        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/
        toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (contact.photoURI != null)
        {
            InputStream image_stream = null;
            try {
                image_stream = getContentResolver().openInputStream(contact.photoURI);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                contactBitmap= BitmapFactory.decodeStream(image_stream, null, options);
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

        MyDbHelper myDbHelper = new MyDbHelper(this);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        final ArrayList<Message> messageArrayList = MyDbHelper.readContactMessages(db, contact);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        adapter = new MyChatRecyclerViewAdapter(messageArrayList, contact, contactBitmap, this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        layoutManager.setStackFromEnd(true);

        editText = (EditText) findViewById(R.id.edit_text);

        sendMessageView = findViewById(R.id.send_view);
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
                    MyDbHelper.insertMessage(new MyDbHelper(ActivityChat.this).getWritableDatabase(), message);
                }
            }
        });
    }

}
