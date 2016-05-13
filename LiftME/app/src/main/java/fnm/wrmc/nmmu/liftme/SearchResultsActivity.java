package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;
import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.ServerConnection.SearchTripsRunner;
import fnm.wrmc.nmmu.liftme.ServerConnection.SearchTripsRunner.SearchTripsTask;

public class SearchResultsActivity extends AppCompatActivity {

    private static final int SEARCH_TOLERANCE_KM = 2;

    private Trip userTrip;
    private Handler searchResultsHandler;
    private List<SearchedTrip> matchingTrips;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        mRecyclerView = (RecyclerView) findViewById(R.id.search_results_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SearchResultsAdapter(matchingTrips);
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
                                matchingTrips = task.searchedTripResults;
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };

        handleIntent();
        getMatchingTrips();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                userTrip = (Trip) extras.getSerializable("TRIP");
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

}
