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
            String name = readStream.readUTF();
            String surname = readStream.readUTF();
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            String contactNum = readStream.readUTF();
            String availableAsDriver = readStream.readUTF();
            String numberOfPassengers = readStream.readUTF();
            Boolean success = DatabaseHandler.RegisterUser(name,surname,password,email,contactNum,Integer.parseInt(availableAsDriver),Integer.parseInt(numberOfPassengers));
            writeStream.writeBoolean(success);
            writeStream.flush();
            System.out.println("CLIENT THREAD : Registering user '" + name + " " + surname);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void authenticate(){
        try {
            String email = readStream.readUTF();
            String password = readStream.readUTF();
            User userObj = DatabaseHandler.AuthenticateUser(email,password);
            if(userObj != null){
                writeStream.writeUTF(AUTHENTICATION_SUCCESS);
                writeStream.writeUTF(userObj.getName());
                writeStream.writeUTF(userObj.getSurname());
                writeStream.writeUTF(userObj.getEmail());
                writeStream.writeUTF(userObj.getPassword());
                writeStream.writeUTF(userObj.getContactNum());
                writeStream.writeUTF(userObj.getUserID().toString());
                writeStream.writeUTF(userObj.getAvailableAsDrive().toString());
                writeStream.writeUTF(userObj.getNumberOfPassengers().toString());
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
