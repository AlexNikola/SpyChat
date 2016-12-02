package com.incode_it.spychat.contacts;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.incode_it.spychat.AddEmailActivity;
import com.incode_it.spychat.C;
import com.incode_it.spychat.ContactsComparator;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.util.ArrayList;
import java.util.Collections;

public class FragmentContacts extends Fragment implements UpdateContactsTask.Callback {

    public static final String FRAGMENT_CONTACTS = "fr_con";

    private RecyclerView recyclerView;
    private MyContactRecyclerViewAdapter adapter;
    private UpdateContactsTask updateContactsTask;
    private boolean isMessageReceiverRegistered;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean isExpanded;
    private String searchQuery = "";

    public static FragmentContacts newInstance() {
        return new FragmentContacts();
    }

    public FragmentContacts() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        checkEmail();
    }

    @Override
    public void onStart() {
        super.onStart();
        initMessageReceiver();
        updateNumbersOfUnreadMessages();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        isMessageReceiverRegistered = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d("fdfdsgfds", "onAttach: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_contact_list, container, false);
        localUpdateContactList();
        serverUpdateContacts();
        initRecyclerView();
        return recyclerView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == C.REQUEST_CODE_ACTIVITY_ADD_EMAIL && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Email successfully added", Toast.LENGTH_SHORT).show();
        }
    }

    private void localUpdateContactList()
    {
        ArrayList<String> registeredContacts;
        registeredContacts = MyDbHelper.readRegisteredContacts(new MyDbHelper(getContext()).getReadableDatabase());
        for (MyContacts.Contact contact: MyContacts.getContacts(getContext()))
        {
            contact.isRegistered = false;
            for (String registeredPhone: registeredContacts)
            {
                if (contact.phoneNumber.equals(registeredPhone))
                {
                    contact.isRegistered = true;
                    break;
                }
            }
        }

        Collections.sort(MyContacts.getContacts(getContext()), new ContactsComparator());
    }

    private void serverUpdateContacts()
    {
        ArrayList<String> contactsNumbers = new ArrayList<>();
        for (MyContacts.Contact contact: MyContacts.getContacts(getContext())) {
            contactsNumbers.add(contact.phoneNumber);
        }
        if (updateContactsTask == null) {
            updateContactsTask = new UpdateContactsTask();
            updateContactsTask.setCallback(this, getContext());
            updateContactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, contactsNumbers);
        } else if (updateContactsTask.isRunning) {
            updateContactsTask.setCallback(this, getContext());
        }

    }

    @Override
    public void onContactsDownloaded(ArrayList<String> contacts) {
        if (contacts != null) {
            for (MyContacts.Contact mContact: MyContacts.getContacts(getContext())) {
                mContact.isRegistered = false;
                for (String regPhoneNumber: contacts)
                {
                    if (mContact.phoneNumber.equals(regPhoneNumber))
                    {
                        mContact.isRegistered = true;
                        break;
                    }
                }
            }
            Collections.sort(MyContacts.getContacts(getContext()), new ContactsComparator());
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }



    private void checkEmail()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String email = sharedPreferences.getString(C.SHARED_MY_EMAIL, "");
        if (email.equals("")) {
            Intent intent = new Intent(getContext(), AddEmailActivity.class);
            startActivityForResult(intent, C.REQUEST_CODE_ACTIVITY_ADD_EMAIL);
        }
    }





    private void initMessageReceiver()
    {
        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String phone = intent.getStringExtra(C.EXTRA_OPPONENT_PHONE_NUMBER);
                for(int i = 0; i < MyContacts.getContacts(context).size(); i++)
                {
                    MyContacts.Contact contact = MyContacts.getContacts(context).get(i);
                    if (contact.phoneNumber.equals(phone))
                    {
                        contact.countUnread ++;
                        if (adapter != null) adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        };

        registerMessageReceiver();
    }

    private void registerMessageReceiver(){
        if(!isMessageReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.RECEIVE_MESSAGE));
            isMessageReceiverRegistered = true;
        }
    }

    private void updateNumbersOfUnreadMessages()
    {
        for(MyContacts.Contact contact: MyContacts.getContacts(getContext()))
        {
            ArrayList<Message> messages = MyDbHelper.readContactMessages(new MyDbHelper(getContext()).getReadableDatabase(), contact, getContext());
            int count = 0;
            for (Message message: messages)
            {
                if (message.isViewed == 0) count++;
            }
            contact.countUnread = count;
        }
    }

    private void initRecyclerView()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyContactRecyclerViewAdapter(getContext());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contacts, menu);
        MenuItem actionMenuItem = menu.findItem(R.id.action_search);
        if (isExpanded) actionMenuItem.expandActionView();
        MenuItemCompat.setOnActionExpandListener(actionMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isExpanded = true;
                MyContacts.initSearchableContacts(getContext());
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isExpanded = false;
                return true;
            }
        });
        final SearchView searchView = (SearchView) actionMenuItem.getActionView();
        searchView.setQuery(searchQuery, false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callSearch(newText);
                return true;
            }

            public void callSearch(String query)
            {
                searchQuery = query;
                MyContacts.getContacts(getContext()).clear();
                if (query.length() > 0)
                {
                    for (MyContacts.Contact contact: MyContacts.getSearchableContacts())
                    {
                        if (contact.name.toLowerCase().contains(query.toLowerCase()))
                        {
                            contact.setSearchableSubString(query);
                            MyContacts.getContacts(getContext()).add(contact);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    MyContacts.getContacts(getContext()).clear();
                    MyContacts.getContacts(getContext()).addAll(MyContacts.getSearchableContacts());
                    for (MyContacts.Contact contact: MyContacts.getContacts(getContext()))
                    {
                        contact.searchableSubString = "";
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_search:

                break;

        }

        return true;
    }



}
