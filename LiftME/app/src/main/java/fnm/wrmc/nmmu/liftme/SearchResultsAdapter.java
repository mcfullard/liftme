package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;

/**
 * Created by minnaar on 2016/05/13.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<MatchedTripViewHolder>
{
    private Context context;
    private List<SearchedTrip> matchingTrips;

    public SearchResultsAdapter(List<SearchedTrip> matchingTrips, Context context) {
        this.matchingTrips = matchingTrips;
        this.context = context;
    }

    @Override
    public MatchedTripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.matched_trip_holder, parent, false);
        MatchedTripViewHolder.TripClickedListener listener = null;
        try {
            listener = (MatchedTripViewHolder.TripClickedListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        MatchedTripViewHolder vh = new MatchedTripViewHolder(v, listener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MatchedTripViewHolder holder, int position) {
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
        holder.subTitle1.setText(String.format("%.2fkm from pickup", trip.getDistanceBetweenPickups()));
        holder.subTitle2.setText(String.format("%.2fkm from destination", trip.getDistanceBetweenDropOffs()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return matchingTrips.size();
    }
}
