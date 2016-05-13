package fnm.wrmc.nmmu.liftme;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by minnaar on 2016/05/14.
 */
public class TripViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView subTitle1;
    public TextView subTitle2;
    public ImageView star;
    public View textArea;
    private TripClickedListener listener;

    public TripViewHolder(View holder, final TripClickedListener listener) {
        super(holder);
        this.listener = listener;
        title = (TextView) holder.findViewById(R.id.tripTitle);
        subTitle1 = (TextView) holder.findViewById(R.id.tripSubtitle1);
        subTitle2 = (TextView) holder.findViewById(R.id.tripSubtitle2);
        star = (ImageView) holder.findViewById(R.id.search_trip_star);
        textArea = (View) holder.findViewById(R.id.search_trip_text);
        textArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTextAreaClicked(v);
            }
        });
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStarClicked(v);
            }
        });
    }

    public interface TripClickedListener {
        public void onTextAreaClicked(View caller);
        public void onStarClicked(View caller);
    }

}
