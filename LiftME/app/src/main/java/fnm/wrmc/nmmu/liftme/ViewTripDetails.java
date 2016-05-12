package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;

/**
 * Created by Francois on 2016/05/08.
 */
public class ViewTripDetails extends TripDetailsFragment {

    public static final String FRAG_IDENTIFYER = "fnm.wrmc.nmmu.liftme.ViewTripDetails";

    private SearchedTrip searchedTrip;
    private FloatingActionButton fabInterestedToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle curBundle = getArguments();

        if (curBundle != null) {
            searchedTrip = (SearchedTrip) curBundle.get(ARG_TRIP);
            trip = searchedTrip;
            return setupFragForViewTrip(inflater, container);
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    protected View setupFragForViewTrip(LayoutInflater inflater,ViewGroup container){
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

    private void OnInterestedUserToggleSuccess(ServerConnection.ToggleInterestedUserRunner.ToggleInterestedUserTask tglTask){
        if(tglTask.toggleStatus == 2) {
            fabInterestedToggle.setBackgroundColor(Color.GRAY);
            fabInterestedToggle.setImageResource(R.drawable.is_interested);
        }else{
            fabInterestedToggle.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            fabInterestedToggle.setImageResource(R.drawable.not_interested);
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
}
