package fnm.wrmc.nmmu.liftme;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyTripsFragment extends Fragment {


    private MyTripsListAdapter adapter;
    private ListView myTripsList;

    public MyTripsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View curView = inflater.inflate(R.layout.my_trips_frag, container, false);

        myTripsList =  (ListView)curView.findViewById(R.id.lVMyTrips);

        return curView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Trip newTrip = new Trip();

        //TODO get user trips here
        List<Trip> trips = new ArrayList<>();
        trips.add(newTrip);
        trips.add(newTrip);
        trips.add(newTrip);
        trips.add(newTrip);

        adapter = new MyTripsListAdapter(getContext(),trips);

        myTripsList.setAdapter(adapter);
    }


}
