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
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import fnm.wrmc.nmmu.liftme.Data_Objects.User;

/**
 * Created by Francois on 2016/05/07.
 */
public class InterestedUsersListAdapter extends RecyclerView.Adapter<InterestedUsersListAdapter.InterestedUserHolder> {

    private List<User> interestedUsers;

    public InterestedUsersListAdapter(List<User> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }

    @Override
    public InterestedUserHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interested_user_item_layout, parent, false);

        //view.setOnClickListener(MainActivity.myOnClickListener);

        InterestedUserHolder interestedUserHolder = new InterestedUserHolder(view);
        return interestedUserHolder;
    }

    @Override
    public void onBindViewHolder(final InterestedUserHolder holder, final int listPosition) {

        TextView userName = holder.userName;
        TextView userEmail = holder.userEmail;
        TextView userPhone = holder.userPhone;
        TextView firstLetter = holder.firstLetter;
        ImageView iVCircleImage = holder.iVCircleImage;

        User curUser = interestedUsers.get(listPosition);

        firstLetter.setText("" + curUser.getName().charAt(0));
        iVCircleImage.setBackgroundTintList(ColorStateList.valueOf(RandomColor()));

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

    public static class InterestedUserHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userEmail;
        TextView userPhone;
        TextView firstLetter;
        ImageView iVCircleImage;

        public InterestedUserHolder(View itemView) {
            super(itemView);
            this.userName = (TextView) itemView.findViewById(R.id.tVInterestedUserName);
            this.userEmail = (TextView) itemView.findViewById(R.id.tVInterestedUserEmail);
            this.userPhone = (TextView) itemView.findViewById(R.id.tVInterestedUserPhone);
            this.firstLetter = (TextView) itemView.findViewById(R.id.tVFirstLetter);
            this.iVCircleImage = (ImageView) itemView.findViewById(R.id.iVInterestedCircle);
        }
    }
}
