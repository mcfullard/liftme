package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import fnm.wrmc.nmmu.liftme.ServerConnection.RegisterRunner.RegistrationTask;

public class RegisterAct extends AppCompatActivity {

    Handler registrationHandler;
    private final int REG_MESSAGE = 1;

    EditText edtEmail, edtPassword, edtPasswordConf;
    ProgressBar pbRegisterSpinner;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        edtEmail = (EditText) findViewById(R.id.edtRegisterEmail);
        edtPassword = (EditText) findViewById(R.id.edtRegisterPassword);
        edtPasswordConf = (EditText) findViewById(R.id.edtRegisterPasswordConf);
        btnRegister = (Button) findViewById(R.id.btnRegRegister);
        pbRegisterSpinner = (ProgressBar) findViewById(R.id.pbRegisterSpinner);

        btnRegister.setVisibility(View.VISIBLE);
        pbRegisterSpinner.setVisibility(View.GONE);

        Intent callingIntent = getIntent();

        edtEmail.setText(callingIntent.getStringExtra("Email"));

        registrationHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REG_MESSAGE:
                        RegistrationTask curRegTask = (RegistrationTask) msg.obj;
                        btnRegister.setVisibility(View.VISIBLE);
                        pbRegisterSpinner.setVisibility(View.GONE);
                        switch (curRegTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnRegistrationSuccess(curRegTask);
                                break;
                            case ServerConnection.AUTHENTICATION_FAIL:
                                OnRegistrationFailure("The email entered already exists.");
                                break;
                            case ServerConnection.AUTHENTICATION_INCOMPLETE:
                                OnRegistrationFailure("Could not successfully connect to server for registration. Please check your network connection and try again.");
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    private void OnRegistrationSuccess(RegistrationTask regTask) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ServerConnection.AUTHENTICATION_TOKEN, regTask.authKey);
        editor.commit();

        //TODO Change to edit profile activity on successful registration
    }

    private void OnRegistrationFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void HandleRegistration(RegistrationTask regTask) {
        Message completeMessage =
                registrationHandler.obtainMessage(REG_MESSAGE, regTask);
        completeMessage.sendToTarget();
    }

    public void OnRegisterClick(View view) {

        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        String passwordConf = edtPasswordConf.getText().toString();
        int dataValidationCheck = 1;

        if (email.isEmpty()) {
            OnRegistrationFailure("Please enter your email.");
            dataValidationCheck = 0;
        } else if (password.isEmpty()) {
            OnRegistrationFailure("Please enter your password.");
            dataValidationCheck = 0;
        } else if (passwordConf.isEmpty()) {
            OnRegistrationFailure("Please confirm your password.");
            dataValidationCheck = 0;
        }

        if (dataValidationCheck == 1) {
            if (password.equals(passwordConf)) {
                btnRegister.setVisibility(View.GONE);
                pbRegisterSpinner.setVisibility(View.VISIBLE);

                RegistrationTask regTask = new RegistrationTask(email, password, this);

                Thread registrationThread = new Thread(new ServerConnection.RegisterRunner(regTask));

                registrationThread.start();
            } else {
                OnRegistrationFailure("Your confirmation password does not match.");
            }
        }
    }
}
