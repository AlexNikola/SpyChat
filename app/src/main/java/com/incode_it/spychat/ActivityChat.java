package com.incode_it.spychat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
    ArrayList<Message> messageArrayList;
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



        messageArrayList = new ArrayList<>();
        messageArrayList.add(new Message("Message 1", "+380639461005"));
        messageArrayList.add(new Message("Message 2", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 3 Message 3 Message 3 Message 3 Message 3 Message 3 ", "+380639461005"));
        messageArrayList.add(new Message("Message 4 Message 4 Message 4", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 5", "+380639461005"));
        messageArrayList.add(new Message("Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6 Message 6", "+380639461005"));
        messageArrayList.add(new Message("Message 7", "+380639461005"));
        messageArrayList.add(new Message("Message 8 Message 8", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 9 Message 9 Message 9 Message 9 Message 9 Message 9", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 10 Message 10 Message 10", "+380639461005"));
        messageArrayList.add(new Message("Message 11", "+380639461005"));
        messageArrayList.add(new Message("Message 12", "+380639461005"));
        messageArrayList.add(new Message("Message 13", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 14", "+380639461005"));
        messageArrayList.add(new Message("Message 15", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 16 Message 16", "+380639461005"));
        messageArrayList.add(new Message("Message 17", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 18 Message 18", "+380639461005"));
        messageArrayList.add(new Message("Message 19", "+380639461005"));
        messageArrayList.add(new Message("Message 20", "+380639461005"));
        messageArrayList.add(new Message("Message 21 Message 21 Message 21 Message 21", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 22 Message 22 Message 22", MainActivity.myPhoneNumber));
        messageArrayList.add(new Message("Message 23", "+380639461005"));
        messageArrayList.add(new Message("Message 24 Message 24 Message 24 Message 24 Message 24 Message 24", "+380639461005"));
        messageArrayList.add(new Message("Message 25", "+380639461005"));
        messageArrayList.add(new Message("Message 26 Message 26 Message 26 Message 26 Message 26 Message 26 Message 26 Message 26", MainActivity.myPhoneNumber));


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
                String message = editText.getText().toString();
                editText.setText("");
                if (message.length() > 0)
                {
                    messageArrayList.add(new Message(message, MainActivity.myPhoneNumber));
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                }
            }
        });
    }

}
