package fnm.wrmc.nmmu.liftme;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by minnaar on 2016/05/03.
 */
public class UserProfileFragment extends Fragment {

    private EditText editProfileName;
    private EditText editProfileSurname;
    private EditText editProfileEmail;
    private EditText editProfilePhone;
    private CheckedTextView checkedProfileDriver;
    private SeekBar seekBarPassengers;
    private TextView textPassengerCount;

    public UserProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        editProfileName = (EditText)view.findViewById(R.id.editProfileName);
        editProfileSurname = (EditText)view.findViewById(R.id.editProfileSurname);
        editProfileEmail = (EditText)view.findViewById(R.id.editProfileEmail);
        editProfilePhone = (EditText)view.findViewById(R.id.editProfilePhone);
        checkedProfileDriver = (CheckedTextView)view.findViewById(R.id.checkedProfileDriver);
        seekBarPassengers = (SeekBar)view.findViewById(R.id.seekBarPassengers);
        textPassengerCount = (TextView)view.findViewById(R.id.textPassengerCount);
        return view;
    }

}


