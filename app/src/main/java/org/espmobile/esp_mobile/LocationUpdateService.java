package org.espmobile.esp_mobile;

import android.Manifest;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LocationUpdateService extends JobService {
    private static final String TAG = LocationUpdateService.class.getSimpleName();
    private JobParameters params;
    final private int jobSecondsInterval = 900;
    final private int fenceRadiusMiles = 20;
    final private int geofenceRadiusMeters = 100;
    final private int loiteringDelayMillisecond = 60000;
    public static HashMap<String, String> geofenceCategories = null;
    public static String lastActivatedGeofence = null;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
        new GeofenceTask().execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void scheduleRefresh() {
        JobScheduler mjobScheduler =
                (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        JobInfo mJobInfo = new JobInfo.Builder(1,
                new ComponentName(getPackageName(), LocationUpdateService.class.getName()))
                .setMinimumLatency(jobSecondsInterval * 1000)
                .build();
        mjobScheduler.schedule(mJobInfo);
    }


    private class GeofenceTask extends AsyncTask<Void, Void, Void> {
        private GeofencingClient mGeoFencingClient;
        private ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();
        private PendingIntent mGeofencePendingIntent = null;
        private FusedLocationProviderClient mFusedLocationClient;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            scheduleRefresh();
            jobFinished(params, false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (!getApplicationContext().getSharedPreferences("login", MODE_PRIVATE).getBoolean("global_disable_alerts", false)) {
                mGeoFencingClient = LocationServices.getGeofencingClient(getApplicationContext());
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                if (!checkPermission()) return null;
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                            @Override
                            public void onSuccess(android.location.Location location) {
                                if (location != null) {
                                    Log.d(TAG, location.toString());
                                    getLocationsAndSetGeofences(location.getLatitude(), location.getLongitude());
                                }
                            }
                        });
            }

            return null;
        }

        private void getLocationsAndSetGeofences(Double lat, Double lon) {
            if (geofenceCategories != null) removeOldGeofences();

            ESPMobileAPI.safetyZones(getApplicationContext(), lat, lon, fenceRadiusMiles * 1609,
                    new ParsedRequestListener<ArrayList<Location>>() {
                        @Override
                        public void onResponse(ArrayList<Location> locations) {
                            //ESPMobileAPI.logResponse(JSONResponse);
                            setGeoFences(locations);
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                            Log.d("API", error.toString());
                        }
                    });
        }

        private void removeOldGeofences() {
            Set<String> oldFences = geofenceCategories.keySet();
            if (lastActivatedGeofence != null) {
                oldFences.remove(lastActivatedGeofence);
            }
            List<String> fenceList = new ArrayList<>(oldFences);
            mGeoFencingClient.removeGeofences(fenceList);
        }

        private void buildGeoFenceList(ArrayList<Location> locations) {
            int locCounter = 0;
            geofenceCategories = new HashMap<String, String>();
            for (Location location : locations) {
                geofenceCategories.put(location.getLocationID(), location.getCategory());
                if (!location.getLocationID().equals(lastActivatedGeofence)) {
                    mGeofenceList.add(new Geofence.Builder()
                            .setRequestId(location.getLocationID())
                            .setCircularRegion(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    geofenceRadiusMeters
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                            .setLoiteringDelay(loiteringDelayMillisecond)
                            .build());
                    locCounter++;
                    if (locCounter >= 50) break;
                }
            }
        }

        private GeofencingRequest getGeofencingRequest(ArrayList<Location> locations) {
            buildGeoFenceList(locations);
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
            builder.addGeofences(mGeofenceList);
            return builder.build();
        }

        private PendingIntent getGeofencePendingIntent() {
            // Reuse the PendingIntent if we already have it.
            if (mGeofencePendingIntent != null) {
                return mGeofencePendingIntent;
            }
            Intent intent = new Intent(getApplicationContext(), GeofenceTransitionsIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
            // calling addGeofences() and removeGeofences().
            mGeofencePendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.
                    FLAG_UPDATE_CURRENT);
            return mGeofencePendingIntent;
        }

        private void setGeoFences(ArrayList<Location> locations) {
            if (!checkPermission()) return;
            mGeoFencingClient.addGeofences(getGeofencingRequest(locations), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });
        }

        private boolean checkPermission() {
            Log.d(TAG, "checkPermission()");
            // Ask for permission if it wasn't granted yet
            return (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED );
        }
    }
}
