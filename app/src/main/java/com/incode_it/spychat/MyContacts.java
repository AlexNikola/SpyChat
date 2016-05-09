package com.incode_it.spychat;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

public class MyContacts
{
    public static ArrayList<Contact> getContactsList(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, null);
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        if (phones != null) {
            while (phones.moveToNext())
            {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                String photoURI = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                Log.d("conterr", "phoneNumber "+phoneNumber+" "+"name "+name);
                if (phoneNumber == null) continue;
                Uri uri = null;
                if (photoURI != null)
                {
                    uri = Uri.parse(photoURI);
                }
                boolean isAdded = false;
                for (Contact contact: contactArrayList)
                {
                    if (contact.phoneNumber.equalsIgnoreCase(phoneNumber))
                    {
                        isAdded = true;
                    }

                    if (myPhoneNumber != null)
                    {
                        if (myPhoneNumber.equalsIgnoreCase(phoneNumber))
                        isAdded = true;
                    }
                }
                if (!isAdded) contactArrayList.add(new Contact(name, phoneNumber, uri));
            }
        }
        if (phones != null) {
            phones.close();
        }
        return contactArrayList;
    }

    /*public static ArrayList<Contact> getContactsList(Context context)
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();

        contactArrayList.add(new Contact("Настя", "+380661234567", null));
        contactArrayList.add(new Contact("Aндрей", "+380665557778", null));
        contactArrayList.add(new Contact("Алексей", "+380662223344", null));
        contactArrayList.add(new Contact("Богдан", "+380669998855", null));
        contactArrayList.add(new Contact("Виктор", "+380661112255", null));
        return contactArrayList;
    }*/

    public static class Contact
    {
        public String subString = "";
        public String name;
        public String phoneNumber;
        public Uri photoURI;
        public boolean isRegistered;


        public Contact(String name, String phoneNumber, Uri photoURI) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.photoURI = photoURI;
        }

        public void setSubString(String subString)
        {
            this.subString = subString;
        }

    }
}
