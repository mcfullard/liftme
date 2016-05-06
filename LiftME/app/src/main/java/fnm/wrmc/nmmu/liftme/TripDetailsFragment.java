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

import com.kd.dynamic.calendar.generator.ImageGenerator;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripDetailsFragment extends Fragment {

    private ImageView detailImage;

    public TripDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View curView = inflater.inflate(R.layout.fragment_trip_details, container, false);
        detailImage = (ImageView)curView.findViewById(R.id.iVMyTripDetailsImage);

        final ViewTreeObserver detailImageVto = detailImage.getViewTreeObserver();
        detailImageVto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {  //code found at http://stackoverflow.com/questions/4142090/how-do-you-to-retrieve-dimensions-of-a-view-getheight-and-getwidth-always-r?lq=1
            @Override
            public void onGlobalLayout() {
                GenerateImage(R.id.iVMyTripDetailsImage);
                detailImageVto.removeOnGlobalLayoutListener(this);
            }
        });

        return curView;
    }

    public Bitmap GenerateImage(int imagaViewID){
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
        return mImageGenerator.generateDateImage(c,imagaViewID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
