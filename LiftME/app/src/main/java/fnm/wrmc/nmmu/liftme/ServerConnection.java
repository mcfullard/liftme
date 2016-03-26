package fnm.wrmc.nmmu.liftme;

import android.app.Activity;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
                    socket = new Socket("192.168.1.69", 5050);


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
                    Log.e("Server Connection","Error authenticating user " + authTask.email);
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

}
