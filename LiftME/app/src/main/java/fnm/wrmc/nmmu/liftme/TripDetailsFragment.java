package fnm.wrmc.nmmu.liftme;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripDetailsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String ARG_TRIP = "Selected_Trip";
    public static final String FRAG_IDENTIFYER = "fnm.wrmc.nmmu.liftme.TripDetailsFragment";

    private ImageView detailImage;
    private TextView tVPUDetails,tVDesDetails;
    private Trip trip;
    private Handler handler;

    public TripDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View curView = inflater.inflate(R.layout.fragment_trip_details, container, false);
        detailImage = (ImageView)curView.findViewById(R.id.iVMyTripDetailsImage);
        tVPUDetails = (TextView) curView.findViewById(R.id.tVPickupDescription);
        tVDesDetails = (TextView) curView.findViewById(R.id.tVDestinationDescription);

        handler = new Handler(){
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
                                OnAddressRetrieveFailure("Unable to retrieve address at this time.");
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(inputMessage);
                        break;
                }
            }
        };

        Bundle curBundle = getArguments();

        if(curBundle != null){
            trip = (Trip)curBundle.get(ARG_TRIP);
            RetrieveAddressFromLatLong();
            GenerateImage();
        }

        return curView;
    }

    private void OnAddressRetrieveSuccess(ServerConnection.GetAddressFromLatLongRunner.GetAddressTask addressTask){
        List<String> addressList = addressTask.Addresses;
        String puDetails = addressList.get(0).replace(",","\n");
        String desDetails = addressList.get(1).replace(",", "\n");
        tVPUDetails.setText(puDetails);
        tVDesDetails.setText(desDetails);
    }

    private void OnAddressRetrieveFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void RetrieveAddressFromLatLong(){

        List<Pair<Double,Double>> coordinates = new ArrayList<>();
        coordinates.add(new Pair<Double, Double>(trip.getPickupLat(),trip.getPickupLong()));
        coordinates.add(new Pair<Double, Double>(trip.getDestinationLat(),trip.getDestinationLong()));
        ServerConnection.GetAddressFromLatLongRunner.GetAddressTask addressTask = new ServerConnection.GetAddressFromLatLongRunner.GetAddressTask(handler,coordinates);
        Thread addrThread = new Thread (new ServerConnection.GetAddressFromLatLongRunner(addressTask));
        addrThread.start();
    }

    public void GenerateImage(){
        ImageGenerator mImageGenerator = new ImageGenerator(getContext());

        // Set the icon size to the generated in dip.
        mImageGenerator.setIconSize(500, 200);

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
        if(trip.getDate() != null){
            cal.setTime(trip.getDate());
        }

        detailImage.setImageBitmap(mImageGenerator.generateDateImage(cal, 0));
        detailImage.refreshDrawableState();
    }



}
