package org.espmobile.esp_mobile;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.espmobile.esp_mobile.login.LoginActivity;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        new Handler().postDelayed(new Runnable(){
            @Override public void run() { setupLaunchActivity(); }
        }, 2000);

        AppCenter.start(getApplication(), "d3dbc6d9-beff-4ded-b5e2-839f7fb58bae",
                Analytics.class, Crashes.class);
    }

    private void setupLaunchActivity() {
        getSharedPreferences("Login", MODE_PRIVATE).edit().putBoolean("distress_mode", false).apply();
        if (getSharedPreferences("Login", MODE_PRIVATE).getString("loggedInUser", "").equals("")){
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(this, LoginActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );

            Intent mainIntent = new Intent(this, LoginActivity.class);
            startActivity(mainIntent);
            finish();
        } else {
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(this, SafetyZonesActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );

            Intent mainIntent = new Intent(this, SafetyZonesActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }
}
