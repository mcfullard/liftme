package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Francois on 2016/04/13.
 */
public class MyTripsListAdapter extends ArrayAdapter<Trip> {

    public MyTripsListAdapter(Context context, List<Trip> objects) {
        super(context, R.layout.my_trip_item_layout, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View curView = convertView;
        if(curView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            curView = layoutInflater.inflate(R.layout.my_trip_item_layout,parent,false);
        }

        Trip curTrip = getItem(position);

        ImageView destinationMapView = (ImageView)curView.findViewById(R.id.iVMyTripMap);
        TextView tVTripTitle = (TextView)curView.findViewById(R.id.tVMyTripTitle);
        TextView tVTripTime = (TextView)curView.findViewById(R.id.tVMyTripTime);


        tVTripTitle.setText(curTrip.getDayOfWeek());
        tVTripTime.setText(curTrip.getDropOffTime().toString());

        DownloadImageTask dlImageTask = new DownloadImageTask();


        dlImageTask.execute(destinationMapView,String.format("https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=16&size=600x240",curTrip.getDestinationLat(),curTrip.getDestinationLong()));

        return curView;
    }

    private class DownloadImageTask extends AsyncTask<Object, Void, Drawable> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        private ImageView imageView;

        protected Drawable doInBackground(Object... args) {
            imageView = (ImageView)args[0];
            return LoadImageFromWebOperations((String)args[1]);
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Drawable result) {
            imageView.setImageDrawable(result);
        }

        private Drawable LoadImageFromWebOperations(String url) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, null);
                return d;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
