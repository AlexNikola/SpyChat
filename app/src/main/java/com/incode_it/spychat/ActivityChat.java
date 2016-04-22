package com.incode_it.spychat;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ActivityChat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int position = getIntent().getIntExtra("position", 0);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentChat fragment = FragmentChat.newInstance(position);
        fragmentTransaction.add(R.id.fragment_chat_container, fragment);
        fragmentTransaction.commit();

    }

}
