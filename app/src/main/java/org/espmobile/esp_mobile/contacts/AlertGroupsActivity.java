package org.espmobile.esp_mobile.contacts;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.*;
import org.espmobile.esp_mobile.Contact;
import org.json.JSONObject;

import java.util.ArrayList;

public class AlertGroupsActivity extends AppCompatActivity implements IListener {
    RecyclerView rvExistingContacts;
    TextView tvExistingContacts;
    RecyclerView rvAlertGroup;
    TextView tvAlertGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_groups);

        Toolbar toolbar = findViewById(R.id.alert_groups_toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        rvAlertGroup = findViewById(R.id.rvAlertGroup);
        tvExistingContacts = findViewById(R.id.non_alert_groups_prompt);
        tvAlertGroup = findViewById(R.id.current_alert_group_prompt);

        rvExistingContacts = findViewById(R.id.rvExistingContacts);
        final LinearLayoutManager rvExistingContactsManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        rvExistingContacts.setLayoutManager(rvExistingContactsManager);

        renderWaitingState(true);
        ESPMobileAPI.getContacts(this, new ParsedRequestListener<ArrayList<org.espmobile.esp_mobile.Contact>>() {
            @Override
            public void onResponse(final ArrayList<Contact> response) {
                renderWaitingState(false);

                final ArrayList<Contact> existingContacts = new ArrayList<>();
                final ArrayList<Contact> alertGroupContacts = new ArrayList<>();

                for (Contact c : response) {
                    if (c.getGroupID() == null) {
                        existingContacts.add(c);
                    } else {
                        alertGroupContacts.add(c);
                    }
                }

                final AlertGroupsAdapter existingContactsAdapter = new AlertGroupsAdapter(
                        existingContacts,
                        AlertGroupsActivity.this,
                        true
                );
                rvExistingContacts.setAdapter(existingContactsAdapter);
                rvExistingContacts.setOnDragListener(existingContactsAdapter.getDragInstance());

                final AlertGroupsAdapter alertGroupsAdapter = new AlertGroupsAdapter(
                        alertGroupContacts,
                        AlertGroupsActivity.this,
                        false
                );
                rvAlertGroup.setAdapter(alertGroupsAdapter);
                rvAlertGroup.setOnDragListener(alertGroupsAdapter.getDragInstance());
                rvAlertGroup.setLayoutManager(new LinearLayoutManager(AlertGroupsActivity.this));

                // add separator between cells
                rvAlertGroup.addItemDecoration(new DividerItemDecoration(AlertGroupsActivity.this, DividerItemDecoration.VERTICAL));
                rvAlertGroup.setHasFixedSize(true);
                rvAlertGroup.setItemAnimator(new DefaultItemAnimator());
            }

            @Override
            public void onError(ANError anError) {
                renderWaitingState(false);
                MessageBanner.show(AlertGroupsActivity.this, R.string.error_message, R.color.espRed);
            }
        });

        configureEnabledAction();
        configureAlertGroupAction();
    }

    private void renderWaitingState(Boolean isWaiting) {
        findViewById(R.id.progressBar).setVisibility(isWaiting ? View.VISIBLE : View.GONE);
        rvExistingContacts.setEnabled(!isWaiting);
        rvAlertGroup.setEnabled(!isWaiting);
        findViewById(R.id.enabled_switch).setEnabled(!isWaiting);
        findViewById(R.id.save_button).setEnabled(!isWaiting);
        findViewById(R.id.save_button).setAlpha(isWaiting ? (float) 0.75 : 1);
    }

    private void renderDisabledState(Boolean isDisabled) {
        rvExistingContacts.setEnabled(!isDisabled);
        rvExistingContacts.setAlpha(isDisabled ? (float) 0.5 : 1);
        rvAlertGroup.setEnabled(!isDisabled);
        rvAlertGroup.setAlpha(isDisabled ? (float) 0.5 : 1);
        findViewById(R.id.save_button).setEnabled(!isDisabled);
        findViewById(R.id.save_button).setAlpha(isDisabled ? (float) 0.5 : 1);
        ((Button) findViewById(R.id.save_button)).setTextColor(isDisabled ? Color.WHITE : Color.BLUE);
    }

    private void configureAlertGroupAction() {
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderWaitingState(true);
                ESPMobileAPI.deleteContactGroup(AlertGroupsActivity.this, new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Contact> newContactGroup = ((AlertGroupsAdapter) rvAlertGroup.getAdapter()).getDataSource();
                        ESPMobileAPI.addContactGroup(AlertGroupsActivity.this, newContactGroup, new ParsedRequestListener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                finish();
                            }

                            @Override
                            public void onError(ANError anError) {
                                renderWaitingState(false);
                                MessageBanner.show(AlertGroupsActivity.this, R.string.error_message, R.color.espRed);
                            }
                        });
                    }

                    @Override
                    public void onError(ANError anError) {
                        renderWaitingState(false);
                        MessageBanner.show(AlertGroupsActivity.this, R.string.error_message, R.color.espRed);
                    }
                });
            }
        });
    }

    private void configureEnabledAction() {
        ((Switch) findViewById(R.id.enabled_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                renderDisabledState(!isChecked);
            }
        });
    }

    @Override
    public void setEmptyListTop(boolean visibility) {
        tvExistingContacts.setVisibility(visibility ? View.VISIBLE : View.GONE);
        rvExistingContacts.setVisibility(visibility ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setEmptyListBottom(boolean visibility) {
        tvAlertGroup.setVisibility(visibility ? View.VISIBLE : View.GONE);
        rvAlertGroup.setVisibility(visibility ? View.GONE : View.VISIBLE);
    }
}
