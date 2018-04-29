package org.espmobile.esp_mobile.contacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.Contact;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AddContactActivity extends AppCompatActivity {
    RecyclerView rvContacts;
    Dialog addContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        Toolbar toolbar = findViewById(R.id.add_contacts_toolbar);
        setSupportActionBar(toolbar);

        ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        rvContacts = findViewById(R.id.rvContacts);
        final PhoneContactsAdapter adapter = new PhoneContactsAdapter(this, getContactList());
        rvContacts.setAdapter(adapter);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        // add separator between cells
        rvContacts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvContacts.setHasFixedSize(true);
        rvContacts.setItemAnimator(new DefaultItemAnimator());

        final SearchView searchView = findViewById(R.id.add_contacts_search_bar);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.toolbar_title).setVisibility(View.GONE);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
                rvContacts.setAdapter(new PhoneContactsAdapter(AddContactActivity.this, getContactList()));
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterContacts(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterContacts(s);
                return true;
            }
        });
    }

    private void filterContacts(String query) {
        ArrayList<Contact> filteredList = new ArrayList<>();
        for (Contact c : getContactList()) {
            if (c.getName().contains(query)) {
                filteredList.add(c);
            }
        }

        rvContacts.setAdapter(new PhoneContactsAdapter(AddContactActivity.this, filteredList));
    }

    private ArrayList<Contact> getContactList() {
        ArrayList<Contact> phoneContacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null
                    );


                    while (pCur != null && pCur.moveToNext()) {
                        String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phone = phone
                                .replaceAll("-","")
                                .replaceAll("\\(","")
                                .replaceAll("\\)","")
                                .replaceAll(" ","");
                        phoneContacts.add(new Contact("", name, phone, null));
                    }

                    if (pCur != null) {
                        pCur.close();
                    }
                }
            }
        }

        if (cur != null) {
            cur.close();
        }

        Collections.sort(phoneContacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        return phoneContacts;
    }

    public void addContactManually(View view) {
        addContact = new Dialog(this);
        addContact.setContentView(R.layout.popup_edit_contact);

        Window currentWindow = addContact.getWindow();
        if (currentWindow != null) {
            currentWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Change title since the default is edit
        TextView title = addContact.findViewById(R.id.intro);
        title.setText(R.string.add_emergency_contact_title);

        final TextView firstNameField = addContact.findViewById(R.id.edit_first_name);
        final TextView lastNameField = addContact.findViewById(R.id.edit_last_name);
        final TextView phoneNumberField = addContact.findViewById(R.id.edit_phone_number);
        final Button saveButton = addContact.findViewById(R.id.edit_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                firstNameField.setEnabled(false);
                firstNameField.setAlpha((float) 0.75);
                lastNameField.setEnabled(false);
                lastNameField.setAlpha((float) 0.75);
                phoneNumberField.setEnabled(false);
                phoneNumberField.setAlpha((float) 0.75);
                saveButton.setEnabled(false);
                saveButton.setAlpha((float) 0.75);

                String contactName;
                if (!lastNameField.getText().toString().equals("")) {
                    contactName = firstNameField.getText().toString() + " " + lastNameField.getText().toString();
                } else {
                    contactName = firstNameField.getText().toString();
                }

                Contact newContact = new Contact("", contactName, phoneNumberField.getText().toString(), null);

                ESPMobileAPI.addContact(AddContactActivity.this, newContact, new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        addContact.dismiss();
                        finishWithMessage();
                    }

                    @Override
                    public void onError(ANError anError) {
                        firstNameField.setEnabled(true);
                        firstNameField.setAlpha(1);
                        lastNameField.setEnabled(true);
                        lastNameField.setAlpha(1);
                        phoneNumberField.setEnabled(true);
                        phoneNumberField.setAlpha(1);
                        saveButton.setEnabled(true);
                        saveButton.setAlpha(1);
                        MessageBanner.show(AddContactActivity.this, R.string.error_message, R.color.espRed);
                    }
                });
            }
        });

        addContact.show();
    }

    public void finishWithMessage() {
        Intent intent = new Intent();
        intent.putExtra("should_refresh_contact_list", true);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }
}
