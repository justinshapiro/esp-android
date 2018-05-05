package org.espmobile.esp_mobile.locations;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Location;
import org.espmobile.esp_mobile.MapEnabledCompatActivity;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;
import org.json.JSONObject;

public class AddLocationActivity extends MapEnabledCompatActivity {

    Dialog addLocationDialog;
    SupportMapFragment mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFrag.getMapAsync(this);

        PlaceAutocompleteFragment searchResultsFragment = (PlaceAutocompleteFragment)
        getFragmentManager().findFragmentById(R.id.search_results_fragment);

        View searchBoxView = searchResultsFragment.getView();
        if (searchBoxView != null) {
            searchBoxView.setBackgroundColor(Color.WHITE);
        }

        searchResultsFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                renderWaitingState(true);
                Location newLocation = new Location();
                newLocation.setName(place.getName().toString());
                newLocation.setAddress(place.getAddress().toString());
                newLocation.setLatitude(place.getLatLng().latitude);
                newLocation.setLongitude(place.getLatLng().longitude);
                newLocation.setPhoneNumber(Location.getCallable(place.getPhoneNumber().toString()));
                ESPMobileAPI.addCustomLocation(AddLocationActivity.this, newLocation, new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        finishWithMessage();
                    }

                    @Override
                    public void onError(ANError anError) {
                        renderWaitingState(false);
                        MessageBanner.show(AddLocationActivity.this, R.string.error_message, R.color.espRed);
                    }
                });
            }

            @Override
            public void onError(Status status) {
                MessageBanner.show(AddLocationActivity.this, R.string.error_message, R.color.espRed);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager != null) {
            android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                final float zoomLevel = ((int) (16 - Math.log((5 * 1609.34) / 500) / Math.log(2))) + .5f;
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
            } else {
                MessageBanner.show(AddLocationActivity.this, R.string.error_message, R.color.espRed);
            }
        }
    }

    public void addLocation(View v) {
        addLocationDialog = new Dialog(this);
        addLocationDialog.setContentView(R.layout.popup_add_custom_location);

        Window currentWindow = addLocationDialog.getWindow();
        if (currentWindow != null) {
            int transparentColor = android.graphics.Color.TRANSPARENT;
            currentWindow.setBackgroundDrawable(new ColorDrawable(transparentColor));
        }

        addLocationDialog.show();

        addLocationDialog.findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderWaitingState(true);
                Location newLocation = new Location();
                newLocation.setName(((TextView) addLocationDialog.findViewById(R.id.location_name)).getText().toString());
                newLocation.setLatitude(mGoogleMap.getCameraPosition().target.latitude);
                newLocation.setLongitude(mGoogleMap.getCameraPosition().target.longitude);
                newLocation.setPhoneNumber(((TextView) addLocationDialog.findViewById(R.id.phone_number)).getText().toString());
                newLocation.setAddress(((TextView) addLocationDialog.findViewById(R.id.address)).getText().toString());
                ESPMobileAPI.addCustomLocation(AddLocationActivity.this, newLocation, new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        addLocationDialog.dismiss();
                        finishWithMessage();
                    }

                    @Override
                    public void onError(ANError anError) {
                        renderWaitingState(false);
                        addLocationDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void dialogAddLocation(View v) {
        addLocationDialog.dismiss();
    }

    public void resetButtonClicked(View v) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager != null) {
            android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    }

    public void finishWithMessage() {
        Intent intent = new Intent();
        intent.putExtra("should_refresh_location_list", true);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }

    private void renderWaitingState(Boolean isWaiting) {
        findViewById(R.id.progressBar).setVisibility(isWaiting ? View.VISIBLE : View.GONE);
        findViewById(R.id.done_button).setEnabled(!isWaiting);
        findViewById(R.id.done_button).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.reset_button).setEnabled(!isWaiting);
        findViewById(R.id.reset_button).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.map2).setEnabled(!isWaiting);
        findViewById(R.id.map2).setAlpha(isWaiting ? (float) 0.5 : 1);
        findViewById(R.id.search_results_fragment).setEnabled(!isWaiting);
        findViewById(R.id.search_results_fragment).setAlpha(isWaiting ? (float) 0.5 : 1);
    }
}
