package org.espmobile.esp_mobile.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Contact;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;

import java.util.ArrayList;

public class EmergencyContactsActivity extends AppCompatActivity {
    RecyclerView rvContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        // add separator between cells
        rvContacts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvContacts.setHasFixedSize(true);
        rvContacts.setItemAnimator(new DefaultItemAnimator());

        putContactsIntoRecyclerView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getBooleanExtra("should_refresh_contact_list", false)) {
            data.putExtra("should_refresh_contact_list", false);
            putContactsIntoRecyclerView();
        }
    }

    private void putContactsIntoRecyclerView() {
        renderWaitingState(true);
        ESPMobileAPI.getContacts(this, new ParsedRequestListener<ArrayList<Contact>>() {
            @Override
            public void onResponse(ArrayList<Contact> response) {
                renderWaitingState(false);

                final ContactsAdapter adapter = new ContactsAdapter(EmergencyContactsActivity.this, response);
                rvContacts.setAdapter(adapter);
            }

            @Override
            public void onError(ANError anError) {
                renderWaitingState(false);
                MessageBanner.show(EmergencyContactsActivity.this, R.string.error_message, R.color.espRed);
            }
        });
    }

    void renderWaitingState(Boolean isWaiting) {
        findViewById(R.id.progressBar).setVisibility(isWaiting ? View.VISIBLE: View.GONE);
        findViewById(R.id.rvContacts).setEnabled(!isWaiting);
        findViewById(R.id.add_contacts_button).setEnabled(!isWaiting);
        findViewById(R.id.alert_groups_button).setEnabled(!isWaiting);
    }

    public void goToContacts(View view) {
        Intent intent = new Intent(
                EmergencyContactsActivity.this,
                AddContactActivity.class
        );
        startActivityForResult(intent, RESULT_FIRST_USER);
    }

    public void goToAlertGroups(View v) {
        Intent intent = new Intent(
                EmergencyContactsActivity.this,
                AlertGroupsActivity.class
        );
        startActivity(intent);
    }
}

