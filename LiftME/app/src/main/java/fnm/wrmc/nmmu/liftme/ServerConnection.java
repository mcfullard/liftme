package fnm.wrmc.nmmu.liftme;

import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import fnm.wrmc.nmmu.liftme.Data_Objects.SearchedTrip;
import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;
import fnm.wrmc.nmmu.liftme.Data_Objects.User;

/**
 * Created by Francois on 2016/03/25.
 */
public class ServerConnection {

    public static final String QUIT_MESSAGE = "#QUIT";
    public static final String REGISTER = "#REGISTER";
    public static final String AUTHENTICATE = "#AUTHENTICATE";
    public static final String AUTHENTICATION_FAIL = "#NOAUTH";
    public static final String AUTHENTICATION_SUCCESS = "#YESAUTH";
    public static final String AUTHENTICATION_INCOMPLETE = "#INCOMPLETEAUTH";
    public static final String AUTHENTICATION_TOKEN = "AUTH_TOKEN";

    public static final String GET_USER_POSTED_TRIPS = "#GET_USER_POSTED_TRIPS";
    public static final String GET_USER_DETAILS = "#GET_USER_DETAILS";
    public static final String SET_USER_DETAILS = "#SET_USER_DETAILS";
    public static final String GET_INTERESTED_USERS = "#GET_INTERESTED_USERS";
    public static final String INTERESTED_USER_TOGGLE = "#INTERESTED_USER_TOGGLE";
    public static final String SEARCH_TRIPS = "#SEARCH_TRIPS";
    public static final String DELETE_TRIP = "#DELETE_TRIP";
    public static final String GET_USER_INTERESTED_TRIPS = "#GET_USER_INTERESTED_TRIPS";
    public static final String POST_NEW_TRIP = "#POST_NEW_TRIP";

    public static final String STATUS_SUCCESS = "#SUCCESS";
    public static final String STATUS_FAILED = "#FAILED";

    public static final int USER_POSTED_TRIP_TASK = 1;
    public static final int GET_USER_DETAILS_TASK = 2;
    public static final int SET_USER_DETAILS_TASK = 3;
    public static final int GET_INTERESTED_USER_TASK = 4;
    public static final int GET_ADDRESS_TASK = 5;
    public static final int TOGGLE_INTERESTED_USER_TASK = 6;
    public static final int SEARCH_TRIPS_TASK = 7;
    public static final int DELETE_TRIP_TASK = 8;
    public static final int GET_USER_INTERESTED_TRIP_TASK = 9;
    public static final int ADD_NEW_TRIP_TASK = 10;

    private static final String SERVER_IP = "192.168.1.82";//"csdev.nmmu.ac.za";
    private static final int SERVER_PORT = 8111;
    private static final int CONNECTION_TIMEOUT = 5000;

    static class AuthenticationRunner implements Runnable {

        private AuthenticationTask authTask;

        AuthenticationRunner(AuthenticationTask authTask) {
            this.authTask = authTask;
        }

        @Override
        public void run() {

            if (authTask.email.isEmpty() && authTask.password.isEmpty()) {
                authTask.authStatus = AUTHENTICATION_FAIL;
                authTask.HandleAuthentication();
            } else {
                Socket socket = null;

                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                    DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream readStream = new DataInputStream(socket.getInputStream());

                    writeStream.writeUTF(AUTHENTICATE);
                    writeStream.writeUTF(authTask.email);
                    writeStream.writeUTF(authTask.password);
                    writeStream.flush();

                    authTask.authStatus = readStream.readUTF();

                    if (authTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                        authTask.authKey = readStream.readUTF();
                    }

                    writeStream.writeUTF(QUIT_MESSAGE);

                    writeStream.close();
                    readStream.close();
                    socket.close();
                } catch (IOException e) {
                    Log.e("Comms | Authentication", "Error authenticating user " + authTask.email);
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

            public AuthenticationTask(String email, String password, LoginActivity callingAct) {
                this.email = email;
                this.password = password;
                this.LoginAct = callingAct;
            }

            public void HandleAuthentication() {
                LoginAct.HandleAuthentication(this);
            }

        }
    }

    static class RegisterRunner implements Runnable {

        RegistrationTask regTask;

        public RegisterRunner(RegistrationTask regTask) {
            this.regTask = regTask;
        }

