package org.espmobile.esp_mobile.contacts;

import java.util.ArrayList;

public class Contact {
    private String mName;
    private String mPhone;

    public Contact(String name, String phone) {
        mName = name;
        mPhone = phone;
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public static ArrayList<Contact> createContactList(int numContacts) {
        ArrayList<Contact> locations = new ArrayList<Contact>();

        // will pull in data from the API here eventually
        for (int i = 1; i <= numContacts; i++) {
            locations.add(new Contact("Name" + i, "(111) 111-1111"));
        }

        return locations;
    }
}
