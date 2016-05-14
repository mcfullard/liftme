package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.Data_Objects.User;

/**
 * Created by Francois on 2016/05/08.
 */
public class MyTripDetailsFragment extends TripDetailsFragment implements InterestedUsersListAdapter.IInterestedUserCallback{

    public static final String FRAG_IDENTIFYER = "fnm.wrmc.nmmu.liftme.MyTripDetailsFragment";

    private InterestedUsersListAdapter adapter;
    private RecyclerView rVinterestedUser;
    private RecyclerView.LayoutManager interestedUserslayoutManager;
    private List<User> interestedUsers;
    private IMyTripsDetailsCallback listener;

    @Override
    public void onUserClicked(User clickedUser) {
        OnInterestedUserClick(clickedUser);
    }

    public interface IMyTripsDetailsCallback{
        void onMyTripDetailsAttach();
        void onMyTripDetailsDetach();
        void onMyTripDetailsDelete();
    }

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
        rVinterestedUser = (RecyclerView)curView.findViewById(R.id.rVTripDetailsInterestedUsers);

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
                    case ServerConnection.DELETE_TRIP_TASK:
                        ServerConnection.DeleteTripRunner.DeleteTripTask deleteTask = (ServerConnection.DeleteTripRunner.DeleteTripTask) inputMessage.obj;
                        switch (deleteTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnDeleteTripSuccess();
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        interestedUserslayoutManager = new LinearLayoutManager(getContext());
        rVinterestedUser.setLayoutManager(interestedUserslayoutManager);
        rVinterestedUser.setItemAnimator(new DefaultItemAnimator());

        interestedUsers = new ArrayList<>();
        adapter = new InterestedUsersListAdapter(interestedUsers,this);
        rVinterestedUser.setAdapter(adapter);

        if(getActivity() instanceof IMyTripsDetailsCallback){
            listener = (IMyTripsDetailsCallback)getActivity();
            listener.onMyTripDetailsAttach();
        }
    }

    private void OnInterestedUsersSuccess(ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask IUTask){
        interestedUsers.addAll(IUTask.interestedUsers);
        adapter.notifyDataSetChanged();
    }

    private void RetrieveInterestedUsers(){
        ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask IUTask = new ServerConnection.GetInterestedUsersRunner.GetInterestedUsersTask(trip.getTripID(),handler);
        Thread IUThread = new Thread(new ServerConnection.GetInterestedUsersRunner(IUTask));
        IUThread.start();
    }

    @Override
    public void onDetach() {
        if(listener != null){
            listener.onMyTripDetailsDetach();
        }
        super.onDetach();
    }

    private void OnDeleteTripSuccess(){
        if(listener != null) {
            listener.onMyTripDetailsDelete();
        }
    }

    public void DeleteTrip(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");

        if(authKey.isEmpty()){
            OnRetrieveFailure("You never logged in previously. Please login.");
            return;
        }

        ServerConnection.DeleteTripRunner.DeleteTripTask deleteTripTask = new ServerConnection.DeleteTripRunner.DeleteTripTask(authKey,trip.getTripID(),handler);
        Thread delThread = new Thread(new ServerConnection.DeleteTripRunner(deleteTripTask));
        delThread.start();
    }

    private void OnInterestedUserClick(User user){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View contactDialog = inflater.inflate(R.layout.contact_dialog, null);
        dialogBuilder.setView(contactDialog);
        final User curUser = user;
        final TextView tvEmail = (TextView) contactDialog.findViewById(R.id.tVCntDialogEmail);
        final TextView tvPhone = (TextView) contactDialog.findViewById(R.id.tVCntDialogPhone);
        final LinearLayout lLEmail = (LinearLayout) contactDialog.findViewById(R.id.layoutEmailUser);
        final LinearLayout lLUser = (LinearLayout) contactDialog.findViewById(R.id.layoutPhoneUser);

        lLEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    String[] Addresses = new String[] {curUser.getEmail()};
                    intent.putExtra(Intent.EXTRA_EMAIL, Addresses);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "LiftMe Trip Interest");
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

        lLUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + curUser.getContactNum()));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

        dialogBuilder.setTitle("Preferred Contact Method");

        if (user.getEmail().isEmpty()) {
            tvEmail.setText("No email available.");
        } else {
            tvEmail.setText("Email " + user.getName() + " " + user.getSurname());
        }

        if(user.getContactNum().isEmpty()){
            tvPhone.setText("No phone number available.");
        }else{
            tvPhone.setText("Call " + user.getName() + " " + user.getSurname());
        }

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