        @Override
        public void run() {
            if (regTask.email.isEmpty() && regTask.password.isEmpty()) {
                regTask.authStatus = AUTHENTICATION_FAIL;
                regTask.HandleRegistration();
            } else {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                    DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream readStream = new DataInputStream(socket.getInputStream());

                    writeStream.writeUTF(REGISTER);
                    writeStream.writeUTF(regTask.email);
                    writeStream.writeUTF(regTask.password);
                    writeStream.flush();

                    regTask.authStatus = readStream.readUTF();

                    if (regTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                        regTask.authKey = readStream.readUTF();
                    }

                    writeStream.writeUTF(QUIT_MESSAGE);

                    writeStream.close();
                    readStream.close();
                    socket.close();
                } catch (IOException e) {
                    Log.e("Comms | Register", "Error Registering user " + regTask.email);
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

            public RegistrationTask(String email, String password, RegisterActivity registerActivity) {
                this.email = email;
                this.password = password;
                this.registerActivity = registerActivity;
            }

            public void HandleRegistration() {
                registerActivity.HandleRegistration(this);
            }
        }
    }

    static class PostedUserTripsRunner implements Runnable {

        UserPostedTripsTask tripsTask;

        public PostedUserTripsRunner(UserPostedTripsTask tripsTask) {
            this.tripsTask = tripsTask;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(GET_USER_POSTED_TRIPS);
                writeStream.writeUTF(tripsTask.authKey);
                writeStream.flush();

                tripsTask.authStatus = readStream.readUTF();

                if (tripsTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    int tripCount = readStream.readInt();
                    List<Trip> trips = new ArrayList<>();
                    for (int x = 0; x < tripCount; x++) {
                        Trip curTrip = new Trip();
                        curTrip.setTripID(readStream.readInt());
                        curTrip.setPickupLat(readStream.readDouble());
                        curTrip.setPickupLong(readStream.readDouble());
                        curTrip.setDestinationLat(readStream.readDouble());
                        curTrip.setDestinationLong(readStream.readDouble());
                        Timestamp pickUpTime = new Timestamp(readStream.readLong());
                        curTrip.setNumInterested(readStream.readInt());
                        curTrip.setPickupTime(pickUpTime);

                        trips.add(curTrip);
                    }

                    tripsTask.trips = trips;
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms | GetTrips", "Error Getting posted trips.");
                e.printStackTrace();
            }

            tripsTask.HandleUserTrips();
        }

        public static class UserPostedTripsTask {

            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public List<Trip> trips;

            public UserPostedTripsTask(String authKey, Handler handler) {
                this.authKey = authKey;
                this.handler = handler;
            }

            public void HandleUserTrips() {
                if (trips == null) {
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

                if (getUserDetailsTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
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
                e.printStackTrace();
            }

            getUserDetailsTask.handleGetUserDetails();
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

            public void handleGetUserDetails() {
                handler.obtainMessage(GET_USER_DETAILS_TASK, this).sendToTarget();
            }
        }
    }

    static class SetUserDetailsRunner implements Runnable {

        SetDetailsTask setDetailsTask;

        public SetUserDetailsRunner(SetDetailsTask setDetailsTask) {
            this.setDetailsTask = setDetailsTask;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(SET_USER_DETAILS);
                writeStream.writeUTF(setDetailsTask.authKey);
                writeStream.flush();

                setDetailsTask.authStatus = readStream.readUTF();

                if(setDetailsTask.authStatus.equals(AUTHENTICATION_SUCCESS)){
                    writeStream.writeUTF(setDetailsTask.name);
                    writeStream.writeUTF(setDetailsTask.surname);
                    writeStream.writeUTF(setDetailsTask.email);
                    writeStream.writeUTF(setDetailsTask.phone);
                    writeStream.writeUTF(String.valueOf(setDetailsTask.availableAsDriver));
                    writeStream.writeUTF(String.valueOf(setDetailsTask.numberOfPassengers));
                    writeStream.flush();
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();
            } catch (IOException e) {
                Log.e("Comms | SetUserDetails", "Error sending user details to server. AuthKey " + setDetailsTask.authKey);
                e.printStackTrace();
            }
            setDetailsTask.handleSetDetailsTask();
        }

        public static class SetDetailsTask {

            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public String name;
            public String surname;
            public String email;
            public String phone;
            public int availableAsDriver;
            public int numberOfPassengers;

            public SetDetailsTask(String authKey, Handler handler, String name, String surname, String phone, String email, int availableAsDriver, int numberOfPassengers) {
                this.authKey = authKey;
                this.handler = handler;
                this.name = name;
                this.surname = surname;
                this.phone = phone;
                this.email = email;
                this.availableAsDriver = availableAsDriver;
                this.numberOfPassengers = numberOfPassengers;
            }

