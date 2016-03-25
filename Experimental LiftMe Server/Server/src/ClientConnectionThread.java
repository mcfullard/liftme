import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
            String authKey = DatabaseHandler.AuthenticateUser(email,password);
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

//    public void sendMessageTo(){
//        try {
//            String toClientName = readStream.readUTF();
//            String Message = readStream.readUTF();
//            System.out.println("CLIENT THREAD : Sending message '" + Message + "' to " + toClientName);
//            theServer.sendMessage(toClientName,clientName,Message);
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }
//
//    public void receiveMessageFrom(String fromName, String Message){
//        try{
//            writeStream.writeUTF(MESSAGE_FROM);
//            writeStream.writeUTF(fromName);
//            writeStream.writeUTF(Message);
//            writeStream.flush();
//            System.out.println("CLIENT THREAD : Receiving message '" + Message + "' from " + fromName);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }


}
