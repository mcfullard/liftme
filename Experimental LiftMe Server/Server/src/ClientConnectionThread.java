import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by s213263904 on 2015/10/16.
 */
public class ClientConnectionThread extends Thread {

    private mainServer theServer;
    private Socket socket = null;
    private DataInputStream readStream = null;
    private DataOutputStream writeStream = null;
    private final String QUIT_MESSAGE = "#QUIT";
    private final String REGISTER = "#REGISTER";
    private final String AUTHENTICATE = "#AUTHENTICATE";
    private final String AUTHENTICATION_FAIL = "#NOAUTH";
    private final String AUTHENTICATION_SUCCESS = "#YESAUTH";
    private final String GET_USER_POSTED_TRIPS = "#GET_USER_POSTED_TRIPS";
    private final String GET_USER_DETAILS = "#GET_USER_DETAILS";
    private final String SET_USER_DETAILS = "#SET_USER_DETAILS";
    private final String GET_INTERESTED_USERS = "#GET_INTERESTED_USERS";
    private final String INTERESTED_USER_TOGGLE = "#INTERESTED_USER_TOGGLE";

    public ClientConnectionThread(mainServer theServer, Socket newSocket) {
        this.theServer = theServer;
        this.socket = newSocket;
    }

    @Override
    public void run() {
        try {
            readStream = new DataInputStream(socket.getInputStream());
            writeStream = new DataOutputStream(socket.getOutputStream());

            String response = readStream.readUTF();
            while (!response.equals(QUIT_MESSAGE)) {

                switch (response) {
                    case REGISTER:
                        Register();
                        break;
                    case AUTHENTICATE:
                        authenticate();
                        break;
                    case GET_USER_POSTED_TRIPS:
                        GetUserPostedTrips();
                        break;
                    case GET_USER_DETAILS:
                        getUserDetails();
                        break;
                    case SET_USER_DETAILS:
                        setUserDetails();
                        break;
                    case GET_INTERESTED_USERS:
                        GetInterestedUsers();
                        break;
                    case INTERESTED_USER_TOGGLE:
                        InterestedUserToggle();
                        break;
                }

                response = readStream.readUTF();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                readStream.close();
                writeStream.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing client connections.");
            }
        }
    }

    private void setUserDetails() {
        try {
            String authKey = readStream.readUTF();
            User user = DatabaseHandler.GetUserDetails(authKey);
            if (user != null) {
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.flush();
                user.setName(readStream.readUTF());
                user.setSurname(readStream.readUTF());
                user.setEmail(readStream.readUTF());
                user.setContactNum(readStream.readUTF());
                user.setAvailableAsDriver(Integer.parseInt(readStream.readUTF()));
                user.setNumberOfPassengers(Integer.parseInt(readStream.readUTF()));
                DatabaseHandler.UpdateUser(user, authKey);
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
                writeStream.flush();
            }
            System.out.println("CLIENT THREAD : Setting user details. AuthKey " + authKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserDetails() {
        try {
            String authKey = readStream.readUTF();
            User user = DatabaseHandler.GetUserDetails(authKey);
            if (user != null) {
                String name = user.getName();
                String surname = user.getSurname();
                String email = user.getEmail();
                String phone = user.getContactNum();
                Integer avail = user.getAvailableAsDriver();
                Integer passengers = user.getNumberOfPassengers();

                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeUTF(name != null ? name : "");
                writeStream.writeUTF(surname != null ? surname : "");
                writeStream.writeUTF(email != null ? email : "");
                writeStream.writeUTF(phone != null ? phone : "");
                writeStream.writeUTF(avail != null ? avail.toString() : "0");
                writeStream.writeUTF(passengers != null ? passengers.toString() : "0");
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Getting user details. AuthKey " + authKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Register() {
        try {
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            String authKey = DatabaseHandler.RegisterUser(password, email);

            if (authKey != null) {
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeUTF(authKey);
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }

            writeStream.flush();
            System.out.println("CLIENT THREAD : Registering user " + email + ". AuthKey: " + authKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        try {
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            String authKey = DatabaseHandler.AuthenticateUser(email, password);
            if (authKey != null) {
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeUTF(authKey);
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Authenticating user " + email);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void GetUserPostedTrips() {
        try {
            String authKey = readStream.readUTF();

            List<Trip> userTrips = DatabaseHandler.GetUserPostedTrips(authKey);

            if (userTrips != null) {
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeInt(userTrips.size());
                for (Trip curTrip : userTrips) {
                    writeStream.writeInt(curTrip.getTripID());
                    writeStream.writeDouble(curTrip.getPickupLat());
                    writeStream.writeDouble(curTrip.getPickupLong());
                    writeStream.writeDouble(curTrip.getDestinationLat());
                    writeStream.writeDouble(curTrip.getDestinationLong());
                    writeStream.writeLong(curTrip.getPickupTime().getTime());
                }

            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Getting user posted trips. AuthKey " + authKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void GetInterestedUsers() {
        try {
            int tripID = readStream.readInt();

            List<User> interestedUsers = DatabaseHandler.GetInterestedUsersOfTrip(tripID);

            if (interestedUsers != null) {
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeInt(interestedUsers.size());
                for (User curUser : interestedUsers) {
                    writeStream.writeInt(curUser.getUserID());
                    writeStream.writeUTF(curUser.getName());
                    writeStream.writeUTF(curUser.getSurname());
                    writeStream.writeUTF(curUser.getPassword());
                    writeStream.writeUTF(curUser.getEmail());
                    writeStream.writeUTF(curUser.getContactNum());
                    writeStream.writeInt(curUser.getAvailableAsDriver());
                    writeStream.writeInt(curUser.getNumberOfPassengers());
                }
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Getting interested users for tripID " + tripID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void InterestedUserToggle() {
        try {
            String authKey = readStream.readUTF();
            int tripID = readStream.readInt();

            int interestedUsers = DatabaseHandler.InterestedUserToggle(authKey, tripID);

            if (interestedUsers != 0) {
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeInt(interestedUsers);
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Getting interested users for tripID " + tripID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
