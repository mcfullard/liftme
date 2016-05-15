package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;
import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.ServerConnection.SearchTripsRunner;
import fnm.wrmc.nmmu.liftme.ServerConnection.SearchTripsRunner.SearchTripsTask;
import fnm.wrmc.nmmu.liftme.ServerConnection.AddNewTripRunner;
import fnm.wrmc.nmmu.liftme.ServerConnection.AddNewTripRunner.AddNewTripTask;

public class SearchResultsActivity extends AppCompatActivity
    implements MatchedTripViewHolder.TripClickedListener
{

    private static final int SEARCH_TOLERANCE_KM = 2;
    private Map<Integer, Integer> interestedTripIds = new HashMap<>();
    private Trip userTrip;
    private Handler searchResultsHandler;
    private Button btnAddTrip;
    private TextView userTripTitle;
    private TextView instructions;
    private CardView matchedTripsCard;
    private List<SearchedTrip> matchingTrips = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        userTripTitle = (TextView) findViewById(R.id.user_trip_title);
        matchedTripsCard = (CardView) findViewById(R.id.matched_trips_card);
        instructions = (TextView) findViewById(R.id.instructions);
        btnAddTrip = (Button) findViewById(R.id.btnAddTrip);
        mRecyclerView = (RecyclerView) findViewById(R.id.search_results_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SearchResultsAdapter(matchingTrips, this);
        mRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchResultsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case ServerConnection.SEARCH_TRIPS_TASK:
                        SearchTripsTask task = (SearchTripsTask) msg.obj;
                        switch(task.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                matchingTrips.addAll(task.searchedTripResults);
                                mAdapter.notifyDataSetChanged();
                                if(matchingTrips.size() == 0) {
                                    matchedTripsCard.setVisibility(View.INVISIBLE);
                                    instructions.setVisibility(View.INVISIBLE);
                                }
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };

        btnAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add the userTrip to the DB and redirect to dashboard
                addUserTrip();
            }
        });

        handleIntent();
        getMatchingTrips();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                userTrip = (Trip) extras.getSerializable("TRIP");
                if(userTrip != null) {
                    String pickupString = "", dropoffString = "";
                    try {
                        List<String> pickupAddress = LocationActivity.getAddressFromLatLng(this,
                                new LatLng(userTrip.getPickupLat(), userTrip.getPickupLong())
                        );
                        pickupString = pickupAddress.get(0);
                        List<String> dropoffAddress = LocationActivity.getAddressFromLatLng(this,
                                new LatLng(userTrip.getDestinationLat(), userTrip.getDestinationLong())
                        );
                        dropoffString = dropoffAddress.get(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    userTripTitle.setText(String.format("%s to %s", pickupString, dropoffString));
                }
            }
        }
    }

    private void getMatchingTrips() {
        SearchTripsTask matchingTripsTask = new SearchTripsTask(
                getAuthKey(),
                userTrip.getPickupLat(),
                userTrip.getPickupLong(),
                userTrip.getDestinationLat(),
                userTrip.getDestinationLong(),
                SEARCH_TOLERANCE_KM,
                searchResultsHandler
        );
        Thread matchingTripsThread = new Thread(new SearchTripsRunner(matchingTripsTask));
        matchingTripsThread.start();
    }

    private String getAuthKey() {
        SharedPreferences sharedPref = getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");
        if(authKey.isEmpty()){
            Toast.makeText(this, "You never logged in previously. Please login.", Toast.LENGTH_LONG).show();
            return "";
        }
        return authKey;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_results_menu, menu);
        getSupportActionBar().setTitle("Trips");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm_trip_interest:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTextAreaClicked(View caller, int pos) {
        SearchedTrip searchedTrip = matchingTrips.get(pos);
        Intent intent = new Intent(SearchResultsActivity.this, TripDetailsActivity.class);
        intent.putExtra(TripDetailsFragment.ARG_TRIP, searchedTrip);
        startActivity(intent);
    }

    @Override
    public void onStarClicked(View caller, int pos) {
        if(interestedTripIds.containsKey(pos)) {
            interestedTripIds.remove(pos);
        } else {
            interestedTripIds.put(pos, matchingTrips.get(pos).getTripID());
        }
    }

    private void addUserTrip() {
        // handler doesn't do anything at the moment
        AddNewTripTask addNewTripTask = new AddNewTripTask(getAuthKey(), userTrip, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ServerConnection.ADD_NEW_TRIP_TASK:
                        AddNewTripTask task = (AddNewTripTask) msg.obj;
                        switch (task.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                Toast.makeText(SearchResultsActivity.this, "Trip added.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SearchResultsActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                break;
                            case ServerConnection.AUTHENTICATION_FAIL:
                                Toast.makeText(SearchResultsActivity.this, "Failed to add trip to database. Please try again.", Toast.LENGTH_LONG).show();
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });
        Thread newTripThread = new Thread(new AddNewTripRunner(addNewTripTask));
        newTripThread.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("TRIP", userTrip);
        super.onSaveInstanceState(outState);
    }
}
