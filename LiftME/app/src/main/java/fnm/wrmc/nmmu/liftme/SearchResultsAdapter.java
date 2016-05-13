package fnm.wrmc.nmmu.liftme;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;

/**
 * Created by minnaar on 2016/05/13.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<TripViewHolder>
    implements TripViewHolder.TripClickedListener
{
    private List<SearchedTrip> matchingTrips;

    public SearchResultsAdapter(List<SearchedTrip> matchingTrips) {
        this.matchingTrips = matchingTrips;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.searched_trip_holder, parent, false);
        TripViewHolder vh = new TripViewHolder(v, this);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        // change the views based on the position in the matchedTrips list
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return matchingTrips.size();
    }

    @Override
    public void onTextAreaClicked(View caller) {

    }

    @Override
    public void onStarClicked(View caller) {

    }
}
