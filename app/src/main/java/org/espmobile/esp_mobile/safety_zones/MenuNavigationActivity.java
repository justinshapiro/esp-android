package org.espmobile.esp_mobile.safety_zones;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.MapEnabledCompatActivity;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.alerts.AlertsActivity;
import org.espmobile.esp_mobile.contacts.EmergencyContactsActivity;
import org.espmobile.esp_mobile.feedback.ProvideFeedbackFinishedActivity;
import org.espmobile.esp_mobile.feedback.ProvideFeedbackPage1Activity;
import org.espmobile.esp_mobile.locations.CustomLocationsActivity;
import org.espmobile.esp_mobile.login.LoginActivity;
import org.espmobile.esp_mobile.profile.EditProfileActivity;
import org.json.JSONObject;

public abstract class MenuNavigationActivity extends MapEnabledCompatActivity
        implements MenuItem.OnMenuItemClickListener, DrawerLayout.DrawerListener {
    private FrameLayout viewStub;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Dialog logOutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_menu_navigation);

        viewStub = findViewById(R.id.view_stub);
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(this);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                0,
                0
        );
        drawerLayout.addDrawerListener(drawerToggle);

        ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Menu drawerMenu = navigationView.getMenu();
        for (int i = 0; i < drawerMenu.size(); i++) {
            drawerMenu.getItem(i).setOnMenuItemClickListener(this);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (viewStub != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            if (inflater != null) {
                View stubView = inflater.inflate(layoutResID, viewStub, false);
                viewStub.addView(stubView, params);
            }
        }
    }

    @Override
    public void setContentView(View view) {
        if (viewStub != null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            viewStub.addView(view, params);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (viewStub != null) {
            viewStub.addView(view, params);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        String userName = getSharedPreferences("nav_menu", MODE_PRIVATE).getString("name", "Your Profile");
        String userEmail = getSharedPreferences("nav_menu", MODE_PRIVATE).getString("email", "");
        ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.user_name)).setText(userName);
        ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.user_email)).setText(userEmail);
    }

    @Override public void onDrawerOpened(@NonNull View drawerView) {}
    @Override public void onDrawerClosed(@NonNull View drawerView) {}
    @Override public void onDrawerStateChanged(int newState) {}

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        // close the navigation drawer first
        drawerLayout.closeDrawers();

        // then go to the selected screen, 500 ms later
        // this allows for a smoother transition
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (item.getItemId()) {
                    case R.id.menu_profile:
                        Intent editProfileIntent = new Intent(
                                MenuNavigationActivity.this,
                                EditProfileActivity.class
                        );
                        startActivity(editProfileIntent);
                        break;
                    case R.id.menu_contacts:
                        Intent emergencyContactsIntent = new Intent(
                                MenuNavigationActivity.this,
                                EmergencyContactsActivity.class
                        );
                        startActivity(emergencyContactsIntent);
                        break;
                    case R.id.menu_locations:
                        Intent customLocationsIntent = new Intent(
                                MenuNavigationActivity.this,
                                CustomLocationsActivity.class
                        );
                        startActivity(customLocationsIntent);
                        break;
                    case R.id.menu_alerts:
                        Intent alertsIntent = new Intent(
                                MenuNavigationActivity.this,
                                AlertsActivity.class
                        );
                        startActivity(alertsIntent);
                        break;
                    default: break;
                }
            }
        }, 500);

        return false;
    }

    public void showLogOut(View v) {
        logOutDialog = new Dialog(this);
        logOutDialog.setContentView(R.layout.popup_logout);
        logOutDialog.show();
        drawerLayout.closeDrawers();
    }

    public void performLogOut(View v) {
        ESPMobileAPI.logout(this, new ParsedRequestListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }

            @Override
            public void onError(ANError anError) {

            }
        });

        Intent logOutIntent = new Intent(MenuNavigationActivity.this, LoginActivity.class);
        startActivity(logOutIntent);
        drawerLayout.closeDrawers();
        finish();
    }

    public void cancelLogOut(View v) {
        logOutDialog.dismiss();
    }

    public void showProvideFeedback(View v) {
        Intent feedbackIntent;
        if (getSharedPreferences("feedback", MODE_PRIVATE).getBoolean("feedback_received", false)) {
            feedbackIntent = new Intent(MenuNavigationActivity.this, ProvideFeedbackFinishedActivity.class);
            setResult(RESULT_FIRST_USER, feedbackIntent);
            startActivityForResult(feedbackIntent, RESULT_FIRST_USER);
        } else {
            feedbackIntent = new Intent(MenuNavigationActivity.this, ProvideFeedbackPage1Activity.class);
            startActivity(feedbackIntent);
        }

        drawerLayout.closeDrawers();
    }
}
