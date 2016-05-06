package fnm.wrmc.nmmu.liftme;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

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
    public static final String GET_USER_POSTED_TRIPS = "#GET_USER_POSTED_TRIPS";
    public static final String GET_USER_DETAILS = "#GET_USER_DETAILS";
    public static final int USER_POSTED_TRIP_TASK = 1;
    public static final int GET_USER_DETAILS_TASK = 1;

    private static final String SERVER_IP = "192.168.56.1";
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
                } catch(IOException e) {
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
            public RegisterActivity registerActivity;

            public RegistrationTask(String email, String password, RegisterActivity registerActivity){
                this.email =email;
                this.password = password;
                this.registerActivity = registerActivity;
            }

            public void HandleRegistration() {
                registerActivity.HandleRegistration(this);
            }
        }
    }

    static class PostedUserTripsRunner implements Runnable{

        UserPostedTripsTask tripsTask;

        public PostedUserTripsRunner(UserPostedTripsTask tripsTask){
            this.tripsTask = tripsTask;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP,SERVER_PORT),CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(GET_USER_POSTED_TRIPS);
                writeStream.writeUTF(tripsTask.authKey);
                writeStream.flush();

                tripsTask.authStatus = readStream.readUTF();

                if(tripsTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    int tripCount = readStream.readInt();
                    List<Trip> trips = new ArrayList<>();
                    for(int x = 0 ; x < tripCount ; x++){
                        Trip curTrip = new Trip();
                        curTrip.setPickupLat(readStream.readFloat());
                        curTrip.setPickupLong(readStream.readFloat());
                        curTrip.setDestinationLat(readStream.readFloat());
                        curTrip.setDestinationLong(readStream.readFloat());
                        Time pickUpTime = new Time(readStream.readLong());
                        Time dropOffTime = new Time(readStream.readLong());
                        Date date = new Date(readStream.readLong());

                        curTrip.setPickupTime(pickUpTime);
                        curTrip.setDropOffTime(dropOffTime);
                        curTrip.setDate(date);

                        trips.add(curTrip);
                    }

                    tripsTask.trips = trips;
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch(IOException e) {
                Log.e("Comms | GetTrips","Error Getting posted trips.");
                e.printStackTrace();
            }

            tripsTask.HandleUserTrips();
        }

        public static class UserPostedTripsTask {

            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public List<Trip> trips;

            public UserPostedTripsTask(String authKey,Handler handler){
                this.authKey =authKey;
                this.handler = handler;
            }

            public void HandleUserTrips() {
                if(trips == null){
                    authStatus = AUTHENTICATION_INCOMPLETE;
                }

                Message completeMessage =
                        handler.obtainMessage(USER_POSTED_TRIP_TASK, this);
                completeMessage.sendToTarget();
            }
        }

    }

    static class GetUserDetailsRunner implements Runnable {

        GetUserDetailsTask getUserDetailsTask;

        public GetUserDetailsRunner(GetUserDetailsTask getUserDetailsTask) {
            this.getUserDetailsTask = getUserDetailsTask;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(GET_USER_DETAILS);
                writeStream.writeUTF(getUserDetailsTask.authKey);
                writeStream.flush();

                getUserDetailsTask.authStatus = readStream.readUTF();

                if(getUserDetailsTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    getUserDetailsTask.name = readStream.readUTF();
                    getUserDetailsTask.surname = readStream.readUTF();
                    getUserDetailsTask.email = readStream.readUTF();
                    getUserDetailsTask.phone = readStream.readUTF();
                    getUserDetailsTask.availableAsDriver = Integer.parseInt(readStream.readUTF());
                    getUserDetailsTask.numberOfPassengers = Integer.parseInt(readStream.readUTF());
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms | GetUserDetails", "Error getting user details.");
            }
        }

        public static class GetUserDetailsTask {
            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public String name = "";
            public String surname = "";
            public String email = "";
            public String phone = "";
            public int availableAsDriver = 0;
            public int numberOfPassengers = 0;

            public GetUserDetailsTask(String authKey, Handler handler) {
                this.authKey = authKey;
                this.handler = handler;
            }

            public void HandleGetUserDetails() {
                handler.obtainMessage(GET_USER_DETAILS_TASK, this).sendToTarget();
            }
        }
    }
}
