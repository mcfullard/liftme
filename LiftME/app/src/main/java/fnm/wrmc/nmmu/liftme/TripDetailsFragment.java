package fnm.wrmc.nmmu.liftme;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.kd.dynamic.calendar.generator.ImageGenerator;

import org.w3c.dom.Text;

import java.util.Calendar;


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

        Bundle curBundle = getArguments();

        if(curBundle != null){
            trip = (Trip)curBundle.get(ARG_TRIP);
            tVPUDetails.setText(String.format("%s\n%s\n%s\n",trip.getPickupLong(),trip.getPickupLong(),trip.getDayOfWeek(),trip.getPickupTime()));
            tVDesDetails.setText(String.format("%s\n%s\n%s\n",trip.getDestinationLong(),trip.getDestinationLong(),trip.getDayOfWeek(),trip.getDropOffTime()));
        }

        return curView;
    }


    public void GenerateImage(int imagaViewID){
        ImageGenerator mImageGenerator = new ImageGenerator(getContext());

        // Set the icon size to the generated in dip.
        mImageGenerator.setIconSize(50, 50);

        // Set the size of the date and month font in dip.
        mImageGenerator.setDateSize(30);
        mImageGenerator.setMonthSize(10);

        // Set the position of the date and month in dip.
        mImageGenerator.setDatePosition(42);
        mImageGenerator.setMonthPosition(14);

        // Set the color of the font to be generated
        mImageGenerator.setDateColor(Color.parseColor("#3c6eaf"));
        mImageGenerator.setMonthColor(Color.WHITE);

        Calendar c = Calendar.getInstance();
        c.set(2016,11,17);
        mImageGenerator.generateDateImage(c,imagaViewID);
    }

}
