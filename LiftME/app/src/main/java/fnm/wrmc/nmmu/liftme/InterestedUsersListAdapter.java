package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

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
        TextView userDetails = holder.userDetails;

        User curUser = interestedUsers.get(listPosition);

        userName.setText(String.format("%s %s", curUser.getName(), curUser.getSurname()));
        userDetails.setText(String.format("%s\n%s", curUser.getEmail(), curUser.getContactNum()));
    }

    @Override
    public int getItemCount() {
        return interestedUsers.size();
    }

    public static class InterestedUserHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userDetails;

        public InterestedUserHolder(View itemView) {
            super(itemView);
            this.userName = (TextView) itemView.findViewById(R.id.tVInterestedUserName);
            this.userDetails = (TextView) itemView.findViewById(R.id.tVInterestedUserDetails);
        }
    }
}
