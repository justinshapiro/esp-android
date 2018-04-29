package org.espmobile.esp_mobile.alerts;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Location;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;

import java.util.ArrayList;

public class AlertsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Lookup the recyclerview in activity layout
        final RecyclerView rvAlerts = findViewById(R.id.rvAlerts);

        // Set layout manager to position the items
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));

        // add separator between cells
        rvAlerts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvAlerts.setHasFixedSize(true);
        rvAlerts.setItemAnimator(new DefaultItemAnimator());

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.alerts_disable_switch).setEnabled(false);
        findViewById(R.id.alerts_disable_switch).setAlpha((float) 0.75);
        ESPMobileAPI.getAlerts(this, new ParsedRequestListener<ArrayList<Location>>() {
            @Override
            public void onResponse(ArrayList<Location> locations) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                findViewById(R.id.alerts_disable_switch).setEnabled(true);
                findViewById(R.id.alerts_disable_switch).setAlpha(1);
                rvAlerts.setAdapter(new AlertsAdapter(getApplicationContext(), locations));
            }
            @Override
            public void onError(ANError error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                findViewById(R.id.alerts_disable_switch).setEnabled(true);
                findViewById(R.id.alerts_disable_switch).setAlpha(1);
                MessageBanner.show(AlertsActivity.this, R.string.error_message, R.color.espRed);
                Log.d("API", error.toString());
            }
        });

        ((Switch) findViewById(R.id.alerts_disable_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences.Editor ed = getSharedPreferences("alerts", MODE_PRIVATE).edit();
                ed.putBoolean("global_disable_alerts", isChecked);
                ed.apply();

                rvAlerts.setEnabled(!isChecked);
                rvAlerts.setAlpha(!isChecked ? 1 : (float) 0.5);
            }
        });

        if (getSharedPreferences("alerts", MODE_PRIVATE).getBoolean("global_disable_alerts", false)) {
            ((Switch) findViewById(R.id.alerts_disable_switch)).setChecked(true);
            rvAlerts.setEnabled(false);
            rvAlerts.setAlpha((float) 0.5);
        } else {
            ((Switch) findViewById(R.id.alerts_disable_switch)).setChecked(false);
            rvAlerts.setEnabled(true);
            rvAlerts.setAlpha(1);
        }
    }
}
