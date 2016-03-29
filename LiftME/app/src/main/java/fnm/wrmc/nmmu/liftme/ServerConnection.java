package fnm.wrmc.nmmu.liftme;

import android.app.Activity;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Francois on 2016/03/25.
 */
public class ServerConnection {

    public static final String QUIT_MESSAGE = "#QUIT";
    public static final String MESSAGE_TO = "#MESSAGETO";
    public static final String MESSAGE_FROM = "#MESSAGEFROM";
    public static final String REGISTER = "#REGISTER";
    public static final String AUTHENTICATE = "#AUTHENTICATE";
    public static final String AUTHENTICATION_FAIL = "#NOAUTH";
    public static final String AUTHENTICATION_SUCCESS = "#YESAUTH";
    public static final String AUTHENTICATION_INCOMPLETE = "#INCOMPLETEAUTH";
    public static final String UPDATE_DETAILS = "#UPDATE_DETAILS";
    public static final String AUTHENTICATION_TOKEN = "AUTH_TOKEN";

    private static final String SERVER_IP = "192.168.1.74";
    private static final int SERVER_PORT = 5050;
    private static final int CONNECTION_TIMEOUT = 5000;

    static class AuthenticationRunner implements Runnable {

        private AuthenticationTask authTask;

        AuthenticationRunner(AuthenticationTask authTask){
            this.authTask = authTask;
        }

        @Override
        public void run() {

            if(authTask.email.isEmpty() && authTask.password.isEmpty()){
                authTask.authStatus = AUTHENTICATION_FAIL;
                authTask.HandleAuthentication();
            }else{
                Socket socket = null;

                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(SERVER_IP,SERVER_PORT),CONNECTION_TIMEOUT);

                    DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream readStream = new DataInputStream(socket.getInputStream());

                    writeStream.writeUTF(AUTHENTICATE);
                    writeStream.writeUTF(authTask.email);
                    writeStream.writeUTF(authTask.password);
                    writeStream.flush();

                    authTask.authStatus = readStream.readUTF();

                    if(authTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                        authTask.authKey = readStream.readUTF();
                    }

                    writeStream.writeUTF(QUIT_MESSAGE);

                    writeStream.close();
                    readStream.close();
                    socket.close();
                }catch(IOException e){
                    Log.e("Comms | Authentication","Error authenticating user " + authTask.email);
                    e.printStackTrace();
                }
                authTask.HandleAuthentication();
            }
        }

        public static class AuthenticationTask {

            public String email;
            public String password;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            public String authKey;

            private LoginActivity LoginAct;

            public AuthenticationTask(String email, String password, LoginActivity callingAct){
                this.email =email;
                this.password = password;
                this.LoginAct =callingAct;
            }

            public void HandleAuthentication() {
                LoginAct.HandleAuthentication(this);
            }

        }
    }

    static class RegisterRunner implements Runnable {

        RegistrationTask regTask;

        public RegisterRunner(RegistrationTask regTask){
            this.regTask = regTask;
        }

        @Override
        public void run() {
            if(regTask.email.isEmpty() && regTask.password.isEmpty()){
                regTask.authStatus = AUTHENTICATION_FAIL;
                regTask.HandleRegistration();
            }else{
                Socket socket = null;

                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(SERVER_IP,SERVER_PORT),CONNECTION_TIMEOUT);

                    DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream readStream = new DataInputStream(socket.getInputStream());

                    writeStream.writeUTF(REGISTER);
                    writeStream.writeUTF(regTask.email);
                    writeStream.writeUTF(regTask.password);
                    writeStream.flush();

                    regTask.authStatus = readStream.readUTF();

                    if(regTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                        regTask.authKey = readStream.readUTF();
                    }

                    writeStream.writeUTF(QUIT_MESSAGE);

                    writeStream.close();
                    readStream.close();
                    socket.close();
                }catch(IOException e){
                    Log.e("Comms | Register","Error Registering user " + regTask.email);
                    e.printStackTrace();
                }
                regTask.HandleRegistration();
            }


        }

        public static class RegistrationTask {

            public String email;
            public String password;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            public String authKey;
            public RegisterAct registerAct;

            public RegistrationTask(String email, String password, RegisterAct registerAct){
                this.email =email;
                this.password = password;
                this.registerAct = registerAct;
            }

            public void HandleRegistration() {
                registerAct.HandleRegistration(this);
            }
        }
    }
}