            public void handleSetDetailsTask() {
                handler.obtainMessage(SET_USER_DETAILS_TASK, this).sendToTarget();
            }
        }
    }

    static class GetAddressFromLatLongRunner implements Runnable {

        private GetAddressTask getAddressTask;

        public GetAddressFromLatLongRunner(GetAddressTask getAddressTask) {
            this.getAddressTask = getAddressTask;
        }

        @Override
        public void run() {
            JsonReader reader = null;
            try {
                for (Pair<Double, Double> latlongPair : getAddressTask.latLongPairs) {
                    String url = String.format(Locale.US, "https://maps.googleapis.com/maps/api/geocode/json?latlng=%.6f,%.6f", latlongPair.first, latlongPair.second);
                    InputStream is = (InputStream) new URL(url).getContent();
                    reader = new JsonReader(new InputStreamReader(is, "UTF-8"));

                    reader.beginObject();
                    outerloop:
                    while (reader.hasNext()) {
                        String rootName = reader.nextName();
                        if (rootName.equals("results")) {
                            reader.beginArray();
                            reader.beginObject();
                            while (reader.hasNext()) {  //Read until first full address is found
                                String name = reader.nextName();
                                if (name.equals("formatted_address")) {
                                    getAddressTask.Addresses.add(reader.nextString());
                                    break outerloop;
                                }
                                reader.skipValue();
                            }
                            reader.endObject();
                            reader.endArray();
                        } else if (rootName.equals("status")) {
                            if (!reader.nextString().equals("OK")) {
                                getAddressTask.Addresses.add("No Natural Address Found.");
                            }
                        }

                    }
                    reader.close();
                }


                getAddressTask.Status = STATUS_SUCCESS;
            } catch (IOException e) {
                Log.e("Comms | GetAddress", "Error occurred during address retrieval.");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("Comms | GetAddress", "Error occurred during reading of json address.");
                e.printStackTrace();
            } finally {
                try {
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    Log.e("Comms | GetAddress", "Error occurred during closing of reader stream.");
                } finally {
                    getAddressTask.HandleGetAddressTask();
                }
            }
        }

        public static class GetAddressTask {

            private Handler handler;
            public List<Pair<Double, Double>> latLongPairs;
            public String Status = STATUS_FAILED;
            public List<String> Addresses = new ArrayList<>();

            /*
            coordinates should be entered lat first and long second. Multiple lat long values can be given
             */
            public GetAddressTask(Handler handler, List<Pair<Double, Double>> coordinates) {
                this.handler = handler;
                this.latLongPairs = coordinates;
            }

            public void HandleGetAddressTask() {
                handler.obtainMessage(GET_ADDRESS_TASK, this).sendToTarget();
            }
        }
    }

    static class GetInterestedUsersRunner implements Runnable {

        GetInterestedUsersTask getInterestedUsersTask;

        public GetInterestedUsersRunner(GetInterestedUsersTask getInterestedUsersTask) {
            this.getInterestedUsersTask = getInterestedUsersTask;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(GET_INTERESTED_USERS);
                writeStream.writeInt(getInterestedUsersTask.tripID);
                writeStream.flush();

                getInterestedUsersTask.authStatus = readStream.readUTF();

                if (getInterestedUsersTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    int numberIUsers = readStream.readInt();
                    for(int x = 0 ; x < numberIUsers ; x++) {
                        User curUser = new User();
                        curUser.setUserID(readStream.readInt());
                        curUser.setName(readStream.readUTF());
                        curUser.setSurname(readStream.readUTF());
                        curUser.setPassword(readStream.readUTF());
                        curUser.setEmail(readStream.readUTF());
                        curUser.setContactNum(readStream.readUTF());
                        curUser.setAvailableAsDriver(readStream.readInt());
                        curUser.setNumberOfPassengers(readStream.readInt());
                        getInterestedUsersTask.interestedUsers.add(curUser);
                    }
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms |GetInterestedUsr", "Error getting user interested users for tripID " + getInterestedUsersTask.tripID + ".");
                e.printStackTrace();
            }

            getInterestedUsersTask.handleGetInterestedUsers();
        }

        public static class GetInterestedUsersTask {
            public int tripID;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public List<User> interestedUsers = new ArrayList<>();

            public GetInterestedUsersTask(int tripID, Handler handler) {
                this.tripID = tripID;
                this.handler = handler;
            }

            public void handleGetInterestedUsers() {
                handler.obtainMessage(GET_INTERESTED_USER_TASK, this).sendToTarget();
            }
        }
    }

    static class ToggleInterestedUserRunner implements Runnable {

        ToggleInterestedUserTask toggleInterestedUserTask;

