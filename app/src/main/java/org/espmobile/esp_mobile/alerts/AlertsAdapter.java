package org.espmobile.esp_mobile.alerts;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Location;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.locations.LocationsAdapter;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;
import org.json.JSONObject;

import java.util.List;

public class AlertsAdapter extends RecyclerSwipeAdapter<AlertsAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView coordTextView;
        Button enableButton;
        SwipeLayout swipeLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.alert_name);
            coordTextView = (TextView) itemView.findViewById(R.id.alert_coords);
            enableButton = itemView.findViewById(R.id.enable_alert_button);
            swipeLayout = itemView.findViewById(R.id.item_alerts);
        }
    }

    // Store a member variable for the contacts
    private List<Location> mLocations;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public AlertsAdapter(Context context, List<Location> locations) {
        mLocations = locations;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public AlertsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View locationView = inflater.inflate(R.layout.item_alerts, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(locationView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final AlertsAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        Location location = mLocations.get(position);

        // Set item views based on your views and data model
        TextView nameTextView = viewHolder.nameTextView;
        nameTextView.setText(location.getName());
        TextView coordTextView = viewHolder.coordTextView;

        String limitedLat = Location.limitDigits(Double.toString(location.getLatitude()), 5);
        String limitedLng = Location.limitDigits(Double.toString(location.getLongitude()), 5);
        String coordText = "Geolocation: " + limitedLat + ", " + limitedLng;
        coordTextView.setText(coordText);

        // Handle the enable button click
        viewHolder.enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locID = mLocations.get(position).getLocationID();
                ESPMobileAPI.deleteAlert(getContext(), locID, new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                        mLocations.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mLocations.size());
                    }

                    @Override
                    public void onError(ANError anError) {
                        MessageBanner.show((Activity) getContext(), R.string.error_message, R.color.espRed);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.item_alerts;
    }
}
