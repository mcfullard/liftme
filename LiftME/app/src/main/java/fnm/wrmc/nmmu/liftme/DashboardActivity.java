package fnm.wrmc.nmmu.liftme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Serializable;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;

public class DashboardActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MyTripsFragment.IMyTripsCallback,
        UserProfileFragment.AcceptChangeClickListener {

    static public String USER_PROFILE_FRAGMENT = "fnm.wrmc.nmmu.liftme.UserProfileFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment mainFragment = getFragmentFromContext("",null);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                String fragment_context = extras.getString("fragment_context");
                mainFragment = getFragmentFromContext(fragment_context, null);
            }
        }

        AddFragmentToContainer(R.id.container,mainFragment);

    }

    private void AddFragmentToContainer(int container,Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(fm.findFragmentById(container) != null) {
            transaction.replace(container, fragment);
            transaction.addToBackStack("");
        } else {
            transaction.add(container, fragment);
        }
        transaction.commit();
    }

    private Fragment getFragmentFromContext(String fragment_context,Bundle fragmentArguments) {
        Fragment newFragment;
        switch (fragment_context) {
            case "fnm.wrmc.nmmu.liftme.UserProfileFragment":
                newFragment = new UserProfileFragment();
                break;
            case "fnm.wrmc.nmmu.liftme.MyTripDetailsFragment":
                newFragment = new MyTripDetailsFragment();
                break;
            case "fnm.wrmc.nmmu.liftme.MyTripsFragment":
            default:
                newFragment = new MyTripsFragment();
        }

        if(fragmentArguments != null){
            newFragment.setArguments(fragmentArguments);
        }

        return newFragment;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMyTripClick(Trip clickedTrip) {
        Bundle B = new Bundle();
        B.putSerializable(TripDetailsFragment.ARG_TRIP, ((Serializable) clickedTrip));
        Fragment newFragment = getFragmentFromContext(MyTripDetailsFragment.FRAG_IDENTIFYER,B);
        AddFragmentToContainer(R.id.container,newFragment);
    }

    @Override
    public void onAcceptChangeClick() {
        AddFragmentToContainer(R.id.container, getFragmentFromContext("fnm.wrmc.nmmu.liftme.MyTripsFragment", null));
    }
}
