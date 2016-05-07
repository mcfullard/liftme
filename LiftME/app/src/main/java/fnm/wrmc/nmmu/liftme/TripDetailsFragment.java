package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripDetailsFragment extends Fragment {

    public static final String ARG_TRIP = "Selected_Trip";
    public static final String ARG_TRIP_TYPE = "Trip_Type";
    public static final String FRAG_IDENTIFYER = "fnm.wrmc.nmmu.liftme.TripDetailsFragment";
    public static final String MY_TRIP_DETAILS = "My Trip Details";
    public static final String VIEW_TRIP_DETAILS = "View Trip Details";

    private ImageView detailImage;
    private TextView tVPUDetails, tVDesDetails;
    private Trip trip;
    private Handler handler;
    private InterestedUsersListAdapter adapter;
    private ListView lVinterestedUser;
    private FloatingActionButton fabInterestedToggle;

    public TripDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View curView;

        Bundle curBundle = getArguments();

        if (curBundle != null) {
            trip = (Trip) curBundle.get(ARG_TRIP);
            String tripType = curBundle.getString(ARG_TRIP_TYPE);
            if(tripType != null && tripType.equals(MY_TRIP_DETAILS)){
                curView = setupFragForMyTrip(inflater, container);
                return curView;
            } else if(tripType != null && tripType.equals(VIEW_TRIP_DETAILS)){
                curView = setupFragForViewTrip(inflater,container);
                return curView;
            }
            else{
                curView = inflater.inflate(R.layout.fragment_trip_details, container, false);
                return curView;
            }

        }else{
            curView = inflater.inflate(R.layout.fragment_trip_details, container, false);
            return curView;
        }
    }

    public View setupFragForMyTrip(LayoutInflater inflater,ViewGroup container){
        View curView = inflater.inflate(R.layout.fragment_trip_details_with_interested_users, container, false);
        detailImage = (ImageView) curView.findViewById(R.id.iVMyTripDetailsImage);
        tVPUDetails = (TextView) curView.findViewById(R.id.tVPickupDescription);
        tVDesDetails = (TextView) curView.findViewById(R.id.tVDestinationDescription);


        adapter = new InterestedUsersListAdapter(getContext(), (new ArrayList<User>()));
        lVinterestedUser = (ListView) curView.findViewById(R.id.lvTripDetailsInterestedUsers);
        lVinterestedUser.setAdapter(adapter);


        handler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case ServerConnection.GET_ADDRESS_TASK:
                        ServerConnection.GetAddressFromLatLongRunner.GetAddressTask AddrTask = (ServerConnection.GetAddressFromLatLongRunner.GetAddressTask) inputMessage.obj;
                        switch (AddrTask.Status) {
                            case ServerConnection.STATUS_SUCCESS:
                                OnAddressRetrieveSuccess(AddrTask);
                                break;
                            case ServerConnection.STATUS_FAILED:
                                OnRetrieveFailure("Unable to retrieve address at this time.");
                                break;
                        }
                        break;
                    case ServerConnection.GET_INTERESTED_USER_TASK:
                        ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask getIUTask = (ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask) inputMessage.obj;
                        switch (getIUTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnInterestedUsersSuccess(getIUTask);
                                break;
                            case ServerConnection.AUTHENTICATION_FAIL:
                                OnRetrieveFailure("Authentication error occurred. Please login.");
                                break;
                            case ServerConnection.AUTHENTICATION_INCOMPLETE:
                                OnRetrieveFailure("Could not successfully connect to server to retrieve interested users. Please check your network connection and try again.");
                                break;
                        }
                    default:
                        super.handleMessage(inputMessage);
                        break;

                }
            }
        };

        RetrieveAddressFromLatLong();
        RetrieveInterestedUsers();
        GenerateImage();

        return curView;
    }

    public View setupFragForViewTrip(LayoutInflater inflater,ViewGroup container){
        View curView = inflater.inflate(R.layout.fragment_trip_details, container, false);
        detailImage = (ImageView) curView.findViewById(R.id.iVMyTripDetailsImage);
        tVPUDetails = (TextView) curView.findViewById(R.id.tVPickupDescription);
        tVDesDetails = (TextView) curView.findViewById(R.id.tVDestinationDescription);

        fabInterestedToggle = (FloatingActionButton)curView.findViewById(R.id.fab);
        fabInterestedToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnInterestedUserToggle();
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case ServerConnection.GET_ADDRESS_TASK:
                        ServerConnection.GetAddressFromLatLongRunner.GetAddressTask AddrTask = (ServerConnection.GetAddressFromLatLongRunner.GetAddressTask) inputMessage.obj;
                        switch (AddrTask.Status) {
                            case ServerConnection.STATUS_SUCCESS:
                                OnAddressRetrieveSuccess(AddrTask);
                                break;
                            case ServerConnection.STATUS_FAILED:
                                OnRetrieveFailure("Unable to retrieve address at this time.");
                                break;
                        }
                        break;
                    case ServerConnection.TOGGLE_INTERESTED_USER_TASK:
                        ServerConnection.ToggleInterestedUserRunner.ToggleInterestedUserTask tglTask = (ServerConnection.ToggleInterestedUserRunner.ToggleInterestedUserTask) inputMessage.obj;
                        switch (tglTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnInterestedUserToggleSuccess(tglTask);
                                break;
                            case ServerConnection.AUTHENTICATION_FAIL:
                                OnRetrieveFailure("Unable to authenticate you. Are you logged in?");
                                break;
                            case ServerConnection.AUTHENTICATION_INCOMPLETE:
                                OnRetrieveFailure("Could not connect to server. Please check internet connection and try again.");
                                break;
                        }
                    default:
                        super.handleMessage(inputMessage);
                        break;

                }
            }
        };

        RetrieveAddressFromLatLong();
        GenerateImage();

        return curView;

    }

    private void OnAddressRetrieveSuccess(ServerConnection.GetAddressFromLatLongRunner.GetAddressTask addressTask) {
        List<String> addressList = addressTask.Addresses;
        String puDetails = addressList.get(0).replace(",", "\n");
        String desDetails = addressList.get(1).replace(",", "\n");
        tVPUDetails.setText(puDetails);
        tVDesDetails.setText(desDetails);
    }

    private void OnInterestedUsersSuccess(ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask IUTask){
        adapter.clear();
        adapter.addAll(IUTask.interestedUsers);
    }

    private void OnInterestedUserToggleSuccess(ServerConnection.ToggleInterestedUserRunner.ToggleInterestedUserTask tglTask){
        if(tglTask.toggleStatus == 2) {
            fabInterestedToggle.setBackgroundColor(Color.GRAY);
        }else{
            fabInterestedToggle.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private void OnInterestedUserToggle(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");

        if(authKey.isEmpty()){
            OnRetrieveFailure("You never logged in previously. Please login.");
            return;
        }

        ServerConnection.ToggleInterestedUserRunner.ToggleInterestedUserTask tglTask = new ServerConnection.ToggleInterestedUserRunner.ToggleInterestedUserTask(authKey,trip.getTripID(),handler);
        Thread tglThread = new Thread(new ServerConnection.ToggleInterestedUserRunner(tglTask));
        tglThread.start();
    }

    private void OnRetrieveFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void RetrieveAddressFromLatLong() {

        List<Pair<Double, Double>> coordinates = new ArrayList<>();
        coordinates.add(new Pair<Double, Double>(trip.getPickupLat(), trip.getPickupLong()));
        coordinates.add(new Pair<Double, Double>(trip.getDestinationLat(), trip.getDestinationLong()));
        ServerConnection.GetAddressFromLatLongRunner.GetAddressTask addressTask = new ServerConnection.GetAddressFromLatLongRunner.GetAddressTask(handler, coordinates);
        Thread addrThread = new Thread(new ServerConnection.GetAddressFromLatLongRunner(addressTask));
        addrThread.start();
    }

    private void RetrieveInterestedUsers(){
        ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask IUTask = new ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask(trip.getTripID(),handler);
        Thread IUThread = new Thread(new ServerConnection.GetInterestedUsersRunner(IUTask));
        IUThread.start();
    }

    public void GenerateImage() {
        ImageGenerator mImageGenerator = new ImageGenerator(getContext());

        // Set the icon size to the generated in dip.
        mImageGenerator.setIconSize(500, 300);

        // Set the size of the date and month font in dip.
        mImageGenerator.setDateSize(100);
        mImageGenerator.setMonthSize(50);

        // Set the position of the date and month in dip.
        mImageGenerator.setDatePosition(160);
        mImageGenerator.setMonthPosition(80);

        // Set the color of the font to be generated
        mImageGenerator.setDateColor(Color.YELLOW);
        mImageGenerator.setMonthColor(Color.WHITE);


        Calendar cal = Calendar.getInstance();
        if (trip.getDate() != null) {
            cal.setTime(trip.getDate());
        }

        detailImage.setImageBitmap(mImageGenerator.generateDateImage(cal, 0));
        detailImage.refreshDrawableState();
    }


}
