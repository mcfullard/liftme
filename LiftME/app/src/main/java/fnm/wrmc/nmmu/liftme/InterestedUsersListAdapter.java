package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import fnm.wrmc.nmmu.liftme.Data_Objects.User;

/**
 * Created by Francois on 2016/05/07.
 */
public class InterestedUsersListAdapter extends RecyclerView.Adapter<InterestedUsersListAdapter.InterestedUserHolder> {

    private List<User> interestedUsers;
    private IInterestedUserCallback listener;

    public interface IInterestedUserCallback{
        void onUserClicked(User clickedUser);
    }

    public InterestedUsersListAdapter(List<User> interestedUsers,IInterestedUserCallback listener) {
        this.interestedUsers = interestedUsers;
        this.listener = listener;
    }

    @Override
    public InterestedUserHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interested_user_item_layout, parent, false);

        //view.setOnClickListener(MainActivity.myOnClickListener);

        InterestedUserHolder interestedUserHolder = new InterestedUserHolder(view,listener);
        return interestedUserHolder;
    }

    @Override
    public void onBindViewHolder(final InterestedUserHolder holder, final int listPosition) {

        TextView userName = holder.userName;
        TextView userEmail = holder.userEmail;
        TextView userPhone = holder.userPhone;
        TextView firstLetter = holder.firstLetter;
        View divider = holder.divider;
        ImageView iVCircleImage = holder.iVCircleImage;
        ImageView iVhasCar = holder.iVHasCar;

        User curUser = interestedUsers.get(listPosition);
        holder.user = curUser;

        firstLetter.setText("" + curUser.getName().charAt(0));
        iVCircleImage.setBackgroundTintList(ColorStateList.valueOf(RandomColor()));

        if(curUser.getAvailableAsDriver() == 1){
            iVhasCar.setVisibility(View.VISIBLE);
        }else{
            iVhasCar.setVisibility(View.INVISIBLE);
        }

        userName.setText(String.format("%s %s", curUser.getName(), curUser.getSurname()));
        if(curUser.getEmail().isEmpty()){
            userEmail.setText("No Email.");
        }else{
            userEmail.setText(curUser.getEmail());
        }

        if(curUser.getContactNum().isEmpty()){
            userPhone.setText("No Phone number.");
        }else{
            userPhone.setText(curUser.getContactNum());
        }

        if(listPosition == interestedUsers.size() - 1){
            divider.setVisibility(View.INVISIBLE);
        }else{
            divider.setVisibility(View.VISIBLE);
        }
    }

    private int RandomColor(){
        Random rand = new Random();
        int selectedColor = 0;
        switch (rand.nextInt(4)){
            case 0:
                selectedColor = Color.argb(255,56,142,60);
                break;
            case 1:
                selectedColor = Color.argb(255,76,175,80);
                break;
            case 2:
                selectedColor = Color.argb(255,46,125,50);
                break;
            case 3:
                selectedColor = Color.argb(255,255,152,0);
                break;
        }
        return selectedColor;
    }

    @Override
    public int getItemCount() {
        return interestedUsers.size();
    }

    public static class InterestedUserHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView userName;
        TextView userEmail;
        TextView userPhone;
        TextView firstLetter;
        ImageView iVCircleImage;
        ImageView iVHasCar;
        RelativeLayout rLayout;
        View divider;
        User user;

        IInterestedUserCallback listener;

        public InterestedUserHolder(View itemView,IInterestedUserCallback listener) {
            super(itemView);
            this.userName = (TextView) itemView.findViewById(R.id.tVInterestedUserName);
            this.userEmail = (TextView) itemView.findViewById(R.id.tVInterestedUserEmail);
            this.userPhone = (TextView) itemView.findViewById(R.id.tVInterestedUserPhone);
            this.firstLetter = (TextView) itemView.findViewById(R.id.tVFirstLetter);
            this.iVCircleImage = (ImageView) itemView.findViewById(R.id.iVInterestedCircle);
            this.rLayout = (RelativeLayout) itemView.findViewById(R.id.rLInterestedUsrLayout);
            this.divider = itemView.findViewById(R.id.interestedUserDividor);
            this.iVHasCar = (ImageView)itemView.findViewById(R.id.iVHasCar);
            rLayout.setOnClickListener(this);
            this.listener = listener;
        }


        @Override
        public void onClick(View v) {
            if(user != null && listener != null){
                listener.onUserClicked(user);
            }
        }
    }
}
