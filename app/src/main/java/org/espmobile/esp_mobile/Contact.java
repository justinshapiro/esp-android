package org.espmobile.esp_mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Contact {
    private String id;
    private String name;
    private String phone;
    private String groupID;

    public Contact() {
        this.id = "";
        this.name = "";
        this.phone = "";
        this.groupID = "";
    }

    public Contact(String id, String name, String phone, String groupID) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.groupID = groupID;
    }

    public Contact(JSONObject json) throws JSONException {
        this.id = json.getString("id");
        this.name = json.getString("name");
        this.phone = json.getString("phone");
        this.groupID = json.getString("group_id").equals("null") ? null : "1";
    }

    public Contact(HashMap<String, String> tabularRepresentation) {
        this.id = tabularRepresentation.getOrDefault("id","");
        this.name = tabularRepresentation.getOrDefault("name","");
        this.phone = tabularRepresentation.getOrDefault("phone","");
        this.groupID = tabularRepresentation.getOrDefault("group_id","");
    }

    static ArrayList<Contact> contactsFromJson(JSONObject json) throws JSONException {
        JSONArray jsonContacts = json.getJSONArray("ESP-Response");
        ArrayList<Contact> contacts = new ArrayList<>();

        for (int i = 0; i < jsonContacts.length(); i++) {
            contacts.add(new Contact(jsonContacts.getJSONObject(i)));
        }

        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        return contacts;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public static ArrayList<HashMap<String, String>> tabularRepresentation(ArrayList<Contact> contacts) {
        ArrayList<HashMap<String, String>> representation = new ArrayList<>();

        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        for (Contact contact : contacts) {
            HashMap<String, String> currentRepresentation = new HashMap<>();
            currentRepresentation.put("id", contact.getID());
            currentRepresentation.put("name", contact.getName());
            currentRepresentation.put("phone", contact.getPhone());
            currentRepresentation.put("group_id", contact.getGroupID());
            representation.add(currentRepresentation);
        }

        return representation;
    }

    public static ArrayList<ArrayList<HashMap<String, String>>> sectionalRepresentation(ArrayList<Contact> contacts) {
        ArrayList<ArrayList<HashMap<String, String>>> sectionalRepresentation = new ArrayList<>();
        ArrayList<HashMap<String, String>> tabularRepresentation = tabularRepresentation(contacts);
        String currentSectionKey = "";
        ArrayList<HashMap<String, String>> currentSection = new ArrayList<>();

        for (int i = 0; i < tabularRepresentation.size(); i++) {
            char firstLetterOfName = tabularRepresentation.get(i).get("name").toCharArray()[0];

            if (currentSectionKey.equals("")) {
                currentSectionKey = Character.toString(Character.toUpperCase(firstLetterOfName));
                currentSection.add(tabularRepresentation.get(i));

                if (tabularRepresentation.size() - 1 == i) {
                    sectionalRepresentation.add(currentSection);
                }
            } else {
                String currentKey = Character.toString(Character.toUpperCase(firstLetterOfName));
                if (!currentKey.equals(currentSectionKey)) {
                    sectionalRepresentation.add(currentSection);
                    currentSection = new ArrayList<>();
                    currentSection.add(tabularRepresentation.get(i));

                    if (tabularRepresentation.size() - 1 == i) {
                        sectionalRepresentation.add(currentSection);
                    }
                } else {
                    currentSection.add(tabularRepresentation.get(i));
                }
            }
        }

        return sectionalRepresentation;
    }

    public static String formatPhoneNumber(String phoneNumber) {
        char[] phoneNumberEnumerated = phoneNumber.toCharArray();
        StringBuilder formattedPhoneNumber = new StringBuilder();

        for (int i = 0; i < phoneNumberEnumerated.length; i++) {
            if (i == 0) {
                formattedPhoneNumber.append("(").append(phoneNumberEnumerated[i]);
            } else if (i == 3) {
                formattedPhoneNumber.append(") ").append(phoneNumberEnumerated[i]);
            } else if (i == 6) {
                formattedPhoneNumber.append("-").append(phoneNumberEnumerated[i]);
            } else {
                formattedPhoneNumber.append(phoneNumberEnumerated[i]);
            }
        }

        return formattedPhoneNumber.toString();
    }
}
