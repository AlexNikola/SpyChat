package com.incode_it.spychat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ActivityChat extends AppCompatActivity {
    MyContacts.Contact contact;
    RecyclerView recyclerView;
    ArrayList<String> arrayList;
    public View sendMessageView;
    private EditText editText;
    MyChatRecyclerViewAdapter adapter;

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



        arrayList = new ArrayList<>();
        arrayList.add("Message 1");
        arrayList.add("Message 2");
        arrayList.add("Message 3");
        arrayList.add("Message 4");
        arrayList.add("Message 5");
        arrayList.add("Message 6");
        arrayList.add("Message 7");
        arrayList.add("Message 8");
        arrayList.add("Message 9");
        arrayList.add("Message 10");
        arrayList.add("Message 11");
        arrayList.add("Message 12");
        arrayList.add("Message 13");
        arrayList.add("Message 14");
        arrayList.add("Message 15");
        arrayList.add("Message 16");
        arrayList.add("Message 17 Message 17 Message 17 Message 17 Message 17 Message 17 Message 17 Message 17 Message 17 Message 17 Message 17");
        arrayList.add("Message 18");
        arrayList.add("Message 19");
        arrayList.add("Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20 Message 20");

        recyclerView = (RecyclerView) findViewById(R.id.list);
        adapter = new MyChatRecyclerViewAdapter(arrayList);
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
                    arrayList.add(message);
                    adapter.notifyItemInserted(arrayList.size() - 1);
                    recyclerView.scrollToPosition(arrayList.size() - 1);
                }
            }
        });
    }

}
