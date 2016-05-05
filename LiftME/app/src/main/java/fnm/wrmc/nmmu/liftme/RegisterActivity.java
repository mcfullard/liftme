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
import android.widget.TextView;
import android.widget.Toast;

import fnm.wrmc.nmmu.liftme.ServerConnection.RegisterRunner.RegistrationTask;

public class RegisterActivity extends AppCompatActivity {

    private static Handler registrationHandler;
    private final int REG_MESSAGE = 1;

    EditText edtEmail, edtPassword, edtPasswordConf;
    ProgressBar pbRegisterSpinner;
    Button btnRegister;
    TextView tvRegistering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        edtEmail = (EditText) findViewById(R.id.edtRegisterEmail);
        edtPassword = (EditText) findViewById(R.id.edtRegisterPassword);
        edtPasswordConf = (EditText) findViewById(R.id.edtRegisterPasswordConf);
        btnRegister = (Button) findViewById(R.id.btnRegRegister);
        pbRegisterSpinner = (ProgressBar) findViewById(R.id.pbRegisterSpinner);
        tvRegistering = (TextView)findViewById(R.id.tVRegistering);

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
                        ResetRegisterAnimation();
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

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("fragment_number", DashboardActivity.USER_PROFILE_FRAGMENT);
        startActivity(intent);
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
            edtEmail.setError("Please enter your email.");
            dataValidationCheck = 0;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Please enter your password.");
            dataValidationCheck = 0;
        }
        if (passwordConf.isEmpty()) {
            edtPasswordConf.setError("Please confirm your password.");
            dataValidationCheck = 0;
        }

        if (dataValidationCheck == 1) {
            if (password.equals(passwordConf)) {
                AnimateRegister();

                RegistrationTask regTask = new RegistrationTask(email, password, this);

                Thread registrationThread = new Thread(new ServerConnection.RegisterRunner(regTask));

                registrationThread.start();
            } else {
                edtPasswordConf.setError("Your confirmation password does not match.");
            }
        }
    }

    private void AnimateRegister() {
        btnRegister.animate().alpha(0.0f).start();
        edtEmail.animate().alpha(0.5f).start();
        edtPassword.animate().alpha(0.5f).start();
        pbRegisterSpinner.setVisibility(View.VISIBLE);
        tvRegistering.setVisibility(View.VISIBLE);

        edtEmail.clearFocus();
        edtEmail.setFocusable(false);
        edtPassword.clearFocus();
        edtPassword.setFocusable(false);

        pbRegisterSpinner.animate().translationX(-230f);
        tvRegistering.animate().translationX(230f);
    }

    private void ResetRegisterAnimation(){
        btnRegister.animate().alpha(1.0f).start();
        edtEmail.animate().alpha(1.0f).start();
        edtPassword.animate().alpha(1.0f).start();
        pbRegisterSpinner.setVisibility(View.INVISIBLE);
        tvRegistering.setVisibility(View.INVISIBLE);

        edtEmail.setFocusable(true);
        edtEmail.setFocusableInTouchMode(true);
        edtPassword.setFocusable(true);
        edtPassword.setFocusableInTouchMode(true);

        pbRegisterSpinner.animate().translationX(230f);
        tvRegistering.animate().translationX(-230f);
    }
}
