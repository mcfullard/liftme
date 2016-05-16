package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Serializable;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;
import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;

public class DashboardActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MyTripsFragment.IMyTripsCallback,
        MyTripDetailsFragment.IMyTripsDetailsCallback,
        UserProfileFragment.AcceptChangeClickListener,
        MyInterestedTripsFragment.IMyinterstedTripsCallback{

    static public String USER_PROFILE_FRAGMENT = "fnm.wrmc.nmmu.liftme.UserProfileFragment";
    private int currentMenuResource = R.menu.dashboard;
    private Fragment activeFragment;

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

        Fragment mainFragment = getFragmentFromContext("", null);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String fragment_context = extras.getString("fragment_context");
                mainFragment = getFragmentFromContext(fragment_context, null);
            }
        }

        AddFragmentToContainer(R.id.container, mainFragment);

    }

    private void AddFragmentToContainer(int container, Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (fm.findFragmentById(container) != null) {
            transaction.replace(container, fragment);
            transaction.addToBackStack("");
        } else {
            transaction.add(container, fragment);
        }
        transaction.commit();
    }

    private Fragment getFragmentFromContext(String fragment_context, Bundle fragmentArguments) {
        Fragment newFragment;
        switch (fragment_context) {
            case "fnm.wrmc.nmmu.liftme.UserProfileFragment":
                newFragment = new UserProfileFragment();
                break;
            case "fnm.wrmc.nmmu.liftme.MyTripDetailsFragment":
                newFragment = new MyTripDetailsFragment();
                break;
            case "fnm.wrmc.nmmu.liftme.MyInterestedTripsFragment":
                newFragment = new MyInterestedTripsFragment();
                break;
            case "fnm.wrmc.nmmu.liftme.ViewTripDetails":
                newFragment = new ViewTripDetails();
                break;
            case "fnm.wrmc.nmmu.liftme.MyTripsFragment":
            default:
                newFragment = new MyTripsFragment();
        }

        if (fragmentArguments != null) {
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
        getMenuInflater().inflate(currentMenuResource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (currentMenuResource) {  //Allows for the action menu to change depending on current context
            case R.menu.dashboard:
                if (id == R.id.action_logout) {
                    logout(DashboardActivity.this);
                    return true;
                }
            case R.menu.my_trip_delete_menu:
                if(id == R.id.action_delete_trip){
                    if(activeFragment instanceof MyTripDetailsFragment) {
                        onTripDeleteActionClick();
                    }
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private void onTripDeleteActionClick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this trip?");

        String positiveText = "Delete";
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(activeFragment instanceof MyTripDetailsFragment){
                            ((MyTripDetailsFragment)activeFragment).DeleteTrip();
                        }
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
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
        Fragment newFragment = getFragmentFromContext(MyTripDetailsFragment.FRAG_IDENTIFIER, B);
        AddFragmentToContainer(R.id.container, newFragment);
    }

    @Override
    public void onAcceptChangeClick() {
        AddFragmentToContainer(R.id.container, getFragmentFromContext("fnm.wrmc.nmmu.liftme.MyTripsFragment", null));
    }

    @Override
    public void onMyTripDetailsAttach() {
        currentMenuResource = R.menu.my_trip_delete_menu;
        invalidateOptionsMenu();
    }

    @Override
    public void onMyTripDetailsDetach() {
        currentMenuResource = R.menu.dashboard;
        invalidateOptionsMenu();
    }

    @Override
    public void onMyTripDetailsDelete() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        activeFragment = fragment;
        super.onAttachFragment(fragment);
    }


    @Override
    public void onMyInterestedTripClick(Trip clickedTrip) {
        Bundle B = new Bundle();
        B.putSerializable(TripDetailsFragment.ARG_TRIP, ((Serializable) new SearchedTrip(clickedTrip,0,0)));
        Fragment newFragment = getFragmentFromContext(ViewTripDetails.FRAG_IDENTIFYER, B);
        AddFragmentToContainer(R.id.container, newFragment);
    }

    public static void logout(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        sharedPref.edit().remove(ServerConnection.AUTHENTICATION_TOKEN).commit();
        Intent intent = new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
