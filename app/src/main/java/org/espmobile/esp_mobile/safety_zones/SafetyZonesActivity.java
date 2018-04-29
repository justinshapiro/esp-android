package org.espmobile.esp_mobile.safety_zones;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.widget.ANImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.espmobile.esp_mobile.Contact;
import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Location;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.UserInfo;
import org.espmobile.esp_mobile.login.CreateAccountActivity;
import org.json.JSONObject;

import java.util.ArrayList;

public class SafetyZonesActivity extends MenuNavigationActivity {

    SupportMapFragment mapFrag;
    ArrayList<Button> selectedFilterButtons = new ArrayList<>();
    ArrayList<Location> allLocations = new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();
    double currentLatitude = 0;
    double currentLongitude = 0;

    // Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_zones);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        findViewById(R.id.filter_button).setTag(0);

        ((SeekBar) findViewById(R.id.radius_slider)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String front_spacing = "";
                if (i < 9) {
                    front_spacing = "  ";
                }
                String radius_label_value = front_spacing + Integer.toString(i + 1) + " mi";
                ((TextView) findViewById(R.id.radius_val_label)).setText(radius_label_value);

                final float zoomLevel = ((int) (16 - Math.log(((i + 1) * 1609.34) / 500) / Math.log(2))) + .5f;
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Display account creation message
        Intent intent = getIntent();
        String message = intent.getStringExtra(CreateAccountActivity.CREATE_ACCOUNT_MESSAGE);
        if (message != null && message.equals("success")) {
            MessageBanner.show(this, R.string.account_created, R.color.espGreen);
        }

        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);

        if (sp.getBoolean("distress_mode", false)) {
            TextView distressModelLabel = findViewById(R.id.distress_mode_label);
            distressModelLabel.setVisibility(View.VISIBLE);

            ActionBar currentActionBar = getSupportActionBar();
            if (currentActionBar != null) {
                currentActionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
                currentActionBar.setDisplayHomeAsUpEnabled(false);
            }

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.RED);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("distress_mode", false);
        ed.apply();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                final ViewGroup nullParent = null;
                View mapCallout = getLayoutInflater().inflate(R.layout.map_callout2, nullParent, false);

                Location currentLocation = null;
                for (Location location : allLocations) {
                    double mLat = marker.getPosition().latitude;
                    double mLon = marker.getPosition().longitude;
                    double currLat = location.getLatitude();
                    double currLon = location.getLongitude();
                    if (mLat == currLat && mLon == currLon) {
                        currentLocation = location;
                        break;
                    }
                }

                if (currentLocation != null) {
                    ((TextView) mapCallout.findViewById(R.id.location_name)).setText(currentLocation.getName());
                    ((TextView) mapCallout.findViewById(R.id.location_phone)).setText(currentLocation.getPhoneNumber());
                    ((TextView) mapCallout.findViewById(R.id.location_address)).setText(currentLocation.getAddress());
                }

