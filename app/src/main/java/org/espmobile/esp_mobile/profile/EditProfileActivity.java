package org.espmobile.esp_mobile.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.UserInfo;
import org.espmobile.esp_mobile.contacts.AlertGroupsActivity;
import org.espmobile.esp_mobile.login.LoginActivity;
import org.espmobile.esp_mobile.R;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {
    Dialog changePasswordDialog;
    Dialog deleteAccountDialog;
    String currentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.ep_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        renderWaitingState(true);
        ESPMobileAPI.getUserInfo(this, new ParsedRequestListener<UserInfo>() {
            @Override
            public void onResponse(UserInfo response) {
                renderWaitingState(false);

                String[] nameComponents = response.getName().split(" ");
                String firstName;
                String lastName;
                if (nameComponents.length > 1) {
                    firstName = nameComponents[0];
                    lastName = nameComponents[1];
                } else {
                    firstName = nameComponents[0];
                    lastName = "";
                }

                ((EditText) findViewById(R.id.ep_first_name_field)).setText(firstName);
                ((EditText) findViewById(R.id.ep_last_name_field)).setText(lastName);
                ((EditText) findViewById(R.id.ep_email_field)).setText(response.getEmail());
                ((EditText) findViewById(R.id.ep_username_field)).setText(response.getUsername());

                currentPassword = response.getPassword();
            }

            @Override
            public void onError(ANError anError) {
                renderWaitingState(false);
                MessageBanner.show(EditProfileActivity.this, R.string.error_message, R.color.espRed);

                findViewById(R.id.change_password_button).setEnabled(false);
                findViewById(R.id.change_password_button).setAlpha((float) 0.5);
            }
        });

        findViewById(R.id.ep_username_field).setEnabled(false);
        findViewById(R.id.ep_username_field).setAlpha((float) 0.5);
    }

    public void changePassword(View v) {
        changePasswordDialog = new Dialog(this);
        changePasswordDialog.setContentView(R.layout.popup_change_password);

        Window currentWindow = changePasswordDialog.getWindow();
        if (currentWindow != null) {
            currentWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        final EditText currentPasswordField = findViewById(R.id.current_password_field);
        final EditText newPasswordField = findViewById(R.id.new_password_field);
        final EditText confirmPasswordField = findViewById(R.id.confirm_password_field);

        findViewById(R.id.change_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPasswordField.getText().toString().equals(currentPassword) &&
                    newPasswordField.getText().toString().equals(confirmPasswordField.getText().toString())) {

                    changePasswordDialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    ESPMobileAPI.updatePassword(EditProfileActivity.this, newPasswordField.getText().toString(), new ParsedRequestListener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            changePasswordDialog.dismiss();
                        }

                        @Override
                        public void onError(ANError anError) {
                            changePasswordDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                            MessageBanner.show(EditProfileActivity.this, R.string.error_message, R.color.espRed);
                        }
                    });
                } else {
                    MessageBanner.show(EditProfileActivity.this, R.string.error_message, R.color.espRed);
                }
            }
        });

        changePasswordDialog.show();
    }

    public void dialogChangePassword(View v) {
        changePasswordDialog.dismiss();
    }

    public void deleteAccount(View v) {
        deleteAccountDialog = new Dialog(this);
        deleteAccountDialog.setContentView(R.layout.popup_delete_account);

        Window currentWindow = deleteAccountDialog.getWindow();
        if (currentWindow != null) {
            currentWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        findViewById(R.id.delete_account_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccountDialog.dismiss();
                renderWaitingState(true);
                ESPMobileAPI.deleteUser(EditProfileActivity.this, new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent logOutIntent = new Intent(EditProfileActivity.this, LoginActivity.class);
                        logOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(logOutIntent);
                        finish();
                    }

                    @Override
                    public void onError(ANError anError) {
                        renderWaitingState(false);
                        MessageBanner.show(EditProfileActivity.this, R.string.error_message, R.color.espRed);
                    }
                });
            }
        });

        deleteAccountDialog.show();
    }

    public void cancelDelete(View v) {
        deleteAccountDialog.dismiss();
    }

    public void updateProfile(View v) {
        renderWaitingState(true);
        EditText firstNameField = findViewById(R.id.ep_first_name_field);
        EditText lastNameField = findViewById(R.id.ep_last_name_field);

        String name;
        if (!lastNameField.getText().toString().equals("")) {
            name = firstNameField.getText().toString() + " " + lastNameField.getText();
        } else {
            name = firstNameField.getText().toString();
        }

        ESPMobileAPI.updateUserName(this, name, new ParsedRequestListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                EditText emailField = findViewById(R.id.ep_email_field);
                ESPMobileAPI.updateUserEmail(EditProfileActivity.this, emailField.getText().toString(), new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        finish();
                    }

                    @Override
                    public void onError(ANError anError) {
                        renderWaitingState(false);
                        MessageBanner.show(EditProfileActivity.this, R.string.error_message, R.color.espRed);
                    }
                });
            }

            @Override
            public void onError(ANError anError) {
                renderWaitingState(false);
                MessageBanner.show(EditProfileActivity.this, R.string.error_message, R.color.espRed);
            }
        });
    }

    private void renderWaitingState(Boolean isWaiting) {
        findViewById(R.id.progressBar).setVisibility(isWaiting ? View.VISIBLE : View.GONE);
        findViewById(R.id.update_button).setEnabled(!isWaiting);
        findViewById(R.id.update_button).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.ep_first_name_field).setEnabled(!isWaiting);
        findViewById(R.id.ep_first_name_field).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.ep_last_name_field).setEnabled(!isWaiting);
        findViewById(R.id.ep_last_name_field).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.ep_email_field).setEnabled(!isWaiting);
        findViewById(R.id.ep_email_field).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.change_password_button).setEnabled(!isWaiting);
        findViewById(R.id.change_password_button).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.delete_account_button).setEnabled(!isWaiting);
        findViewById(R.id.delete_account_button).setAlpha(isWaiting ? (float) 0.5 : 1);
    }
}