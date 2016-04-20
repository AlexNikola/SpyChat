package com.incode_it.spychat;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class MyContacts
{
    public static ArrayList<Contact> getContactsList(Context context)
    {
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        if (phones != null) {
            while (phones.moveToNext())
            {
                String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                String photoURI = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                Uri uri = null;
                Log.d("mnun", name+" "+phoneNumber);
                if (photoURI != null)
                {
                    uri = Uri.parse(photoURI);
                }
                boolean isAdded = false;
                for (Contact contact: contactArrayList)
                {
                    if (contact.phoneNumber.equalsIgnoreCase(phoneNumber) || ActivityMain.myPhoneNumber.equalsIgnoreCase(phoneNumber))
                    {
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

    public static class Contact
    {
        String subString = "";
        String name;
        String phoneNumber;
        Uri photoURI;


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
