package fnm.wrmc.nmmu.liftme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;

public class TripDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Trip Details");
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchedTrip searchedTrip = null;
        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                searchedTrip = (SearchedTrip) extras.getSerializable(TripDetailsFragment.ARG_TRIP);
            }
        }

        Bundle fragBundle = new Bundle();
        if(searchedTrip != null)
            fragBundle.putSerializable(TripDetailsFragment.ARG_TRIP, searchedTrip);
        ViewTripDetails detailsFrag = new ViewTripDetails();
        detailsFrag.setArguments(fragBundle);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.trip_details_container, detailsFrag)
                .commit();
    }

}
