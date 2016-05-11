package fnm.wrmc.nmmu.liftme;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.Data_Objects.User;

/**
 * Created by Francois on 2016/05/08.
 */
public class MyTripDetailsFragment extends TripDetailsFragment {

    public static final String FRAG_IDENTIFYER = "fnm.wrmc.nmmu.liftme.MyTripDetailsFragment";

    private InterestedUsersListAdapter adapter;
    private ListView lVinterestedUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle curBundle = getArguments();

        if (curBundle != null) {
            trip = (Trip) curBundle.get(ARG_TRIP);
            return setupFragForMyTrip(inflater, container);
        }else{
            return super.onCreateView(inflater,container,savedInstanceState);
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
                        break;
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

    private void OnInterestedUsersSuccess(ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask IUTask){
        adapter.clear();
        adapter.addAll(IUTask.interestedUsers);
    }

    private void RetrieveInterestedUsers(){
        ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask IUTask = new ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask(trip.getTripID(),handler);
        Thread IUThread = new Thread(new ServerConnection.GetInterestedUsersRunner(IUTask));
        IUThread.start();
    }
}