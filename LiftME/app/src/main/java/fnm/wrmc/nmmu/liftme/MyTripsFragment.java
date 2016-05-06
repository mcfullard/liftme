package fnm.wrmc.nmmu.liftme;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fnm.wrmc.nmmu.liftme.ServerConnection.PostedUserTripsRunner.UserPostedTripsTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyTripsFragment extends Fragment {

    private MyTripsListAdapter adapter;
    private ListView myTripsList;
    private List<Trip> trips;
    private Handler tripsHandler;

    public MyTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View curView = inflater.inflate(R.layout.fragment_my_trips, container, false);

        myTripsList =  (ListView)curView.findViewById(R.id.lVMyTrips);

        return curView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        trips = new ArrayList<>();
        adapter = new MyTripsListAdapter(getContext(),trips);
        myTripsList.setAdapter(adapter);

        tripsHandler = new Handler(){
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case ServerConnection.USER_POSTED_TRIP_TASK:
                        UserPostedTripsTask tripsTask = (UserPostedTripsTask) inputMessage.obj;
                        switch (tripsTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnUserTripRetrievalSuccess(tripsTask);
                                break;
                            case ServerConnection.AUTHENTICATION_FAIL:
                                OnUserTripRetrievalFailure("Authentication error occurred. Please login.");
                                break;
                            case ServerConnection.AUTHENTICATION_INCOMPLETE:
                                OnUserTripRetrievalFailure("Could not successfully connect to server to retrieve your trips. Please check your network connection and try again.");
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(inputMessage);
                        break;
                }
            }
        };

        GetUserTrips();
    }

    private void GetUserTrips(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("GlobalPref",Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");

        if(authKey.isEmpty()){
            OnUserTripRetrievalFailure("You never logged in previously. Please login.");
            return;
        }

        UserPostedTripsTask tripsTask = new UserPostedTripsTask(authKey,tripsHandler);
        Thread tripThread = new Thread (new ServerConnection.PostedUserTripsRunner(tripsTask));
        tripThread.start();
    }

    private void OnUserTripRetrievalSuccess(UserPostedTripsTask tripsTask){
        adapter.clear();
        adapter.addAll(tripsTask.trips);
    }

    private void OnUserTripRetrievalFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
