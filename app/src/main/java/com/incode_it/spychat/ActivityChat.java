package com.incode_it.spychat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ActivityChat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String phone = getIntent().getStringExtra(C.PHONE_NUMBER);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fr = fragmentManager.findFragmentByTag(FragmentChat.TAG_FRAGMENT);
        if (fr == null)
        {
            FragmentChat fragment = FragmentChat.newInstance(phone);
            fragmentTransaction.add(R.id.fragment_chat_container, fragment, FragmentChat.TAG_FRAGMENT);
            fragmentTransaction.commit();
        }


    }
}
