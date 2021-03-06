import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by s213263904 on 2015/10/16.
 */
public class mainServer {

    public static void main(String args[]) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        new mainServer();
    }

    private Map<String,ClientConnectionThread> currentConnections;

    public mainServer(){
        ServerSocket serverSocket = null;
        currentConnections = new HashMap<>();

        try{
            serverSocket = new ServerSocket(8111);
            System.out.println("SERVER Thread: Server started Listening");
            while(true){

                Socket newSocket = serverSocket.accept();
                ClientConnectionThread clientConnection = new ClientConnectionThread(this,newSocket);
                clientConnection.start();

                System.out.println("SERVER Thread: New Client Connected");

            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                }catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
