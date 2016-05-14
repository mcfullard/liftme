package fnm.wrmc.nmmu.liftme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import fnm.wrmc.nmmu.liftme.ServerConnection.AuthenticationRunner.AuthenticationTask;

public class LoginActivity extends AppCompatActivity {

    Handler authenticationHandler;
    private final int  AUTH_MESSAGE = 1;
    ProgressBar loginSpinner;
    Button btnLogin;
    Button btnRegister;
    EditText edtEmail;
    EditText edtPassword;
    TextView tVLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        quickLogin();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        loginSpinner = (ProgressBar)findViewById(R.id.pBLoginSpinner);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        edtEmail = (EditText)findViewById(R.id.edtLoginEmail);
        edtPassword = (EditText)findViewById(R.id.edtLoginPassword);
        tVLogin = (TextView)findViewById(R.id.tVLogin);

        btnLogin.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.VISIBLE);
        loginSpinner.setVisibility(View.INVISIBLE);

        authenticationHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case AUTH_MESSAGE:
                        AuthenticationTask curAuthTask = (AuthenticationTask) inputMessage.obj;
                        ResetLoginAnimation();

                        switch (curAuthTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnAuthenticationSuccess(curAuthTask);
                                break;
                            case ServerConnection.AUTHENTICATION_FAIL:
                                OnAuthenticationFailure("The email and password combination was not found.");
                                break;
                            case ServerConnection.AUTHENTICATION_INCOMPLETE:
                                OnAuthenticationFailure("Could not successfully connect to server for login. Please check your network connection and try again.");
                                break;
                        }
                        break;
                    default:
                        super.handleMessage(inputMessage);
                        break;
                }
            }
        };


    }

    private void quickLogin(){
        SharedPreferences sharedPref = this.getSharedPreferences("GlobalPref", Context.MODE_PRIVATE);
        String authKey = sharedPref.getString(ServerConnection.AUTHENTICATION_TOKEN,"");

        if(!authKey.isEmpty()){
            GoToDashBoard();
        }
    }

    public void HandleAuthentication(AuthenticationTask authTask){
        Message completeMessage =
                authenticationHandler.obtainMessage(AUTH_MESSAGE, authTask);
        completeMessage.sendToTarget();
    }

    private void OnAuthenticationSuccess(AuthenticationTask authTask){
        SharedPreferences sharedPref = this.getSharedPreferences("GlobalPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ServerConnection.AUTHENTICATION_TOKEN, authTask.authKey);
        editor.commit();
        GoToDashBoard();

    }

    private void GoToDashBoard(){


        // may want to send the dashboard useful data here... or perhaps that should be done asynchronously?
//        Intent intent = new Intent(this, DashboardActivity.class);
//        startActivity(intent);

        Intent intent = new Intent(this, LocationActivity.class);
        startActivity(intent);
    }

    private void OnAuthenticationFailure(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();


        //TODO remove this!!!!!!
        Toast.makeText(this,"FOR DEBUGGING PURPOSES ONLY YOU CAN LOGIN WITHOUT THE SERVER WORKING!!!!!!!!!",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        /*
        Intent intent = new Intent(this, LocationActivity.class);
        startActivity(intent);
        */
    }

    public void OnLoginClick(View view){


        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        Boolean bContinue = true;

        if(email.isEmpty()){
            edtEmail.setError("Please enter your email.");
            bContinue = false;
        }

        if(password.isEmpty()){
            edtPassword.setError("Please enter your password.");
            bContinue = false;
        }

        if(bContinue){

            AnimateLogin();


            AuthenticationTask authTask = new AuthenticationTask(email,password,this);

            Thread authenticationThread = new Thread(new ServerConnection.AuthenticationRunner(authTask));

            authenticationThread.start();
        }

    }

    private void AnimateLogin() {
        btnLogin.animate().alpha(0.0f).start();
        btnRegister.animate().alpha(0.0f).start();
        edtEmail.animate().alpha(0.5f).start();
        edtPassword.animate().alpha(0.5f).start();
        loginSpinner.setVisibility(View.VISIBLE);
        tVLogin.setVisibility(View.VISIBLE);
        edtEmail.clearFocus();
        edtEmail.setFocusable(false);
        edtPassword.clearFocus();
        edtPassword.setFocusable(false);

        float movement = tVLogin.getWidth()/2;

        loginSpinner.animate().translationX(-movement);
        tVLogin.animate().translationX(movement);
    }

    private void ResetLoginAnimation(){
        btnLogin.animate().alpha(1.0f).start();
        btnRegister.animate().alpha(1.0f).start();
        edtEmail.animate().alpha(1.0f).start();
        edtPassword.animate().alpha(1.0f).start();
        loginSpinner.setVisibility(View.INVISIBLE);
        tVLogin.setVisibility(View.INVISIBLE);

        edtEmail.setFocusable(true);
        edtEmail.setFocusableInTouchMode(true);
        edtPassword.setFocusable(true);
        edtPassword.setFocusableInTouchMode(true);

        float movement = tVLogin.getWidth()/2;
        loginSpinner.animate().translationX(movement);
        tVLogin.animate().translationX(-movement);
    }

    public void OnRegisterClick(View view){
        Intent registerIntent = new Intent(this,RegisterActivity.class);
        registerIntent.putExtra("Email",edtEmail.getText().toString());
        startActivity(registerIntent);
    }
}