        public ToggleInterestedUserRunner(ToggleInterestedUserTask toggleInterestedUserTask) {
            this.toggleInterestedUserTask = toggleInterestedUserTask;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(INTERESTED_USER_TOGGLE);
                writeStream.writeUTF(toggleInterestedUserTask.authKey);
                writeStream.flush();
                toggleInterestedUserTask.authStatus = readStream.readUTF();
                if(toggleInterestedUserTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    writeStream.writeInt(toggleInterestedUserTask.tripIDs.size());
                    for (int tripID : toggleInterestedUserTask.tripIDs) {
                        writeStream.writeInt(tripID);
                    }
                    writeStream.flush();
                }
                writeStream.writeUTF(QUIT_MESSAGE);
                writeStream.close();
                readStream.close();
                socket.close();
            } catch (IOException e) {
                Log.e("Comms |TgLInterestedUsr", "Error sending trip favorites for AuthKey " + toggleInterestedUserTask.authKey + ".");
                e.printStackTrace();
            }

            toggleInterestedUserTask.handleToggleInterestedUser();
        }

        public static class ToggleInterestedUserTask {
            public Collection<Integer> tripIDs;
            public int toggleStatus;
            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public ToggleInterestedUserTask(String authKey, Collection<Integer> tripIDs, Handler handler) {
                this.authKey = authKey;
                this.tripIDs = tripIDs;
                this.handler = handler;
            }

            public void handleToggleInterestedUser() {
                handler.obtainMessage(TOGGLE_INTERESTED_USER_TASK, this).sendToTarget();
            }
        }
    }

    static class SearchTripsRunner implements Runnable {

        SearchTripsTask searchTripsTask;

        public SearchTripsRunner(SearchTripsTask searchTripsTask) {
            this.searchTripsTask = searchTripsTask;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(SEARCH_TRIPS);
                writeStream.writeUTF(searchTripsTask.authKey);
                writeStream.writeDouble(searchTripsTask.pickupLat);
                writeStream.writeDouble(searchTripsTask.pickupLong);
                writeStream.writeDouble(searchTripsTask.dropOffLat);
                writeStream.writeDouble(searchTripsTask.dropOffLong);
                writeStream.writeDouble(searchTripsTask.searchTolerance);
                writeStream.flush();

                searchTripsTask.authStatus = readStream.readUTF();

                if (searchTripsTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    int tripCount = readStream.readInt();
                    List<SearchedTrip> trips = new ArrayList<>();
                    for (int x = 0; x < tripCount; x++) {
                        SearchedTrip curTrip = new SearchedTrip();
                        curTrip.setTripID(readStream.readInt());
                        curTrip.setPickupLat(readStream.readDouble());
                        curTrip.setPickupLong(readStream.readDouble());
                        curTrip.setDestinationLat(readStream.readDouble());
                        curTrip.setDestinationLong(readStream.readDouble());
                        Timestamp pickUpTime = new Timestamp(readStream.readLong());
                        curTrip.setPickupTime(pickUpTime);
                        curTrip.setDistanceBetweenPickups(readStream.readDouble());
                        curTrip.setDistanceBetweenDropOffs(readStream.readDouble());

                        trips.add(curTrip);
                    }

                    searchTripsTask.searchedTripResults = trips;
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms | GetTrips", "Error Getting posted trips.");
                e.printStackTrace();
            }

            searchTripsTask.HandleSearchTripsTask();
        }

        public static class SearchTripsTask {

            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;
            double pickupLat;
            double pickupLong;
            double dropOffLat;
            double dropOffLong;
            double searchTolerance;

            public List<SearchedTrip> searchedTripResults;

            public SearchTripsTask(String authKey,double pickupLat, double pickupLong, double dropOffLat, double dropOffLong, double searchToleranceInKM,  Handler handler) {
                this.authKey = authKey;
                this.handler = handler;
                this.pickupLat = pickupLat;
                this.pickupLong = pickupLong;
                this.dropOffLat = dropOffLat;
                this.dropOffLong = dropOffLong;
                this.searchTolerance = searchToleranceInKM;
            }

            public void HandleSearchTripsTask() {
                handler.obtainMessage(SEARCH_TRIPS_TASK, this).sendToTarget();
            }
        }

    }

    static class DeleteTripRunner implements Runnable {

        DeleteTripTask deleteTripTask;

