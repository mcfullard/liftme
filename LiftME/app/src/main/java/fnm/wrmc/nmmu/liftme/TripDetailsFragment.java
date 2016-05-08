package fnm.wrmc.nmmu.liftme;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.Utilities.ImageGenerator;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class TripDetailsFragment extends Fragment {

    public static final String ARG_TRIP = "Selected_Trip";
    public static final String ARG_TRIP_TYPE = "Trip_Type";


    protected ImageView detailImage;
    protected TextView tVPUDetails, tVDesDetails;

    protected Handler handler;
    protected Trip trip;



    public TripDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_details, container, false);
    }

    protected void OnAddressRetrieveSuccess(ServerConnection.GetAddressFromLatLongRunner.GetAddressTask addressTask) {
        List<String> addressList = addressTask.Addresses;
        String puDetails = addressList.get(0).replace(",", "\n");
        String desDetails = addressList.get(1).replace(",", "\n");
        tVPUDetails.setText(puDetails);
        tVDesDetails.setText(desDetails);
    }

    protected void OnRetrieveFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void RetrieveAddressFromLatLong() {

        List<Pair<Double, Double>> coordinates = new ArrayList<>();
        coordinates.add(new Pair<Double, Double>(trip.getPickupLat(), trip.getPickupLong()));
        coordinates.add(new Pair<Double, Double>(trip.getDestinationLat(), trip.getDestinationLong()));
        ServerConnection.GetAddressFromLatLongRunner.GetAddressTask addressTask = new ServerConnection.GetAddressFromLatLongRunner.GetAddressTask(handler, coordinates);
        Thread addrThread = new Thread(new ServerConnection.GetAddressFromLatLongRunner(addressTask));
        addrThread.start();
    }

    protected void GenerateImage() {
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
        if (trip.getPickupTime() != null) {
            cal.setTime(trip.getPickupTime());
        }

        detailImage.setImageBitmap(mImageGenerator.generateDateImage(cal, 0));
        detailImage.refreshDrawableState();
    }


}
