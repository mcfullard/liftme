import java.sql.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Francois on 2016/03/24.
 */
public class DatabaseHandler {

    static private Lock writeLock = new ReentrantLock();

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/liftme";

    //  Database credentials
    static final String USER = "root";
    static final String PASSWORD = "1678425";

    static boolean RegisterUser(String name, String surname, String password, String email, String contactNum, int availableAsDriver, int numberOfPassangers ){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean bSuccess = false;
        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to register user " + name + " " + surname + ".");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            String registerSql = "INSERT INTO user ( name, surname, password, email, contactNum, availableAsDriver, numberOfPassengers) VALUES ( ? , ? , ? , ? , ? , ? , ? );";
            stmt = conn.prepareStatement(registerSql);

            stmt.setString(1,name);
            stmt.setString(2,surname);
            stmt.setString(3,password);
            stmt.setString(4,email);
            stmt.setString(5, contactNum);
            stmt.setInt(6,availableAsDriver);
            stmt.setInt(7,numberOfPassangers);

            stmt.execute();

            bSuccess = true;

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst registering " + name + " " + surname + ".");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst registering " + name + " " + surname + ".");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return bSuccess;
    }

    /**
     * Authenticates a user. Returns a user object containing al relative user info else returns null if no user or password mismatch
     * @param email
     * @param password
     * @return
     */
    static User AuthenticateUser(String email, String password){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        User userObj = null;

        boolean bSuccess = false;
        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to Authenticate user " + email + ".");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            String registerSql = "SELECT * FROM user WHERE email = ?";
            stmt = conn.prepareStatement(registerSql);

            stmt.setString(1, email);

            ResultSet userSet = stmt.executeQuery();


            if(userSet.next()){
                userObj = new User();

                userObj.setName(userSet.getString("name"));
                userObj.setSurname(userSet.getString("surname"));
                userObj.setEmail(email);
                userObj.setAvailableAsDrive(userSet.getInt("availableAsDriver"));
                userObj.setContactNum(userSet.getString("contactNum"));
                userObj.setUserID(userSet.getInt("userID"));
                userObj.setNumberOfPassengers(userSet.getInt("numberOfPassengers"));
                userObj.setPassword(userSet.getString("password"));

                if(!userObj.getPassword().equals(password)){
                    userObj = null;
                }
            }

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst authenticating " + email + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst authenticating " + email + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return userObj;
    }

}
