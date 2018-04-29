package org.espmobile.esp_mobile.login;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.LocationUpdateService;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.UserInfo;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;

import org.json.JSONObject;

public class CreateAccountActivity extends AppCompatActivity {
    public static final String CREATE_ACCOUNT_MESSAGE = "org.espmobile.esp_mobile.CREATE_ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }

    public void createAccount(View v) {
        EditText etFirstName = findViewById(R.id.ca_first_name_field);
        EditText etLastName = findViewById(R.id.ca_last_name_field);
        EditText etEmail = findViewById(R.id.ca_email_field);
        EditText etUsername = findViewById(R.id.ca_username_field);
        EditText etPassword = findViewById(R.id.ca_password_field);
        EditText etConfirmPassword = findViewById(R.id.ca_confirm_password_field);

        // Check that required fields are not empty (return if empty)
        EditText[] requiredFields = {etFirstName, etEmail, etUsername, etPassword, etConfirmPassword};
        for (EditText et : requiredFields) {
            String etString = et.getText().toString().trim();
            if (TextUtils.isEmpty(etString)) {
                String hint = et.getHint().toString();
                et.setError(hint + " is required.");
                et.requestFocus();
                return;
            }
        }

        // Get strings
        String strFirstName = etFirstName.getText().toString().trim();
        String strLastName = etLastName.getText().toString().trim();
        String strEmail = etEmail.getText().toString().trim();
        String strUsername = etUsername.getText().toString().trim();
        String strPassword = etPassword.getText().toString().trim();
        String strConfirmPassword = etConfirmPassword.getText().toString().trim();

        // Check that passwords match
        if (!strPassword.equals(strConfirmPassword)) {
            etConfirmPassword.setError("Passwords must match.");
            etConfirmPassword.requestFocus();
            return;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setName(strFirstName + " " + strLastName);
        userInfo.setEmail(strEmail);
        userInfo.setPassword(strPassword);
        userInfo.setUsername(strUsername);
        userInfo.setConfirmPassword(strConfirmPassword);

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.create_account_button).setEnabled(false);
        findViewById(R.id.create_account_button).setAlpha((float) 0.75);
        ESPMobileAPI.addUser(userInfo, new ParsedRequestListener<UserInfo>() {
            @Override
            public void onResponse(UserInfo response) {
                ESPMobileAPI.login(CreateAccountActivity.this, response.getUsername(), response.getPassword(), new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        PackageManager packageManager = getPackageManager();
                        packageManager.setComponentEnabledSetting(
                                new ComponentName(getApplicationContext(), SafetyZonesActivity.class),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        Intent intent = new Intent(CreateAccountActivity.this, SafetyZonesActivity.class);
                        startActivity(intent);

                        // Start the background job which performs location updates for alerts
                        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                        JobInfo mJobInfo = new JobInfo.Builder(1,
                                new ComponentName(getPackageName(), LocationUpdateService.class.getName()))
                                .setMinimumLatency(10000)
                                .build();

                        if (mJobScheduler != null) {
                            mJobScheduler.schedule(mJobInfo);
                        }

                        // we never want to go back to the login screen. The user must "Log Out" to do this
                        finish();
                    }

                    @Override
                    public void onError(ANError anError) {
                        findViewById(R.id.create_account_button).setEnabled(true);
                        findViewById(R.id.create_account_button).setAlpha(1);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(ANError anError) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                findViewById(R.id.create_account_button).setEnabled(true);
                findViewById(R.id.create_account_button).setAlpha(1);
                Log.d("API", anError.toString());
            }
        });
    }
}
