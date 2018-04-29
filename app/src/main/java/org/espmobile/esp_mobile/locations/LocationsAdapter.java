package org.espmobile.esp_mobile.locations;

import android.content.Context;
import android.support.annotation.NonNull;
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
import org.espmobile.esp_mobile.R;
import org.json.JSONObject;

import java.util.List;

public class LocationsAdapter extends RecyclerSwipeAdapter<LocationsAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView nameTextView;
        TextView coordTextView;
        Button deleteButton;
        SwipeLayout swipeLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.location_name);
            coordTextView = itemView.findViewById(R.id.location_coords);
            deleteButton = itemView.findViewById(R.id.delete_location_button);
            swipeLayout = itemView.findViewById(R.id.item_contact);
        }
    }

    // Store a member variable for the contacts
    private List<Location> mLocations;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    LocationsAdapter(Context context, List<Location> locations) {
        mLocations = locations;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public LocationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View locationView = inflater.inflate(R.layout.item_location, parent, false);

        // Return a new holder instance
        return new ViewHolder(locationView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull final LocationsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        final int realPosition = position;

        // Get the data model based on position
        final Location location = mLocations.get(realPosition);

        // Set item views based on your views and data model
        TextView nameTextView = viewHolder.nameTextView;
        nameTextView.setText(location.getName());
        TextView coordTextView = viewHolder.coordTextView;

        String limitedLatitude = Location.limitDigits(Double.toString(location.getLatitude()), 5);
        String limitedLongitude = Location.limitDigits(Double.toString(location.getLongitude()), 5);
        String geoLocationString = "Geolocation: " + limitedLatitude + ", " + limitedLongitude;
        coordTextView.setText(geoLocationString);

        // Handle the delete button click
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CustomLocationsActivity) mContext).findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                ESPMobileAPI.deleteCustomLocation(getContext(), location.getLocationID(), new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ((CustomLocationsActivity) getContext()).findViewById(R.id.progressBar).setVisibility(View.GONE);
                        mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                        mLocations.remove(realPosition);
                        notifyItemRemoved(realPosition);
                        notifyItemRangeChanged(realPosition, mLocations.size());
                    }

                    @Override
                    public void onError(ANError anError) {
                        ((CustomLocationsActivity) getContext()).findViewById(R.id.progressBar).setVisibility(View.GONE);
                        ((CustomLocationsActivity) getContext()).showError();
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
        return R.id.item_location;
    }
}
