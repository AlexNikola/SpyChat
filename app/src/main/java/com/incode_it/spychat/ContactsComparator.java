package com.incode_it.spychat;

import java.util.Comparator;

public class ContactsComparator implements Comparator<MyContacts.Contact>
{
    @Override
    public int compare(MyContacts.Contact lhs, MyContacts.Contact rhs) {
        int result;
        if (lhs.isRegistered == rhs.isRegistered)
        {
            result = lhs.name.compareToIgnoreCase(rhs.name);
        }
        else if (lhs.isRegistered)
        {
            result = -1;
        }
        else
        {
            result = 1;
        }
        return result;
    }
}
