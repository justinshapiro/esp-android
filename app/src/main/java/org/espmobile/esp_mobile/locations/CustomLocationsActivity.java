package org.espmobile.esp_mobile.locations;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Location;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;

import java.util.ArrayList;

public class CustomLocationsActivity extends AppCompatActivity {

    RecyclerView rvLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_locations);

        ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        rvLocations = findViewById(R.id.rvLocations);
        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        rvLocations.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvLocations.setHasFixedSize(true);
        rvLocations.setItemAnimator(new DefaultItemAnimator());
        putLocationsIntoRecyclerView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getBooleanExtra("should_refresh_location_list", false)) {
            data.putExtra("should_refresh_location_list", false);
            putLocationsIntoRecyclerView();
        }
    }

    private void putLocationsIntoRecyclerView() {
        rvLocations.setEnabled(false);
        rvLocations.setAlpha((float) 0.5);
        findViewById(R.id.add_locations_button).setEnabled(false);
        findViewById(R.id.add_locations_button).setAlpha((float) 0.5);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        ESPMobileAPI.getUserLocations(this, new ParsedRequestListener<ArrayList<Location>>() {
            @Override
            public void onResponse(ArrayList<Location> locations) {
                rvLocations.setEnabled(true);
                rvLocations.setAlpha(1);
                findViewById(R.id.add_locations_button).setEnabled(true);
                findViewById(R.id.add_locations_button).setAlpha(1);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                rvLocations.setAdapter(new LocationsAdapter(CustomLocationsActivity.this, locations));
            }
            @Override
            public void onError(ANError error) {
                rvLocations.setEnabled(true);
                rvLocations.setAlpha(1);
                findViewById(R.id.add_locations_button).setEnabled(true);
                findViewById(R.id.add_locations_button).setAlpha(1);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                showError();
                Log.d("API", error.toString());
            }
        });
    }

    public void showError() {
        MessageBanner.show(CustomLocationsActivity.this, R.string.error_message, R.color.espRed);
    }

    public void addLocation(View v) {
        startActivityForResult(new Intent(this, AddLocationActivity.class), RESULT_FIRST_USER);
    }
}