        public DeleteTripRunner(DeleteTripTask deleteTripTask) {
            this.deleteTripTask = deleteTripTask;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(DELETE_TRIP);
                writeStream.writeUTF(deleteTripTask.authKey);
                writeStream.writeInt(deleteTripTask.tripID);
                writeStream.flush();

                deleteTripTask.authStatus = readStream.readUTF();

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms | DelTrip", "Error Deleting trip.");
                e.printStackTrace();
            }

            deleteTripTask.HandleDeleteTripTask();
        }

        public static class DeleteTripTask {

            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;
            public int tripID;

            public DeleteTripTask(String authKey,int tripID,  Handler handler) {
                this.authKey = authKey;
                this.handler = handler;
                this.tripID = tripID;
            }

            public void HandleDeleteTripTask() {
                handler.obtainMessage(DELETE_TRIP_TASK, this).sendToTarget();
            }
        }

    }

    static class InterestedUserTripsRunner implements Runnable {

        UserInterestedTripsTask userInterestedTripsTask;

        public InterestedUserTripsRunner(UserInterestedTripsTask userInterestedTripsTask) {
            this.userInterestedTripsTask = userInterestedTripsTask;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(GET_USER_INTERESTED_TRIPS);
                writeStream.writeUTF(userInterestedTripsTask.authKey);
                writeStream.flush();

                userInterestedTripsTask.authStatus = readStream.readUTF();

                if (userInterestedTripsTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    int tripCount = readStream.readInt();
                    List<Trip> trips = new ArrayList<>();
                    for (int x = 0; x < tripCount; x++) {
                        Trip curTrip = new Trip();
                        curTrip.setTripID(readStream.readInt());
                        curTrip.setPickupLat(readStream.readDouble());
                        curTrip.setPickupLong(readStream.readDouble());
                        curTrip.setDestinationLat(readStream.readDouble());
                        curTrip.setDestinationLong(readStream.readDouble());
                        Timestamp pickUpTime = new Timestamp(readStream.readLong());
                        curTrip.setNumInterested(readStream.readInt());
                        curTrip.setPickupTime(pickUpTime);

                        trips.add(curTrip);
                    }

                    userInterestedTripsTask.trips = trips;
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms | GetTrips", "Error Getting interested trips.");
                e.printStackTrace();
            }

            userInterestedTripsTask.HandleUserInterestedTripsTask();
        }

        public static class UserInterestedTripsTask {

            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;

            public List<Trip> trips;

            public UserInterestedTripsTask(String authKey, Handler handler) {
                this.authKey = authKey;
                this.handler = handler;
            }

            public void HandleUserInterestedTripsTask() {
                handler.obtainMessage(GET_USER_INTERESTED_TRIP_TASK, this).sendToTarget();
            }
        }

    }

    static class AddNewTripRunner implements Runnable {

        AddNewTripTask addNewTripTask;

        public AddNewTripRunner(AddNewTripTask addNewTripTask) {
            this.addNewTripTask = addNewTripTask;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);

                DataOutputStream writeStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream readStream = new DataInputStream(socket.getInputStream());

                writeStream.writeUTF(POST_NEW_TRIP);
                writeStream.writeUTF(addNewTripTask.authKey);
                writeStream.flush();

                addNewTripTask.authStatus = readStream.readUTF();

                if (addNewTripTask.authStatus.equals(AUTHENTICATION_SUCCESS)) {
                    writeStream.writeUTF(String.valueOf(addNewTripTask.userTrip.getPickupLat()));
                    writeStream.writeUTF(String.valueOf(addNewTripTask.userTrip.getPickupLong()));
                    writeStream.writeUTF(String.valueOf(addNewTripTask.userTrip.getDestinationLat()));
                    writeStream.writeUTF(String.valueOf(addNewTripTask.userTrip.getDestinationLong()));
                    writeStream.writeUTF(String.valueOf(addNewTripTask.userTrip.getPickupTime().getTime()));
                }

                writeStream.writeUTF(QUIT_MESSAGE);

                writeStream.close();
                readStream.close();
                socket.close();


            } catch (IOException e) {
                Log.e("Comms | GetUserDetails", "Error getting user details.");
                e.printStackTrace();
            }

            addNewTripTask.handleAddNewTrip();
        }

        public static class AddNewTripTask {
            public String authKey;
            public String authStatus = AUTHENTICATION_INCOMPLETE;
            private Handler handler;
            private Trip userTrip;

            public AddNewTripTask(String authKey, Trip userTrip, Handler handler) {
                this.authKey = authKey;
                this.userTrip = userTrip;
                this.handler = handler;
            }

            public void handleAddNewTrip() {
                handler.obtainMessage(ADD_NEW_TRIP_TASK, this).sendToTarget();
            }
        }
    }
}
