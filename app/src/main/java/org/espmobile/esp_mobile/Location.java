package org.espmobile.esp_mobile;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

@JsonIgnoreProperties({"user_table_id", "alertable"})
public class Location {
    private double latitude;
    private double longitude;
    private String name;
    private String address;
    private String locationID;
    private String phoneNumber;
    private String category;
    private String photoRef;
    private String alertable;
    private String description;

    public Location() {
        this.latitude = 0;
        this.longitude = 0;
        this.name = "";
        this.address = "";
        this.locationID = "";
        this.phoneNumber = "";
        this.category = "";
        this.photoRef = "";
        this.alertable = "";
        this.description = "";
    }

    public Location(double latitude,
             double longitude,
             String name,
             String address,
             String locationID,
             String phoneNumber,
             String category,
             String photoRef,
             String alertable,
             String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
        this.locationID = locationID;
        this.phoneNumber = phoneNumber;
        this.category = category;
        this.photoRef = photoRef;
        this.alertable = alertable;
        this.description = description;
    }

    Location(JSONObject json) throws JSONException {
        JSONObject geometry, properties;
        try {
            // Safety Zones endpoint format
            geometry = json.getJSONObject("geometry");
            properties = json.getJSONObject("properties");
            this.latitude = geometry.getJSONArray("coordinates").getDouble(0);
            this.longitude = geometry.getJSONArray("coordinates").getDouble(1);
            this.name = properties.getString("name");
            this.address = properties.getString("address");
            this.locationID = properties.getString("location_id");
            this.category = properties.getString("category");
            this.alertable = null;

            try {
                this.photoRef = properties.getString("photo_ref");
            } catch (JSONException e) {
                this.photoRef = null;
            }

            try {
                this.phoneNumber = properties.getString("phone_number");
            } catch (JSONException e) {
                this.phoneNumber = null;
            }
        } catch (JSONException e) {
            // Alerts endpoint format
            this.latitude = json.getDouble("latitude");
            this.longitude = json.getDouble("longitude");
            this.name = json.getString("name");
            this.address = null;
            this.locationID = json.getString("location_id");
            this.photoRef = null;
            this.category = null;
        }

        // currently we're not utilizing this
        this.description = null;
    }

    static ArrayList<Location> locationsFromJson(JSONObject json) throws JSONException {
        JSONArray jsonLocations;

        try {
            jsonLocations = json.getJSONObject("ESP-Response").getJSONObject("GeoJson").getJSONArray("features");
        } catch (JSONException e) {
            JSONArray jsonLocationsArray = json.getJSONArray("ESP-Response");
            if (jsonLocationsArray.length() > 0) {
                jsonLocations = json.getJSONArray("ESP-Response").getJSONObject(0).getJSONArray("locations");
            } else {
                jsonLocations = jsonLocationsArray;
            }
        }

        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < jsonLocations.length(); i++) {
            locations.add(new Location(jsonLocations.getJSONObject(i)));
        }

        return locations;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @JsonGetter("location_id")
    public String getLocationID() {
        return locationID;
    }

    @JsonSetter("location_id")
    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhotoRef() {
        return photoRef;
    }

    public void setPhotoRef(String photoRef) {
        this.photoRef = photoRef;
    }

    public String getAlertable() {
        return alertable;
    }

    public void setAlertable(String alertable) {
        this.alertable = alertable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static ArrayList<HashMap<String, String>> tabularRepresentation(ArrayList<Location> locations) {
        ArrayList<HashMap<String, String>> representation = new ArrayList<>();

        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location lhs, Location rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        for (Location location : locations) {
            HashMap<String, String> currentRepresentation = new HashMap<>();
            currentRepresentation.put("id", location.getLocationID());
            currentRepresentation.put("name", location.getName());
            currentRepresentation.put("latitude", Double.toString(location.getLatitude()));
            currentRepresentation.put("longitude", Double.toString(location.getLongitude()));
            representation.add(currentRepresentation);
        }

        return representation;
    }

    public static String getCallable(String phoneNumber) {
        return phoneNumber
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll(" ", "")
                .replaceAll("-", "");
    }

    public static String limitDigits(String geoLocation, int limit) {
        String[] floatComponents = geoLocation.split("\\.");

        if (floatComponents.length > 1) {
            char[] floatingDigits = floatComponents[1].toCharArray();
            StringBuilder limitedGeoLocation = new StringBuilder(floatComponents[0] + ".");
            for (int i = 0; i < floatingDigits.length; i++) {
                if (i < limit) {
                    limitedGeoLocation.append(Character.toString(floatingDigits[i]));
                }
            }

            return limitedGeoLocation.toString();
        } else {
            return geoLocation;
        }
    }

    public static String capitalize(String word) {
        char[] wordEnumerated = word.toCharArray();
        StringBuilder capitalizedWord = new StringBuilder();
        Boolean shouldCapitalize = true;

        for (char letter : wordEnumerated) {
            if (shouldCapitalize) {
                capitalizedWord.append(Character.toUpperCase(letter));
                shouldCapitalize = false;
            } else if (letter == '_') {
                shouldCapitalize = true;
                capitalizedWord.append(" ");
            } else {
                capitalizedWord.append(letter);
            }
        }

        return capitalizedWord.toString();
    }
}
