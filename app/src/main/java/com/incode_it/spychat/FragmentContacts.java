package com.incode_it.spychat;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;


public class FragmentContacts extends Fragment {

    RecyclerView recyclerView;
    ServiceConnection sConn;
    boolean bound = false;
    private OnFragmentInteractionListener mListener;
    MyContactRecyclerViewAdapter adapter;



    public static FragmentContacts newInstance() {
        FragmentContacts fragment = new FragmentContacts();
        return fragment;
    }

    public FragmentContacts() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.d(MainActivity.FRAGMENT_SETTINGS, "Contacts "+this.hashCode());
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyContactRecyclerViewAdapter(MyContacts.getContactsList(getContext()), mListener);
        recyclerView.setAdapter(adapter);

        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(MyService.LOG_TAG, "onServiceConnected");
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(MyService.LOG_TAG, "onServiceDisconnected");
                bound = false;
            }
        };
        return recyclerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contacts, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//              if (searchView.isExpanded() && TextUtils.isEmpty(newText)) {
                callSearch(newText);
//              }
                return true;
            }

            public void callSearch(String query)
            {
                ArrayList<MyContacts.Contact> arrayList = MyContacts.getContactsList(getContext());
                ArrayList<MyContacts.Contact> newArrayList = new ArrayList<>();
                for (MyContacts.Contact contact: arrayList)
                {
                    if (contact.name.toLowerCase().contains(query.toLowerCase()))
                    {
                        newArrayList.add(contact);
                    }
                }
                adapter.setContacts(newArrayList);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_search:
                //getContext().stopService(new Intent(getContext(), MyService.class));
                //getContext().unbindService(sConn);
                break;

        }

        return true;
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
