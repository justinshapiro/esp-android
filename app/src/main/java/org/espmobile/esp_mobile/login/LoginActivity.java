package org.espmobile.esp_mobile.login;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.LocationUpdateService;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.UserInfo;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    public void goToMap(View view) {
        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(
                new ComponentName(this, SafetyZonesActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        Intent intent = new Intent(LoginActivity.this, SafetyZonesActivity.class);
        startActivity(intent);

        // Start the background job which performs location updates for alerts
        startScheduledJob();

        // we never want to go back to the login screen. The user must "Log Out" to do this
        finish();
    }

    public void goToCreateAccount(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    public void goToDistressMode(View v) {
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("distress_mode", true);
        ed.apply();

        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(
                new ComponentName(this, SafetyZonesActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        Intent intent = new Intent(this, SafetyZonesActivity.class);
        startActivity(intent);

        // don't call finish(), as we want to come back here if we're in distress mode
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signInButton = findViewById(R.id.email_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(view);
            }
        });

        // Set up the login form.
        mEmailView = findViewById(R.id.username_field);
        populateAutoComplete();

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin(textView);
                    return true;
                }

                return false;
            }
        });
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, 0);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, 0);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void attemptLogin(final View view) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Credentials meet the requirements, send a login request to the API

            renderWaitingState(true);
            ESPMobileAPI.login(LoginActivity.this, email, password, new ParsedRequestListener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    goToMap(view);
                }

                @Override
                public void onError(ANError error) {
                    MessageBanner.show(LoginActivity.this, R.string.invalid_credentials, R.color.espRed);
                    renderWaitingState(false);
                    Log.d("API", error.toString());
                }
            });
        }
    }

    private void renderWaitingState(Boolean isWaiting) {
        findViewById(R.id.progressBar).setVisibility(isWaiting ? View.VISIBLE : View.GONE);
        findViewById(R.id.email_sign_in_button).setEnabled(!isWaiting);
        findViewById(R.id.email_sign_in_button).setAlpha(!isWaiting ? 1 : (float) 0.5);
        findViewById(R.id.username_field).setEnabled(!isWaiting);
        findViewById(R.id.username_field).setAlpha(!isWaiting ? 1 : (float) 0.5);
        findViewById(R.id.password).setAlpha(!isWaiting ? 1 : (float) 0.5);
        findViewById(R.id.password).setEnabled(!isWaiting);
        findViewById(R.id.distress_button).setAlpha(!isWaiting ? 1: (float) 0.5);
        findViewById(R.id.distress_button).setEnabled(!isWaiting);
    }

    private boolean isEmailValid(String email) {
        // no requirements for username/email at this time
        return true;
    }

    private boolean isPasswordValid(String password) {
        // no requirements for passwords at this time
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    private void startScheduledJob() {
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo mJobInfo = new JobInfo.Builder(1,
                new ComponentName(getPackageName(), LocationUpdateService.class.getName()))
                .setMinimumLatency(10000)
                .build();

        if (mJobScheduler != null) {
            mJobScheduler.schedule(mJobInfo);
        }
    }
}
