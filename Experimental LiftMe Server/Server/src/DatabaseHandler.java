import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    static boolean UpdateUser(String authKey, String name, String surname, String password, String email, String contactNum, int availableAsDriver, int numberOfPassengers ){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean bSuccess = false;
        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to register user " + name + " " + surname + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String updateSQL = "UPDATE user SET name = ? , surname = ? , password = ?, email = ?, contactNum = ?, availableAsDriver = ?, numberOfPassengers = ? WHERE authenticationToken = ?;";
            stmt = conn.prepareStatement(updateSQL);

            stmt.setString(1,name);
            stmt.setString(2,surname);
            stmt.setString(3,password);
            stmt.setString(4,email);
            stmt.setString(5, contactNum);
            stmt.setInt(6, availableAsDriver);
            stmt.setInt(7, numberOfPassengers);
            stmt.setString(8,authKey);

            stmt.execute();

            stmt.close();
            conn.close();
            bSuccess = true;

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst updating user " + name + " " + surname + ".");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst updating user " + name + " " + surname + ".");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return bSuccess;
    }

    /**
     * Registers a user. If successful, returns an authentication key similar to when a user login.
     * @param password
     * @param email
     * @return
     */
    static String RegisterUser( String password, String email){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;

        String tempKey = null;
        String authKey = null;
        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to register user " + email + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));
            tempKey = GenerateAuthKey();

            String registerSql = "INSERT INTO user ( password, email , authenticationToken) VALUES ( ? , ? , ? );";
            stmt = conn.prepareStatement(registerSql);

            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.setString(3, tempKey);

            stmt.execute();

            stmt.close();
            conn.close();

            authKey = tempKey;

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst registering " + email + ".");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst registering " + email + ".");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return authKey;
    }

    /**
     * Gets a user's details based on their authentication key.
     * @param authKey Key returned when a user successfully authenticates.
     * @return
     */
    static User GetUserDetails(String authKey){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        User userObj = null;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to get user details " + authKey + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String authSql = "SELECT * FROM user WHERE authenticationToken = ?";
            stmt = conn.prepareStatement(authSql);

            stmt.setString(1, authKey);

            ResultSet userSet = stmt.executeQuery();


            if(userSet.next()){
                userObj = new User();

                userObj.setName(userSet.getString("name"));
                userObj.setSurname(userSet.getString("surname"));
                userObj.setEmail(userSet.getString("email"));
                userObj.setAvailableAsDriver(userSet.getInt("availableAsDriver"));
                userObj.setContactNum(userSet.getString("contactNum"));
                userObj.setUserID(userSet.getInt("userID"));
                userObj.setNumberOfPassengers(userSet.getInt("numberOfPassengers"));
                userObj.setPassword(userSet.getString("password"));
            }

            stmt.close();
            conn.close();

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst getting user details with " + authKey + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst getting user details with " + authKey + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return userObj;
    }

    /**
     * Checks whether the user's email and password matches in the database and returns a authentication key if so.
     * @param email
     * @param password
     * @return
     */
    static String AuthenticateUser(String email, String password){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        User userObj = null;
        String authKey = null;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to Authenticate user " + email + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String authSql = "SELECT * FROM user WHERE email = ?";
            stmt = conn.prepareStatement(authSql);

            stmt.setString(1, email);

            ResultSet userSet = stmt.executeQuery();


            if(userSet.next()){
                userObj = new User();

                userObj.setUserID(userSet.getInt("userID"));
                userObj.setPassword(userSet.getString("password"));

                if(userObj.getPassword().equals(password)){
                    authKey = GenerateAuthKey();
                    stmt.close();

                    String insertAuth = "UPDATE user SET authenticationToken = '" + authKey + "' WHERE userID = " + userObj.getUserID() + ";";
                    Statement insertStmt = conn.createStatement();

                    insertStmt.execute(insertAuth);

                    insertStmt.close();
                }
            }

            stmt.close();
            conn.close();
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
        return authKey;
    }

    static String GenerateAuthKey(){
        String authKey = UUID.randomUUID().toString().toUpperCase();
        return authKey;
    }

    static List<Trip> GetUserPostedTrips(String authKey){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;

        List<Trip> trips = null;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to retrieve trips for authKey " + authKey + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String authSql = "SELECT * FROM trip WHERE userID = (SELECT userID FROM user WHERE authenticationToken = ?)";
            stmt = conn.prepareStatement(authSql);

            stmt.setString(1, authKey);

            ResultSet tripSet = stmt.executeQuery();

            trips = new ArrayList<>();
            while(tripSet.next()){
                Trip curTrip = new Trip();
                curTrip.setPickupLat(tripSet.getFloat("pickUpLat"));
                curTrip.setPickupLong(tripSet.getFloat("pickUpLong"));
                curTrip.setDestinationLat(tripSet.getFloat("dropOffLat"));
                curTrip.setDestinationLong(tripSet.getFloat("dropOffLong"));
                curTrip.setPickupTime(tripSet.getTime("pickUpTime"));
                curTrip.setDropOffTime(tripSet.getTime("dropOffTime"));
                curTrip.setDate(tripSet.getDate("date"));
                trips.add(curTrip);
            }

            stmt.close();
            conn.close();
        }catch(SQLException e){
            System.out.println("SQL error occurred whilst retrieving trips for authKey " + authKey + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst retrieving trips for authKey " + authKey + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return trips;
    }

}
