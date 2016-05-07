package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Francois on 2016/05/07.
 */
public class InterestedUsersListAdapter extends ArrayAdapter<User> {

    public InterestedUsersListAdapter(Context context, List<User> objects) {
        super(context, R.layout.interested_user_item_layout, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View curView = convertView;

        if(curView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            curView = layoutInflater.inflate(R.layout.interested_user_item_layout,parent,false);
        }

        TextView userName = (TextView)curView.findViewById(R.id.tVInterestedUserName);
        TextView userDetails = (TextView)curView.findViewById(R.id.tVInterestedUserDetails);

        User curUser = getItem(position);

        userName.setText(String.format("%s %s",curUser.getName(),curUser.getSurname()));
        userDetails.setText(String.format("%s\n%s",curUser.getEmail(),curUser.getContactNum()));

        return curView;
    }
}
