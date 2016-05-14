package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;

/**
 * Created by minnaar on 2016/05/13.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<TripViewHolder>
{
    private Context context;
    private List<SearchedTrip> matchingTrips;

    public SearchResultsAdapter(List<SearchedTrip> matchingTrips, Context context) {
        this.matchingTrips = matchingTrips;
        this.context = context;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.searched_trip_holder, parent, false);
        TripViewHolder.TripClickedListener listener = null;
        try {
            listener = (TripViewHolder.TripClickedListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        TripViewHolder vh = new TripViewHolder(v, listener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        // change the views based on the position in the matchedTrips list
        SearchedTrip trip = matchingTrips.get(position);
        String pickupAddress = "", dropoffAddress = "";
        try {
            pickupAddress = TextUtils.join(", ", LocationActivity.getAddressFromLatLng(
                    holder.textArea.getContext(),
                    new LatLng(trip.getPickupLat(), trip.getPickupLong())
            ));
            dropoffAddress = TextUtils.join(", ", LocationActivity.getAddressFromLatLng(
                    holder.textArea.getContext(),
                    new LatLng(trip.getDestinationLat(), trip.getDestinationLong())
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.title.setText(String.format("%s to %s", pickupAddress, dropoffAddress));
        holder.subTitle1.setText(String.format("%fm from your pickup", trip.getDistanceBetweenPickups()));
        holder.subTitle2.setText(String.format("%fm from your destination", trip.getDistanceBetweenDropOffs()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return matchingTrips.size();
    }
}
