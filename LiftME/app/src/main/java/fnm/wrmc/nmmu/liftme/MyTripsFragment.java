package fnm.wrmc.nmmu.liftme;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.ServerConnection.PostedUserTripsRunner.UserPostedTripsTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyTripsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public interface IMyTripsCallback{
        void onMyTripClick(Trip clickedTrip);
    }

    private IMyTripsCallback callbackListener;
    private MyTripsListAdapter adapter;
    private ListView myTripsList;
    private List<Trip> trips;
    private Handler tripsHandler;
    private SwipeRefreshLayout myTripsSwipeRefresh;
    private FloatingActionButton addTripFab;
    private LinearLayout emptyView;

    public MyTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View curView = inflater.inflate(R.layout.fragment_my_trips, container, false);

        myTripsList =  (ListView)curView.findViewById(R.id.lVMyTrips);
        myTripsSwipeRefresh = (SwipeRefreshLayout)curView.findViewById(R.id.my_trips_swipe_refresh_layout);
        addTripFab = (FloatingActionButton)curView.findViewById(R.id.fabAddTrip);
        emptyView = (LinearLayout)curView.findViewById(R.id.lLemptyTrip);

        return curView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        trips = new ArrayList<>();
        adapter = new MyTripsListAdapter(getContext(),trips);
        myTripsList.setAdapter(adapter);
        addTripFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTripClick();
            }
        });

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

        if(getActivity() instanceof IMyTripsCallback){
            callbackListener = (IMyTripsCallback)getActivity();
        }

        if(getActivity() instanceof SetToolbarTitleListener) {
            SetToolbarTitleListener toolbarTitleListener = (SetToolbarTitleListener) getActivity();
            toolbarTitleListener.onSetTitle("My Trips");
        }

        myTripsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trip clickedTrip = (Trip) myTripsList.getItemAtPosition(position);
                OnMyTripClick(clickedTrip);
            }
        });

        myTripsList.setEmptyView(emptyView);
        myTripsSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetUserTrips();
            }
        });

        myTripsSwipeRefresh.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary,R.color.colorPrimaryDark);
        myTripsSwipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                myTripsSwipeRefresh.setRefreshing(true);
                GetUserTrips();
            }
        });
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
        myTripsSwipeRefresh.setRefreshing(false);
    }

    private void OnUserTripRetrievalFailure(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            myTripsSwipeRefresh.setRefreshing(false);
        }
    }

    public void OnMyTripClick(Trip trip){
        if(callbackListener != null){
            callbackListener.onMyTripClick(trip);
        }
    }

    private void AddTripClick(){
        Intent intent = new Intent(getContext(), LocationActivity.class);
        intent.putExtra("IS_PICKUP",1);
        startActivity(intent);
    }

}
