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

import org.w3c.dom.Text;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;

/**
 * Created by Francois on 2016/05/08.
 */
public class ViewTripDetails extends TripDetailsFragment {

    public static final String FRAG_IDENTIFYER = "fnm.wrmc.nmmu.liftme.ViewTripDetails";

    private SearchedTrip searchedTrip;
    private TextView tVPickUpDistance, tVDestinationDistance;

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
        tVPickUpDistance = (TextView) curView.findViewById(R.id.tvPickUpDistance);
        tVDestinationDistance = (TextView) curView.findViewById(R.id.tVDestinationDistance);

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
                    default:
                        super.handleMessage(inputMessage);
                        break;
                }
            }
        };

        RetrieveAddressFromLatLong();
        GenerateImage();
        DisplayDistances();

        return curView;

    }

    private void DisplayDistances(){
        if(searchedTrip.getDistanceBetweenPickups() == 0 ){
            tVPickUpDistance.setText("");
        }else{
            tVPickUpDistance.setText(String.format("%.2fkm from pickup.", searchedTrip.getDistanceBetweenPickups()));
        }

        if(searchedTrip.getDistanceBetweenPickups() == 0 ){
            tVDestinationDistance.setText("");
        }else{
            tVDestinationDistance.setText(String.format("%.2fkm from destination.", searchedTrip.getDistanceBetweenDropOffs()));
        }
    }
}
