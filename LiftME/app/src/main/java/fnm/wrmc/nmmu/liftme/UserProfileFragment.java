package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    Handler setUserDetailsHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        editProfileName = (EditText)view.findViewById(R.id.editProfileName);
        editProfileSurname = (EditText)view.findViewById(R.id.editProfileSurname);
        editProfileEmail = (EditText)view.findViewById(R.id.editProfileEmail);
        editProfilePhone = (EditText)view.findViewById(R.id.editProfilePhone);
        checkedProfileDriver = (CheckedTextView)view.findViewById(R.id.checkedProfileDriver);
        seekBarPassengers = (SeekBar)view.findViewById(R.id.seekBarPassengers);
        textPassengerCount = (TextView)view.findViewById(R.id.textPassengerCount);

        checkedProfileDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckedTextView checkBox = (CheckedTextView) v;
                if(checkBox.isChecked())
                    checkBox.setChecked(false);
                else
                    checkBox.setChecked(true);
            }
        });

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.change_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_accept_changes) {
            setUserDetails();
        }
        return super.onOptionsItemSelected(item);
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

        /**
         * Just defined for now. Don't see the use yet.
         */
        setUserDetailsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        getUserDetails();
    }

    private String getAuthKey() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");
        if(authKey.isEmpty()){
            onGetUserDetailsFailure("You never logged in previously. Please login.");
            return "";
        }
        return authKey;
    }

    /**
     *  Extract the user data from the graphical components, pass it to a task and pass the task to a runnable thread
     */
    private void setUserDetails() {
        String authKey = getAuthKey();
        ServerConnection.SetUserDetailsRunner.SetDetailsTask setDetailsTask = new ServerConnection.SetUserDetailsRunner.SetDetailsTask(
                authKey,
                setUserDetailsHandler,
                editProfileName.getText().toString(),
                editProfileSurname.getText().toString(),
                editProfilePhone.getText().toString(),
                editProfileEmail.getText().toString(),
                checkedProfileDriver.isChecked() ? 1 : 0,
                seekBarPassengers.getProgress()
        );
        Thread setUserDetailsThread = new Thread(new ServerConnection.SetUserDetailsRunner(setDetailsTask));
        setUserDetailsThread.start();
    }

    private void getUserDetails() {
        String authKey = getAuthKey();
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


