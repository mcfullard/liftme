package fnm.wrmc.nmmu.liftme;

import android.content.res.ColorStateList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by minnaar on 2016/05/14.
 */
public class MatchedTripViewHolder extends RecyclerView.ViewHolder {
    public boolean interested = false;
    public TextView title;
    public TextView subTitle1;
    public TextView subTitle2;
    public ImageView star;
    public View textArea;
    private TripClickedListener listener;

    public MatchedTripViewHolder(View holder, final TripClickedListener listener) {
        super(holder);
        this.listener = listener;
        title = (TextView) holder.findViewById(R.id.tripTitle);
        subTitle1 = (TextView) holder.findViewById(R.id.tripSubtitle1);
        subTitle2 = (TextView) holder.findViewById(R.id.tripSubtitle2);
        star = (ImageView) holder.findViewById(R.id.search_trip_star);
        textArea = (View) holder.findViewById(R.id.search_trip_text);
        final View holderFinal = holder;
        textArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTextAreaClicked(v, getLayoutPosition());
            }
        });
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(interested) {
                    star.setBackgroundResource(R.drawable.ic_action_star_0);
                } else {
                    star.setBackgroundResource(R.drawable.ic_action_star_10);
                }
                star.setBackgroundTintList(ColorStateList.valueOf(holderFinal.getResources().getColor(R.color.colorAccent)));
                interested = !interested;
                listener.onStarClicked(v, getLayoutPosition());
            }
        });
    }

    public interface TripClickedListener {
        public void onTextAreaClicked(View caller, int pos);
        public void onStarClicked(View caller, int pos);
    }

}
