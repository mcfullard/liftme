package fnm.wrmc.nmmu.liftme;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import fnm.wrmc.nmmu.liftme.ServerConnection.AuthenticationRunner.AuthenticationTask;

public class LoginActivity extends AppCompatActivity {

    Handler authenticationHandler;
    private final int  AUTH_MESSAGE = 1;
    ProgressBar loginSpinner;
    Button btnLogin;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        loginSpinner = (ProgressBar)findViewById(R.id.pBLoginSpinner);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnRegister = (Button)findViewById(R.id.btnRegister);

        btnLogin.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.VISIBLE);
        loginSpinner.setVisibility(View.INVISIBLE);

        authenticationHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case AUTH_MESSAGE:
                        AuthenticationTask curAuthTask = (AuthenticationTask) inputMessage.obj;
                        btnLogin.setVisibility(View.VISIBLE);
                        btnRegister.setVisibility(View.VISIBLE);
                        loginSpinner.setVisibility(View.INVISIBLE);
                        switch (curAuthTask.authStatus) {
                            case ServerConnection.AUTHENTICATION_SUCCESS:
                                OnAuthenticationFailure("!!!PLACEHOLDER!!! SUCCESS");
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

    public void HandleAuthentication(AuthenticationTask authTask){
        Message completeMessage =
                authenticationHandler.obtainMessage(AUTH_MESSAGE, authTask);
        completeMessage.sendToTarget();
    }

    private void OnAuthenticationSuccess(AuthenticationTask authTask){


    }

    private void OnAuthenticationFailure(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void OnLoginClick(View view){
        EditText edtEmail = (EditText)findViewById(R.id.edtLoginEmail);
        EditText edtPassword = (EditText)findViewById(R.id.edtLoginPassword);

        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        Boolean bContinue = true;

        if(email.isEmpty()){
            OnAuthenticationFailure("You have not entered your email.");
            bContinue = false;
        }

        if(password.isEmpty()){
            OnAuthenticationFailure("You have not entered your password.");
            bContinue = false;
        }

        if(bContinue){

            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.GONE);
            loginSpinner.setVisibility(View.VISIBLE);
            AuthenticationTask authTask = new AuthenticationTask(email,password,this);

            Thread authenticationThread = new Thread(new ServerConnection.AuthenticationRunner(authTask));

            authenticationThread.start();
        }

    }
}
