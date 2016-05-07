package com.incode_it.spychat.contacts;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.ContactsComparator;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.R;
import com.incode_it.spychat.data_base.MyDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class FragmentContacts extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<MyContacts.Contact> mContacts = new ArrayList<>();
    private MyContactRecyclerViewAdapter adapter;

    private UpdateContactsTask updateContactsTask;

    public static FragmentContacts newInstance() {
        FragmentContacts fragment = new FragmentContacts();
        return fragment;
    }

    public FragmentContacts() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("qwew", "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("qwew", "onCreate ");
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_contact_list, container, false);
        loadMyContacts();
        initRecyclerView();
        localUpdateContactList();
        updateContacts();

        return recyclerView;
    }

    private void loadMyContacts()
    {
        Log.d("lodl", "loadMyContacts");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    C.READ_CONTACTS_CODE);
        }
        else
        {
            mContacts = MyContacts.getContactsList(getContext());
            Log.d("lodl", "mContacts "+mContacts.size());

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("lodl", "onRequestPermissionsResult " + requestCode);
        if (requestCode == C.READ_CONTACTS_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                ArrayList<MyContacts.Contact> arr = MyContacts.getContactsList(getContext());
                mContacts.clear();
                mContacts.addAll(arr);
                localUpdateContactList();
                updateContacts();
            }
            else
            {
                Toast.makeText(getContext(), "Sorry!!! Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void localUpdateContactList()
    {
        Log.d("lodl", "localUpdateContactList");
        ArrayList<String> registeredContacts;
        registeredContacts = MyDbHelper.readRegisteredContacts(new MyDbHelper(getContext()).getReadableDatabase());
        for (MyContacts.Contact contact: mContacts)
        {
            for (String registeredPhone: registeredContacts)
            {
                if (contact.phoneNumber.equals(registeredPhone))
                {
                    contact.isRegistered = true;
                    break;
                }
            }
        }

        Collections.sort(mContacts, new ContactsComparator());
        adapter.notifyDataSetChanged();
        Log.d("lodl", "mContacts "+mContacts.size());
    }

    private void updateContacts()
    {
        Log.d("lodl", "updateContacts");
        ArrayList<String> contactsNumbers = new ArrayList<>();
        for (MyContacts.Contact contact: mContacts)
        {
            contactsNumbers.add(contact.phoneNumber);
        }
        if (updateContactsTask == null)
        {
            updateContactsTask = new UpdateContactsTask(contactsNumbers);
            updateContactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    private class UpdateContactsTask extends AsyncTask<Void, Void, ArrayList<String>>
    {
        ArrayList<String> contactsNumbers;

        public UpdateContactsTask(ArrayList<String> contactsNumbers) {
            this.contactsNumbers = contactsNumbers;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> registeredContacts = null;
            try
            {
                JSONArray jsonArray = tryUpdateContacts(contactsNumbers);
                if (jsonArray != null)
                {
                    registeredContacts = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject contact = (JSONObject) jsonArray.get(i);
                        String phoneNumber = contact.getString("phone");
                        boolean isRegistered = contact.getBoolean("isRegistered");

                        if (isRegistered)
                        {
                            registeredContacts.add(phoneNumber);
                        }
                    }
                    MyDbHelper.insertRegisteredContacts(new MyDbHelper(getContext()).getWritableDatabase(), registeredContacts);

                }
            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();
            }

            return registeredContacts;
        }

        @Override
        protected void onPostExecute(ArrayList<String> regContacts) {
            if (regContacts != null)
            {
                for (MyContacts.Contact mContact: mContacts)
                {
                    mContact.isRegistered = false;
                    for (String regPhoneNumber: regContacts)
                    {
                        if (mContact.phoneNumber.equals(regPhoneNumber))
                        {
                            mContact.isRegistered = true;
                            break;
                        }
                    }
                }
                Collections.sort(mContacts, new ContactsComparator());
                adapter.notifyDataSetChanged();
                Log.d("lodl", "mContacts "+mContacts.size());
            }
            else
            {

                Context context = getContext();
                if (context != null)
                {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private JSONArray tryUpdateContacts(ArrayList<String> contactsNumbers) throws IOException, JSONException {
        StringBuilder sbParams = new StringBuilder();
        for (String number: contactsNumbers)
        {
            sbParams.append("contacts=").append(URLEncoder.encode(number, "UTF-8")).append("&");
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
        URL url = new URL(C.BASE_URL + "api/v1/usersJob/inSystem/");
        String header = "Bearer "+accessToken;

        String response = MyConnection.post(url, sbParams.toString(), header);

        JSONArray jsonArray = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(getContext()))
                jsonArray = tryUpdateContacts(contactsNumbers);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            String res = jsonResponse.getString("result");
            if (res.equals("success"))
                jsonArray = jsonResponse.getJSONArray("contacts");
        }

        return jsonArray;
    }

    private void initRecyclerView()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyContactRecyclerViewAdapter(getContext(), mContacts);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("qwew", "onCreateOptionsMenu");
        inflater.inflate(R.menu.contacts, menu);
        MenuItem actionMenuItem = menu.findItem(R.id.action_search);
        //if (isExpanded) actionMenuItem.expandActionView();
        /*MenuItemCompat.setOnActionExpandListener(actionMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isExpanded = false;
                return true;
            }
        });
*/
        SearchView searchView = (SearchView) actionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//              if (searchView.isExpanded() && TextUtils.isEmpty(newText)) {
                //callSearch(newText);
//              }
                return true;
            }

            public void callSearch(String query)
            {
                /*ArrayList<MyContacts.Contact> arrayList = adapter.mContacts;
                ArrayList<MyContacts.Contact> newArrayList = new ArrayList<>();
                if (query.length() > 0)
                {
                    for (MyContacts.Contact contact: arrayList)
                    {
                        if (contact.name.toLowerCase().contains(query.toLowerCase()))
                        {
                            contact.setSubString(query);
                            newArrayList.add(contact);
                        }
                    }
                    adapter.setContacts(newArrayList);
                }
                else adapter.restoreContacts();*/

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






    @Override
    public void onStop() {
        super.onStop();
        Log.d("qwew", "onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("qwew", "onDetach");
    }

}
