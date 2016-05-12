package fnm.wrmc.nmmu.liftme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fnm.wrmc.nmmu.liftme.Utilities.PermissionUtils;

public class LocationActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener,
        OnRequestPermissionsResultCallback {

    private static int IS_PICKUP = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private Marker locationMarker;
    private ImageView customMapPin;
    private Button setLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Button setLocationButton = (Button) findViewById(R.id.setLocationButton);
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
        ImageView customMapPin = (ImageView) findViewById(R.id.customMapPin);
        customMapPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMarker(mMap.getCameraPosition().target);
                showMarkerAddress();
            }
        });

        Toolbar searchToolbar = (Toolbar) findViewById(R.id.searchToolbar);
        setSupportActionBar(searchToolbar);

        handleIntent();
        updateToolbarTitle();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                IS_PICKUP = extras.getInt("IS_PICKUP");
            }
        }
    }

    private void updateToolbarTitle() {
        ActionBar toolbar = getSupportActionBar();
        switch (IS_PICKUP) {
            case 2:
                toolbar.setTitle("Destination");
                break;
            case 1:
            default:
                toolbar.setTitle("Pickup");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                try {
                    // The autocomplete activity requires Google Play Services to be available. The intent
                    // builder checks this and throws an exception if it is not the case.
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
                        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
                } catch (GooglePlayServicesRepairableException e) {
                    // Indicates that Google Play Services is either not installed or not up to date. Prompt
                    // the user to correct the issue.
                    GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                        0 /* requestCode */).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    // Indicates that Google Play Services is not available and the problem is not easily
                    // resolvable.
                    String message = "Google Play Services is not available: " +
                        GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

                    Log.e("LocationActivity", message);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            switch (resultCode) {
                case RESULT_OK:
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    panAndZoomCam(place.getLatLng());
                    break;
                case PlaceAutocomplete.RESULT_ERROR:
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);
        enableMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
            {
                LatLng locationPos = new LatLng(location.getLatitude(), location.getLongitude());
                panAndZoomCam(locationPos);
            }
        }
    }

    // method to move camera
    private void panAndZoomCam(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(latLng)
            .zoom(17)
            .bearing(0)
            .tilt(30)
            .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }



    public static ArrayList<String> getAddressFromLatLng(Context context, LatLng latLng) throws IOException {
        ArrayList<String> address = new ArrayList<>();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        int maxIndex = addresses.get(0).getMaxAddressLineIndex();
        for(int i = 0; i <= 1; i++) {
            address.add(i, addresses.get(0).getAddressLine(i));
        }
        return address;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        resetMarker(cameraPosition.target);
        showMarkerAddress();
    }

    private void resetMarker(LatLng latLng) {
        if(locationMarker != null) {
            locationMarker.setPosition(latLng);
            locationMarker.hideInfoWindow();
        } else {
            locationMarker = mMap.addMarker(new MarkerOptions().position(latLng));
            locationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.translucent_pixel));
        }
    }

    private void showMarkerAddress() {
        try {
            ArrayList<String> address = getAddressFromLatLng(LocationActivity.this, locationMarker.getPosition());
            locationMarker.setTitle(TextUtils.join(", ", address));
        } catch (IOException e) {
            locationMarker.setTitle("");
        }
        locationMarker.showInfoWindow();
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

}
