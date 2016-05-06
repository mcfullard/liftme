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
    private final String UPDATE_DETAILS = "#UPDATE_DETAILS";
    private final String GET_USER_POSTED_TRIPS = "#GET_USER_POSTED_TRIPS";
    private final String GET_USER_DETAILS = "#GET_USER_DETAILS";

    public ClientConnectionThread(mainServer theServer,Socket newSocket){
        this.theServer = theServer;
        this.socket = newSocket;
    }

    @Override
    public void run() {
        try{
            readStream = new DataInputStream(socket.getInputStream());
            writeStream = new DataOutputStream(socket.getOutputStream());

            String response = readStream.readUTF();
            while(!response.equals(QUIT_MESSAGE)){

                switch (response){
                    case REGISTER:
                        Register();
                        break;
                    case AUTHENTICATE:
                        authenticate();
                        break;
                    case UPDATE_DETAILS:
                        UpdateDetails();
                        break;
                    case GET_USER_POSTED_TRIPS:
                        GetUserPostedTrips();
                        break;
                    case GET_USER_DETAILS:
                        getUserDetails();
                        break;
                }

                response = readStream.readUTF();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try {
                readStream.close();
                writeStream.close();
                socket.close();
            }catch(IOException e){
                System.out.println("Error closing client connections.");
            }
        }
    }

    private void getUserDetails() {
        try {
            String authKey = readStream.readUTF();
            User user = DatabaseHandler.GetUserDetails(authKey);
            if(user != null) {
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
                writeStream.writeUTF(avail != null ? avail.toString() : "");
                writeStream.writeUTF(passengers != null ? passengers.toString() : "");
            } else {
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Getting user details. AuthKey " + authKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Register(){
        try {
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            String authKey = DatabaseHandler.RegisterUser(password,email);

            if(authKey != null){
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeUTF(authKey);
            }else{
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }

            writeStream.flush();
            System.out.println("CLIENT THREAD : Registering user " + email);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void UpdateDetails(){
        try {
            String authKey = readStream.readUTF();
            String name = readStream.readUTF();
            String surname = readStream.readUTF();
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            String contactNum = readStream.readUTF();
            String availableAsDriver = readStream.readUTF();
            String numberOfPassengers = readStream.readUTF();
            Boolean success = DatabaseHandler.UpdateUser(authKey , name, surname, password, email, contactNum, Integer.parseInt(availableAsDriver), Integer.parseInt(numberOfPassengers));
            writeStream.writeBoolean(success);
            writeStream.flush();
            System.out.println("CLIENT THREAD : Updating user '" + name + " " + surname);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void authenticate(){
        try {
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            String authKey = DatabaseHandler.AuthenticateUser(email, password);
            if(authKey != null){
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeUTF(authKey);
            }else{
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Authenticating user " + email);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void GetUserPostedTrips(){
        try {
            String authKey = readStream.readUTF();

            List<Trip> userTrips = DatabaseHandler.GetUserPostedTrips(authKey);

            if(userTrips != null){
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeInt(userTrips.size());
                for(Trip curTrip : userTrips){
                    writeStream.writeFloat(curTrip.getPickupLat());
                    writeStream.writeFloat(curTrip.getPickupLong());
                    writeStream.writeFloat(curTrip.getDestinationLat());
                    writeStream.writeFloat(curTrip.getDestinationLong());
                    writeStream.writeLong(curTrip.getPickupTime().getTime());
                    writeStream.writeLong(curTrip.getDropOffTime().getTime());
                    writeStream.writeLong(curTrip.getDate().getTime());
                }

            }else{
                writeStream.writeUTF(AUTHENTICATION_FAIL);
            }
            writeStream.flush();
            System.out.println("CLIENT THREAD : Getting user posted trips. AuthKey " + authKey);
        }catch(IOException e){
            e.printStackTrace();
        }
    }



}
