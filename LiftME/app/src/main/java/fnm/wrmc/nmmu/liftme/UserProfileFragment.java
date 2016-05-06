package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import fnm.wrmc.nmmu.liftme.ServerConnection.GetUserDetailsRunner.GetUserDetailsTask;

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

    Handler getUserDetailsHandler;

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

        seekBarPassengers.setProgress(0);
        seekBarPassengers.setMax(7);
        seekBarPassengers.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                textPassengerCount.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getUserDetailsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ServerConnection.GET_USER_DETAILS_TASK:
                        GetUserDetailsTask getUserDetailsTask = (GetUserDetailsTask) msg.obj;
                        switch (getUserDetailsTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_FAIL:
                                onGetUserDetailsFailure("Authentication error occurred. Please login.");
                                break;
                            case ServerConnection.AUTHENTICATION_INCOMPLETE:
                                onGetUserDetailsFailure("Could not successfully connect to server to retrieve your trips. Please check your network connection and try again.");
                                break;
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                onGetUserDetailsSuccess(getUserDetailsTask);
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };

        getUserDetails();
    }

    private void getUserDetails() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");

        if(authKey.isEmpty()){
            onGetUserDetailsFailure("You never logged in previously. Please login.");
            return;
        }

        ServerConnection.GetUserDetailsRunner.GetUserDetailsTask userDetailsTask = new ServerConnection.GetUserDetailsRunner.GetUserDetailsTask(authKey, getUserDetailsHandler);
        Thread userDetailsThread = new Thread (new ServerConnection.GetUserDetailsRunner(userDetailsTask));
        userDetailsThread.start();
    }

    private void onGetUserDetailsSuccess(GetUserDetailsTask userDetailsTask) {
        editProfileName.setText(userDetailsTask.name);
        editProfileSurname.setText(userDetailsTask.surname);
        editProfileEmail.setText(userDetailsTask.email);
        editProfilePhone.setText(userDetailsTask.phone);
        checkedProfileDriver.setChecked(userDetailsTask.availableAsDriver == 1);
        if(userDetailsTask.numberOfPassengers > seekBarPassengers.getMax()) {
            seekBarPassengers.setProgress(seekBarPassengers.getMax());
        } else if(userDetailsTask.numberOfPassengers <= 0) {
            seekBarPassengers.setProgress(0);
        } else {
            seekBarPassengers.setProgress(userDetailsTask.numberOfPassengers);
        }
    }

    private void onGetUserDetailsFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}