                return mapCallout;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final Dialog locationDetailDialog = new Dialog(SafetyZonesActivity.this);
                locationDetailDialog.setContentView(R.layout.popup_location_detail);
                Window currentWindow = locationDetailDialog.getWindow();
                if (currentWindow != null) {
                    currentWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                Location currentLocation = null;
                for (Location location : allLocations) {
                    double mLat = marker.getPosition().latitude;
                    double mLon = marker.getPosition().longitude;
                    double currLat = location.getLatitude();
                    double currLon = location.getLongitude();
                    if (mLat == currLat && mLon == currLon) {
                        currentLocation = location;
                        break;
                    }
                }

                if (currentLocation != null) {
                    ((TextView) locationDetailDialog.findViewById(R.id.place_title)).setText(currentLocation.getName());
                    ((TextView) locationDetailDialog.findViewById(R.id.place_address)).setText(currentLocation.getAddress());
                    ((TextView) locationDetailDialog.findViewById(R.id.place_phone)).setText(currentLocation.getPhoneNumber());
                    ((TextView) locationDetailDialog.findViewById(R.id.zone_type)).setText(Location.capitalize(currentLocation.getCategory()));

                    String limitedLat = Location.limitDigits(Double.toString(currentLocation.getLatitude()), 5);
                    String limitedLng = Location.limitDigits(Double.toString(currentLocation.getLongitude()), 5);
                    String latLngStr = limitedLat + ", " + limitedLng;
                    ((TextView) locationDetailDialog.findViewById(R.id.zone_coords)).setText(latLngStr);

                    if (!getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE).getBoolean("distress_mode", true)) {
                        ((Switch) locationDetailDialog.findViewById(R.id.alerts_switch)).setChecked(currentLocation.getAlertable() == null);

                        if (getSharedPreferences("alerts", MODE_PRIVATE).getBoolean("global_disable_alerts", false)) {
                            ((Switch) locationDetailDialog.findViewById(R.id.alerts_switch)).setChecked(false);
                            locationDetailDialog.findViewById(R.id.alerts_switch).setEnabled(false);
                            locationDetailDialog.findViewById(R.id.alerts_switch).setAlpha((float) 0.5);
                        }
                    } else {
                        locationDetailDialog.findViewById(R.id.alerts_switch).setVisibility(View.GONE);
                    }

                    final String currentLocationID = currentLocation.getLocationID();
                    final String phoneNumber = Location.getCallable(currentLocation.getPhoneNumber());
                    final String phoneNumberFormatted = currentLocation.getPhoneNumber();

                    locationDetailDialog.findViewById(R.id.place_phone).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + phoneNumber));
                            if (ActivityCompat.checkSelfPermission(SafetyZonesActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[] { Manifest.permission.CALL_PHONE },1);
                                return;
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(SafetyZonesActivity.this, R.style.AlertDialogTheme);
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == DialogInterface.BUTTON_POSITIVE) {
                                        try {
                                            startActivity(callIntent);
                                            dialogInterface.dismiss();
                                        } catch (SecurityException e) { /* do nothing */ }

                                    } else {
                                        dialogInterface.dismiss();
                                    }
                                }
                            };

                            builder.setMessage("Call " + phoneNumberFormatted+ "?")
                                    .setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener)
                                    .show();
                        }
                    });

                    ((Switch) locationDetailDialog.findViewById(R.id.alerts_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton compoundButton, boolean isChecked) {
                            if (!isChecked) {
                                locationDetailDialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                                ESPMobileAPI.addAlert(SafetyZonesActivity.this, currentLocationID, new ParsedRequestListener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        locationDetailDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        compoundButton.setChecked(false);
                                        locationDetailDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
                                    }
                                });
                            } else {
                                locationDetailDialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                                ESPMobileAPI.deleteAlert(SafetyZonesActivity.this, currentLocationID, new ParsedRequestListener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        locationDetailDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        compoundButton.setChecked(true);
                                        locationDetailDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
                                    }
                                });
                            }
                        }
                    });

                    if (currentLocation.getCategory().equals("custom")) {
                        ((ANImageView) locationDetailDialog.findViewById(R.id.place_image)).setDefaultImageResId(R.drawable.custom_location_icon);
                        ((ANImageView) locationDetailDialog.findViewById(R.id.place_image)).setErrorImageResId(R.drawable.custom_location_icon);
                    } else {
                        ((ANImageView) locationDetailDialog.findViewById(R.id.place_image)).setDefaultImageResId(R.drawable.building_icon);
                        ((ANImageView) locationDetailDialog.findViewById(R.id.place_image)).setErrorImageResId(R.drawable.building_icon);
                    }

                    if (currentLocation.getPhotoRef() != null) {
                        ((ANImageView) locationDetailDialog.findViewById(R.id.place_image)).setImageUrl("http://espmobile.org/api/v1/locations/photo?photo_ref=" + currentLocation.getPhotoRef());
                    }

                    locationDetailDialog.show();
                }
            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                final double mLat = marker.getPosition().latitude;
                final double mLon = marker.getPosition().longitude;

                for (int i = 0; i < allLocations.size(); i++) {
                    if (allLocations.get(i).getLatitude() == mLat && allLocations.get(i).getLongitude() == mLon) {
                        final int index = i;
                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                        if (!allLocations.get(i).getCategory().equals("custom")) {
                            // we still need to get the phone number and photoRef
                            ESPMobileAPI.getLocation(allLocations.get(i).getLocationID(), new ParsedRequestListener<Location>() {
                                @Override
                                public void onResponse(Location response) {
                                    allLocations.set(index, response);

                                    if (getSharedPreferences("alerts", MODE_PRIVATE).getBoolean("global_disable_alerts", false)) {
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        marker.showInfoWindow();
                                    } else {
                                        if (!getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE).getBoolean("distress_mode", true)) {
                                            ESPMobileAPI.getAlerts(SafetyZonesActivity.this, new ParsedRequestListener<ArrayList<Location>>() {
                                                @Override
                                                public void onResponse(ArrayList<Location> response) {
                                                    for (Location alertableLocation : response) {
                                                        if (alertableLocation.getLatitude() == mLat && alertableLocation.getLongitude() == mLon) {
                                                            allLocations.get(index).setAlertable("false");
                                                            break;
                                                        }
                                                    }

                                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                                    marker.showInfoWindow();
                                                }

                                                @Override
                                                public void onError(ANError anError) {
                                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                                    MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
                                                }
                                            });
                                        } else {
                                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            marker.showInfoWindow();
                                        }
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
                                }
                            });
                        } else {
                            allLocations.get(i).setPhoneNumber(Contact.formatPhoneNumber(allLocations.get(i).getPhoneNumber()));
                            if (!getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE).getBoolean("distress_mode", true)) {
                                ESPMobileAPI.getAlerts(SafetyZonesActivity.this, new ParsedRequestListener<ArrayList<Location>>() {
                                    @Override
                                    public void onResponse(ArrayList<Location> response) {
                                        for (Location alertableLocation : response) {
                                            if (alertableLocation.getLatitude() == mLat && alertableLocation.getLongitude() == mLon) {
                                                allLocations.get(index).setAlertable("false");
                                                break;
                                            }
                                        }

                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        marker.showInfoWindow();
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
                                    }
                                });
                            } else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                marker.showInfoWindow();
                            }
                        }
                    }
                }

                return true;
            }
        });

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

                currentLatitude = latitude;
                currentLongitude = longitude;

                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
                final float zoomLevel = ((int) (16 - Math.log((5 * 1609.34) / 500) / Math.log(2))) + .5f;
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));

                putLocationsOnMap(latitude, longitude);
            } else {
                MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
            }
        }
    }

    private void putLocationsOnMap(Double latitude, Double longitude) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        ESPMobileAPI.safetyZones(this, latitude, longitude, 20 * 1609, new ParsedRequestListener<ArrayList<Location>>() {
            @Override
            public void onResponse(ArrayList<Location> response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                allLocations = response;
                for (Location location : response) {
                    addMarker(location);
                }
            }

            @Override
            public void onError(ANError anError) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        try {
            if (locationManager != null) {
                android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    putLocationsOnMap(location.getLatitude(), location.getLongitude());
                    buildGoogleApiClient();
                    mGoogleMap.setMyLocationEnabled(true);
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");

                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
                    final float zoomLevel = ((int) (16 - Math.log((5 * 1609.34) / 500) / Math.log(2))) + .5f;
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
                } else {
                    MessageBanner.show(SafetyZonesActivity.this, R.string.error_message, R.color.espRed);
                }
            }
        } catch (SecurityException e) { /* do nothing */ }
    }

    // Helper methods

    private void addMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        String pinType;
        switch (location.getCategory()) {
            case "police": pinType = "police_pin"; break;
            case "fire_station": pinType = "fire_pin"; break;
            case "hospital": pinType = "hospital_pin"; break;
            default: pinType = "custom_pin";
        }

        Resources r = getResources();
        Bitmap imageBitmap = BitmapFactory.decodeResource(r, r.getIdentifier(pinType, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                getResources().getInteger(R.integer.map_pin_width),
                getResources().getInteger(R.integer.map_pin_height),
                false
        );

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
        Marker m = mGoogleMap.addMarker(markerOptions);
        markers.add(m);
    }

    public void filterContentButtonTapped(View v) {
        Button b = findViewById(v.getId());
        Boolean markerVisibility;

        if (selectedFilterButtons.contains(b)) {
            b.setBackgroundResource(R.drawable.rounded_button_orange);
            b.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            selectedFilterButtons.remove(b);
            markerVisibility = true;
        } else {
            b.setBackgroundResource(R.drawable.rounded_button_white);
            b.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            selectedFilterButtons.add(b);
            markerVisibility = false;
        }

        String locationType = b.getText().toString();
        String categoryKey;
        switch (locationType) {
            case "Hospital": categoryKey = "hospital"; break;
            case "Police": categoryKey = "police"; break;
            case "Fire": categoryKey = "fire_station"; break;
            default: categoryKey = "custom";
        }

        for (Location location : allLocations) {
            if (location.getCategory().equals(categoryKey)) {
                for (Marker m : markers) {
                    double mLat = m.getPosition().latitude;
                    double mLon = m.getPosition().longitude;
                    double currLat = location.getLatitude();
                    double currLon = location.getLongitude();
                    if (mLat == currLat && mLon == currLon) {
                        m.setVisible(markerVisibility);
                    }
                }
            }
        }
    }

    public void filterButtonTapped(View v) {
        ImageButton b = findViewById(R.id.filter_button);
        if (b.getTag() == Integer.valueOf(0)) {
            b.setBackgroundResource(R.drawable.filter_black_filled_highlighted);
            b.setTag(1);

            final LinearLayout filter_container = findViewById(R.id.filter_container);
            ObjectAnimator anim = ObjectAnimator.ofFloat(filter_container, "x", 1000, 25);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    filter_container.setVisibility(View.VISIBLE);
                }

                @Override public void onAnimationEnd(Animator animator) {}
                @Override public void onAnimationCancel(Animator animator) {}
                @Override public void onAnimationRepeat(Animator animator) {}
            });
            anim.setDuration(400);
            anim.start();
        } else {
            b.setBackgroundResource(R.drawable.filter_black_filled);
            b.setTag(0);

            final LinearLayout filter_container = findViewById(R.id.filter_container);
            ObjectAnimator anim = ObjectAnimator.ofFloat(filter_container, "x", 25, 1000);
            anim.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationEnd(Animator animator) {
                    filter_container.setVisibility(View.GONE);
                }

                @Override public void onAnimationStart(Animator animator) {}
                @Override public void onAnimationCancel(Animator animator) {}
                @Override public void onAnimationRepeat(Animator animator) {}
            });
            anim.setDuration(400);
            anim.start();
        }
    }
}