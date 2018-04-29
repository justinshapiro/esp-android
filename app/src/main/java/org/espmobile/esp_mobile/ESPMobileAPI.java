package org.espmobile.esp_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;

import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public final class ESPMobileAPI {

    // Route: POST /api/v1/authentication/login
    public static void login(final Context context, String loginID, String password, final ParsedRequestListener<JSONObject> listener) {
        final String endpoint = "https://espmobile.org/api/v1/authentication/login";

        final HashMap<String, String> queryParameters = new HashMap<>();

        queryParameters.put("username", loginID);
        queryParameters.put("password", password);

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(final JSONObject loginResponse) {
                        try {
                            String userID = loginResponse.getJSONObject("ESP-Response").getString("user_id");
                            SharedPreferences.Editor ed = context.getSharedPreferences("Login", MODE_PRIVATE).edit();
                            ed.putString("loggedInUser", userID);
                            ed.apply();

                            getUserInfo(context, new ParsedRequestListener<UserInfo>() {
                                @Override
                                public void onResponse(UserInfo response) {
                                    SharedPreferences.Editor ed = context.getSharedPreferences("nav_menu", MODE_PRIVATE).edit();
                                    ed.putString("name", response.getName());
                                    ed.putString("email", response.getEmail());
                                    ed.apply();

                                    listener.onResponse(loginResponse);
                                }

                                @Override
                                public void onError(ANError anError) {
                                    SharedPreferences.Editor ed = context.getSharedPreferences("nav_menu", MODE_PRIVATE).edit();
                                    ed.putString("name", "Your Profile");
                                    ed.putString("email", "");
                                    ed.apply();

                                    listener.onError(anError);
                                }
                            });
                        } catch (JSONException e) {
                            listener.onError(new ANError());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        listener.onError(anError);
                    }
                });
    }

    // GET /api/v1/authentication/logout
    public static void logout(final Context context, final ParsedRequestListener<JSONObject> listener) {
        String endpoint = "https://espmobile.org/api/v1/authentication/logout";

        AndroidNetworking.get(endpoint).setPriority(Priority.MEDIUM).build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // remove logged in user from cache
                        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
                        SharedPreferences.Editor Ed = sp.edit();
                        Ed.clear();
                        Ed.apply();
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route GET /api/v1/locations
    public static void safetyZones(Context context, Double latitude, Double longitude, Integer radius, final ParsedRequestListener<ArrayList<Location>> listener) {
        String endpoint = "https://espmobile.org/api/v1/locations";
        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);
        String rad = String.valueOf(radius);

        HashMap<String, String> queryParameters = new HashMap<>();

        queryParameters.put("latitude", lat);
        queryParameters.put("longitude", lon);
        queryParameters.put("radius", rad);

        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", null);

        if (userID != null) {
            Log.d("USERID", userID);
            queryParameters.put("user_id", userID);
        }

        AndroidNetworking.get(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(Location.locationsFromJson(response));
                } catch (JSONException e) {
                    onError(new ANError());
                }
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    // Route: GET /api/v1/locations/{id}
    public static void getLocation(String locationID, final ParsedRequestListener<Location> listener) {
        String endpoint = "https://espmobile.org/api/v1/locations/" + locationID;

        AndroidNetworking.get(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(new Location(response.getJSONObject("ESP-Response")));
                } catch (JSONException e) {
                    listener.onError(new ANError());
                }
            }
            @Override
            public void onError(ANError error) {
                listener.onError(error);
            }
        });
    }

    // Route: GET /api/v1/users/{id}
    public static void getUserInfo(Context context, final ParsedRequestListener<UserInfo> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID;

        AndroidNetworking.get(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(new UserInfo(response.getJSONArray("ESP-Response").getJSONObject(0)));
                } catch (JSONException e) {
                    listener.onError(new ANError());
                }
            }

            @Override
            public void onError(ANError error) {
                listener.onError(error);
                Log.d("API", "API Error");
            }
        });
    }

    // Route: POST /api/v1/users
    public static void addUser(UserInfo userInfo, final ParsedRequestListener<UserInfo> listener) {
        String endpoint = "https://espmobile.org/api/v1/users";
        HashMap<String, String> queryParameters = new HashMap<>();

        queryParameters.put("authentication_type", "1");
        queryParameters.put("name", userInfo.getName());
        queryParameters.put("username", userInfo.getUsername());
        queryParameters.put("email", userInfo.getEmail());
        queryParameters.put("password", userInfo.getPassword());

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(new UserInfo(response));
                } catch (JSONException e) {
                    listener.onError(new ANError());
                }
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    // Route: PUT /api/v1/users/{id}/name
    public static void updateUserName(final Context context, final String userName, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        final String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/name";

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("name", userName);

        AndroidNetworking.put(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences.Editor ed = context.getSharedPreferences("nav_menu", MODE_PRIVATE).edit();
                        ed.putString("name", userName);
                        ed.apply();
                        listener.onResponse(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route PUT /api/v1/users/{id}/email
    public static void updateUserEmail(final Context context, final String userEmail, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/email";
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("email", userEmail);

        AndroidNetworking.put(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences.Editor ed = context.getSharedPreferences("nav_menu", MODE_PRIVATE).edit();
                        ed.putString("email", userEmail);
                        ed.apply();

                        listener.onResponse(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: DELETE /api/v1/users/{id}
    public static void deleteUser(Context context, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        final String endpoint = "https://espmobile.org/api/v1/users/" + userID;

        logout(context, new ParsedRequestListener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                AndroidNetworking.delete(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    // Route: GET /api/v1/users/{id}/locations
    public static void getUserLocations(Context context, final ParsedRequestListener<ArrayList<Location>> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/locations";

        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        AndroidNetworking.get(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(Location.locationsFromJson(response));
                } catch (JSONException e){
                    listener.onError(new ANError());
                }
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    // Route: POST /api/v1/users/{id}/locations
    public static void addCustomLocation(Context context, Location location, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/locations";

        /*  Since our value type is boxed as an object, we will have to later unbox it with
            the correct wrapper type ie Double lat = (Double) queryParameters.get("latitude") */
        HashMap<String, String> queryParameters = new HashMap<>();

        queryParameters.put("name", location.getName());
        queryParameters.put("latitude", Double.toString(location.getLatitude()));
        queryParameters.put("longitude", Double.toString(location.getLongitude()));
        queryParameters.put("address", location.getAddress());
        queryParameters.put("phone_number", location.getPhoneNumber());
        queryParameters.put("description", location.getDescription());

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: DELETE /api/v1/user/{id}/locations/{id}
    public static void deleteCustomLocation(Context context, String locationID, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/locations/" + locationID;

        AndroidNetworking.delete(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: GET /api/v1/user/{id}/contacts
    public static void getContacts(Context context, final ParsedRequestListener<ArrayList<Contact>> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/contacts";

        AndroidNetworking.get(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(Contact.contactsFromJson(response));
                } catch (JSONException e) {
                    listener.onError(new ANError());
                }
            }
            @Override
            public void onError(ANError error) {
                listener.onError(error);
            }
        });
    }

    // Route: POST /api/v1/user/{id}/contacts
    public static void addContact(Context context, Contact contact, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/contacts";
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("name", contact.getName());
        queryParameters.put("phone", contact.getPhone());

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: PUT /api/v1/user/{id}/contact/{id}/phone
    public static void updateContactPhone(Context context, String contactID, String contactPhone, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/contacts/" + contactID + "/phone";
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("phone_number", contactPhone);

        AndroidNetworking.put(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: DELETE /api/v1/user/{id}/contacts/{id}
    public static void deleteContact(Context context, String contactID, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/contacts/" + contactID;

        AndroidNetworking.delete(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                listener.onResponse(response);
            }
            @Override
            public void onError(ANError error) {
                listener.onError(error);
                Log.d("API", "API Error");
            }
        });
    }

    // ROUTE: POST /api/v1/user/{id}/contacts/group
    public static void addContactGroup(Context context, ArrayList<Contact> contactGroup, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/contacts/group";

        StringBuilder contactIDs = new StringBuilder("[");
        for (int i = 0; i < contactGroup.size(); i++) {
            if (i + 1 != contactGroup.size()) {
                contactIDs.append("\"").append(contactGroup.get(i).getID()).append("\", ");
            } else {
                contactIDs.append("\"").append(contactGroup.get(i).getID()).append("\"]");
            }
        }

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("contact_ids", contactIDs.toString());

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: DELETE /api/v1/user/{id}/contacts/group
    public static void deleteContactGroup(Context context, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/contacts/group";

        AndroidNetworking.delete(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    listener.onResponse(response);
                }
                @Override
                public void onError(ANError error) {
                    listener.onError(error);
                    Log.d("API", "API Error");
                }
        });
    }

    // Route: GET /api/v1/users/{id}/alert
    public static void getAlerts(Context context, final ParsedRequestListener<ArrayList<Location>> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/alert";

        AndroidNetworking.get(endpoint).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(Location.locationsFromJson(response));
                } catch (JSONException e) {
                    listener.onError(new ANError());
                }
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    // Route: POST /api/v1/users/{id}/alert
    public static void addAlert(Context context, String locationID, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/alert";
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("location_id", locationID);
        queryParameters.put("alertable", "false");

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: DELETE /api/v1/users/{id}/alert/{id}
    public static void deleteAlert(Context context, String locationID, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/alert";
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("location_id", locationID);

        AndroidNetworking.delete(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: PUT /api/v1/users/{id}/password
    public static void updatePassword(Context context, String newPassword, final ParsedRequestListener<JSONObject> listener) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        HashMap<String, String> queryParameters = new HashMap<>();
        String endpoint = "https://espmobile.org/api/v1/users/" + userID + "/password";

        queryParameters.put("new_password", newPassword);

        AndroidNetworking.put(endpoint)
                .addQueryParameter(queryParameters).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }

    // Route: POST /api/v1/notification
    static void sendNotification(String locationID, String category, Context context) {
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String userID = sp.getString("loggedInUser", "");
        String endpoint = "https://espmobile.org/api/v1/notification";
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("user_id", userID);
        queryParameters.put("location_id", locationID);
        if (category.toLowerCase().equals("custom")) {
            category = "custom";
        } else {
            category = "google";
        }
        queryParameters.put("location_type", category);

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override public void onResponse(JSONObject response) {}
                    @Override public void onError(ANError error) { Log.d("API", "API Error"); }
                });
    }

    // ROUTE: POST /api/v1/feedback
    public static void sendFeedback(Context c, Feedback feedback, final ParsedRequestListener<JSONObject> listener) {
        String endpoint = "https://espmobile.org/api/v1/feedback";
        HashMap<String, String> queryParameters = feedback.tabularRepresentation(true);

        AndroidNetworking.post(endpoint)
                .addQueryParameter(queryParameters)
                .setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        listener.onError(error);
                        Log.d("API", "API Error");
                    }
                });
    }
}
