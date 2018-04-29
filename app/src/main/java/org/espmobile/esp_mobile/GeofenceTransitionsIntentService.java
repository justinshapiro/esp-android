package org.espmobile.esp_mobile;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // Get the location ID from geofence id
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        // First geofence is the one that was triggered first, most likely correct
        // This isn't guaranteed though, not sure how to fix it at this point
        String fenceID = triggeringGeofences.get(0).getRequestId();
        Log.d(TAG, fenceID);
        String category = LocationUpdateService.geofenceCategories.get(fenceID);
        Log.d(TAG, category);
        LocationUpdateService.lastActivatedGeofence = fenceID;
        ESPMobileAPI.sendNotification(fenceID, category, getApplicationContext());
    }
}
