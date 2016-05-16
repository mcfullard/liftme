package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.Data_Objects.User;

/**
 * Created by Francois on 2016/05/14.
 */
public class MyInterestedTripsFragment extends Fragment{

    public interface IMyinterstedTripsCallback{
        void onMyInterestedTripClick(Trip clickedTrip);
    }

    private IMyinterstedTripsCallback callbackListener;
    private SetToolbarTitleListener toolbarTitleListener;
    private MyTripsListAdapter adapter;
    private ListView myTripsList;
    private List<Trip> trips;
    private Handler tripsHandler;
    private SwipeRefreshLayout myTripsSwipeRefresh;
    private FloatingActionButton addTripFab;

    public MyInterestedTripsFragment() {
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
                    case ServerConnection.GET_USER_INTERESTED_TRIP_TASK:
                        ServerConnection.InterestedUserTripsRunner.UserInterestedTripsTask tripsTask = (ServerConnection.InterestedUserTripsRunner.UserInterestedTripsTask) inputMessage.obj;
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

        if(getActivity() instanceof IMyinterstedTripsCallback){
            callbackListener = (IMyinterstedTripsCallback)getActivity();
        }
        if(getActivity() instanceof SetToolbarTitleListener) {
            toolbarTitleListener = (SetToolbarTitleListener) getActivity();
            toolbarTitleListener.onSetTitle("Favourite Trips");
        }

        myTripsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trip clickedTrip = (Trip) myTripsList.getItemAtPosition(position);
                OnMyInterestedTripClick(clickedTrip);
            }
        });

        myTripsSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetInterestedTrips();
            }
        });

        myTripsSwipeRefresh.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary,R.color.colorPrimaryDark);
        myTripsSwipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                myTripsSwipeRefresh.setRefreshing(true);
                GetInterestedTrips();
            }
        });
    }

    private void GetInterestedTrips(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");

        if(authKey.isEmpty()){
            OnUserTripRetrievalFailure("You never logged in previously. Please login.");
            return;
        }

        ServerConnection.InterestedUserTripsRunner.UserInterestedTripsTask tripsTask = new ServerConnection.InterestedUserTripsRunner.UserInterestedTripsTask(authKey,tripsHandler);
        Thread tripThread = new Thread (new ServerConnection.InterestedUserTripsRunner(tripsTask));
        tripThread.start();
    }

    private void OnUserTripRetrievalSuccess(ServerConnection.InterestedUserTripsRunner.UserInterestedTripsTask tripsTask){
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

    public void OnMyInterestedTripClick(Trip trip){
        if(callbackListener != null){
            callbackListener.onMyInterestedTripClick(trip);
        }
    }

    private void AddTripClick(){
        Intent intent = new Intent(getContext(), LocationActivity.class);
        startActivity(intent);
    }
}
