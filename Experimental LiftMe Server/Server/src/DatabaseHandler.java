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
    static final String DB_URL = "jdbc:mysql://postgrad.nmmu.ac.za:3306/liftme";

    static final double TRIP_SEARCH_DB_TOLERANCE = 0.5;

    static boolean UpdateUser(User user, String authKey){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean bSuccess = false;
        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to register user " + user.getName() + " " + user.getSurname() + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String updateSQL = "UPDATE user SET name = ? , surname = ? , password = ?, email = ?, contactNum = ?, availableAsDriver = ?, numberOfPassengers = ? WHERE authenticationToken = ?;";
            stmt = conn.prepareStatement(updateSQL);

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getSurname());
            stmt.setString(3,user.getPassword());
            stmt.setString(4,user.getEmail());
            stmt.setString(5, user.getContactNum());
            stmt.setInt(6, user.getAvailableAsDriver());
            stmt.setInt(7, user.getNumberOfPassengers());
            stmt.setString(8,authKey);

            stmt.execute();

            stmt.close();
            conn.close();
            bSuccess = true;

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst updating user " + user.getName() + " " + user.getSurname() + ".");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst updating user " + user.getName() + " " + user.getSurname() + ".");
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

            String authSql = "SELECT curTrip.*, (SELECT COUNT(*) FROM interesteduser WHERE  interesteduser.tripID = curTrip.tripID) AS numinterested FROM trip AS curTrip WHERE curTrip.userID = (SELECT userID FROM user WHERE authenticationToken = ?);";
            stmt = conn.prepareStatement(authSql);

            stmt.setString(1, authKey);

            ResultSet tripSet = stmt.executeQuery();

            trips = new ArrayList<>();
            while(tripSet.next()){
                Trip curTrip = new Trip();
                curTrip.setTripID(tripSet.getInt("tripID"));
                curTrip.setPickupLat(tripSet.getDouble("pickUpLat"));
                curTrip.setPickupLong(tripSet.getDouble("pickUpLong"));
                curTrip.setDestinationLat(tripSet.getDouble("dropOffLat"));
                curTrip.setDestinationLong(tripSet.getDouble("dropOffLong"));
                curTrip.setPickupTime(tripSet.getTimestamp("pickUpTime"));
                curTrip.setNumInterested(tripSet.getInt("numinterested"));
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

    static List<User> GetInterestedUsersOfTrip(int tripID){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;

        List<User> interestedUsers = null;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to retrieve interested users for tripID: " + tripID + ";");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String interUserSQL = "SELECT user.* FROM user,interesteduser WHERE interesteduser.tripID = ? AND user.userID = interesteduser.userID;";
            stmt = conn.prepareStatement(interUserSQL);

            stmt.setInt(1, tripID);

            ResultSet interUserSet = stmt.executeQuery();

            interestedUsers = new ArrayList<>();
            while(interUserSet.next()){
                User curUser = new User();
                curUser.setUserID(interUserSet.getInt("userID"));
                curUser.setName(interUserSet.getString("name"));
                curUser.setSurname(interUserSet.getString("surname"));
                curUser.setPassword("");
                curUser.setEmail(interUserSet.getString("email"));
                curUser.setContactNum(interUserSet.getString("contactNum"));
                curUser.setAvailableAsDriver(interUserSet.getByte("availableAsDriver") == 0 ? 0 : 1);
                curUser.setNumberOfPassengers(interUserSet.getInt("numberOfPassengers"));
                interestedUsers.add(curUser);
            }

            stmt.close();
            conn.close();
        }catch(SQLException e){
            System.out.println("SQL error occurred whilst retrieving interested users for tripID " + tripID + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst retrieving interested users for tripID " + tripID + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return interestedUsers;
    }

    static int InterestedUserToggle(String authKey,int tripID){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        int affectedRows = 0;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to toggel interested user for tripID: " + tripID + ";");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String checkInterStatusSQL = "SELECT * FROM interesteduser WHERE tripID = ? AND userID = (SELECT userID FROM user WHERE authenticationToken = ?);";
            stmt = conn.prepareStatement(checkInterStatusSQL);

            stmt.setInt(1, tripID);
            stmt.setString(2, authKey);

            ResultSet resultSet = stmt.executeQuery();

            if(resultSet.next()){
                String deleteInterSQL = "DELETE FROM interesteduser WHERE tripID = ? AND userID = (SELECT userID FROM user WHERE authenticationToken = ?);";
                stmt = conn.prepareStatement(deleteInterSQL);
                stmt.setInt(1, tripID);
                stmt.setString(2, authKey);
                affectedRows = stmt.executeUpdate();
                if(affectedRows > 0){
                    affectedRows = 1;
                }
            }else{
                String insertInterSQL = "INSERT INTO interesteduser (userID,tripID) VALUES ((SELECT userID FROM user WHERE authenticationToken = ?),?);";
                stmt = conn.prepareStatement(insertInterSQL);
                stmt.setString(1, authKey);
                stmt.setInt(2, tripID);
                affectedRows = stmt.executeUpdate();
                if(affectedRows > 0){
                    affectedRows = 2;
                }
            }

            stmt.close();
            conn.close();
        }catch(SQLException e){
            System.out.println("SQL error occurred whilst toggling interested user for tripID " + tripID + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst toggling interested user for tripID " + tripID + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return affectedRows;
    }

    static List<SearchedTrip> SearchTripInDatabase(String authKey, double pickupLat, double pickupLong, double dropOffLat, double dropOffLong,double searchToleranceInKM){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;

        List<SearchedTrip> searchTrips = null;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to retrieve search results for user : " + authKey + ";");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String getUserIDSQL = "SELECT userID FROM user WHERE authenticationToken = ?;";
            stmt = conn.prepareStatement(getUserIDSQL);
            stmt.setString(1, authKey);

            ResultSet userResult = stmt.executeQuery();
            if(userResult.next()) {
                int userID = userResult.getInt("userID");

                String localizedTripSql = "SELECT curTrip.* FROM trip AS curTrip WHERE curTrip.pickUpLat <= ? AND curTrip.pickUpLat >= ? AND curTrip.pickUpLong <= ? AND curTrip.pickUpLong >= ?" +
                                            "AND curTrip.dropOffLat <= ? AND curTrip.dropOffLat >= ? AND curTrip.dropOffLong <= ? AND curTrip.dropOffLong >= ? AND curTrip.userID != ? " +
                                                "AND (? NOT IN (SELECT interesteduser.userID FROM interesteduser WHERE tripID = curTrip.tripID)) ;";
                stmt = conn.prepareStatement(localizedTripSql);

                stmt.setDouble(1, pickupLat + TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(2, pickupLat - TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(3, pickupLong + TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(4, pickupLong - TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(5, dropOffLat + TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(6, dropOffLat - TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(7, dropOffLong + TRIP_SEARCH_DB_TOLERANCE);
                stmt.setDouble(8, dropOffLong - TRIP_SEARCH_DB_TOLERANCE);
                stmt.setInt(9, userID);
                stmt.setInt(10,userID);

                ResultSet matchingSearchSet = stmt.executeQuery();

                List<Trip> searchResults = new ArrayList<>();
                while (matchingSearchSet.next()) {
                    Trip curTrip = new Trip();
                    curTrip.setTripID(matchingSearchSet.getInt("tripID"));
                    curTrip.setPickupLat(matchingSearchSet.getDouble("pickUpLat"));
                    curTrip.setPickupLong(matchingSearchSet.getDouble("pickUpLong"));
                    curTrip.setDestinationLat(matchingSearchSet.getDouble("dropOffLat"));
                    curTrip.setDestinationLong(matchingSearchSet.getDouble("dropOffLong"));
                    curTrip.setPickupTime(matchingSearchSet.getTimestamp("pickUpTime"));
                    searchResults.add(curTrip);
                }

                searchTrips = DetermineTripDistance(searchResults, pickupLat, pickupLong, dropOffLat, dropOffLong, searchToleranceInKM);

            }

            stmt.close();
            conn.close();
        }catch(SQLException e){
            System.out.println("SQL error occurred whilst querying search for " + authKey + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst querying search for " + authKey + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }

        return searchTrips;
    }

    private static List<SearchedTrip> DetermineTripDistance(List<Trip> trips, double pickupLat, double pickupLong, double dropOffLat, double dropOffLong,double searchToleranceInKM){

        List<SearchedTrip> searchTrips = new ArrayList<>();
        for(Trip curTrip : trips) {
            double distanceBetweenPickups = HaversineFormula(pickupLat,curTrip.getPickupLat(),pickupLong,curTrip.getPickupLong());
            double distanceBetweenDropOffs = HaversineFormula(dropOffLat,curTrip.getDestinationLat(),dropOffLong,curTrip.getDestinationLong());

            if(distanceBetweenPickups <= searchToleranceInKM && distanceBetweenDropOffs <= searchToleranceInKM){
                searchTrips.add(new SearchedTrip(curTrip,distanceBetweenPickups,distanceBetweenDropOffs));
            }
        }

        return searchTrips;
    }

    private static double HaversineFormula(double lat1, double lat2, double long1, double long2){
        double earthRadiusInKM = 6371;
        double angle1 = Math.toRadians(lat1);
        double angle2 = Math.toRadians(lat2);
        double deltaAngle1 = Math.toRadians(lat2 - lat1);
        double deltaAngle2 = Math.toRadians(long2 - long1);

        double a = Math.pow(Math.sin(deltaAngle1/2),2) + Math.cos(angle1) * Math.cos(angle2) * Math.pow(Math.sin(deltaAngle2/2),2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        double distance = earthRadiusInKM*c;

        return distance;
    }

    static boolean DeleteTrip(String authKey,int tripID){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        User userObj = null;
        boolean deleteSuccess = false;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to delete trip for " + authKey + " tripID " + tripID + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String deleteTripSQL = "DELETE FROM trip WHERE tripID = ? AND userID = (SELECT userID FROM user WHERE authenticationToken = ?);";
            stmt = conn.prepareStatement(deleteTripSQL);

            stmt.setInt(1,tripID);
            stmt.setString(2, authKey);

            int resultCount = stmt.executeUpdate();

            if(resultCount > 0){
                deleteSuccess = true;
            }

            stmt.close();
            conn.close();

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst deleting trip " + tripID + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst deleting trip " + tripID  + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return deleteSuccess;
    }

    static List<Trip> GetUserInterestedTrips(String authKey){
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;

        List<Trip> trips = null;

        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to retrieve interested trips for authKey " + authKey + ".");
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String authSql = "SELECT curTrip.*, (SELECT COUNT(*) FROM interesteduser WHERE  interesteduser.tripID = curTrip.tripID) AS numinterested " +
                    "FROM interesteduser,trip AS curTrip " +
                    "WHERE interesteduser.userID = (SELECT userID FROM user WHERE authenticationToken = ?) AND curTrip.tripID = interesteduser.tripID;";
            stmt = conn.prepareStatement(authSql);

            stmt.setString(1, authKey);

            ResultSet tripSet = stmt.executeQuery();

            trips = new ArrayList<>();
            while(tripSet.next()){
                Trip curTrip = new Trip();
                curTrip.setTripID(tripSet.getInt("tripID"));
                curTrip.setPickupLat(tripSet.getDouble("pickUpLat"));
                curTrip.setPickupLong(tripSet.getDouble("pickUpLong"));
                curTrip.setDestinationLat(tripSet.getDouble("dropOffLat"));
                curTrip.setDestinationLong(tripSet.getDouble("dropOffLong"));
                curTrip.setPickupTime(tripSet.getTimestamp("pickUpTime"));
                curTrip.setNumInterested(tripSet.getInt("numinterested"));
                trips.add(curTrip);
            }

            stmt.close();
            conn.close();
        }catch(SQLException e){
            System.out.println("SQL error occurred whilst retrieving interested trips for authKey " + authKey + " .");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst retrieving interested trips for authKey " + authKey + " .");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
        return trips;
    }

    public static void postNewTrip(User user, Trip userTrip) {
        writeLock.lock();
        Connection conn = null;
        PreparedStatement stmt = null;
        try{

            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database to post new trip. Email: " + user.getEmail());
            PropertyManager pm = PropertyManager.getInstance();
            conn = DriverManager.getConnection(DB_URL, pm.getProperty("USER"), pm.getProperty("PASSWORD"));

            String registerSql = "INSERT INTO trip (pickUpLat, pickUpLong, dropOffLat, dropOffLong, pickUpTime, userID) VALUES (?, ?, ?, ?, ?, ?);";
            stmt = conn.prepareStatement(registerSql);

            stmt.setDouble(1, userTrip.getPickupLat());
            stmt.setDouble(2, userTrip.getPickupLong());
            stmt.setDouble(3, userTrip.getDestinationLat());
            stmt.setDouble(4, userTrip.getDestinationLong());
            stmt.setTimestamp(5, userTrip.getPickupTime());
            stmt.setInt(6, user.getUserID());

            stmt.execute();

            stmt.close();
            conn.close();

        }catch(SQLException e){
            System.out.println("SQL error occurred whilst registering " + user.getEmail() + ".");
            System.out.println(e);
        }catch(Exception e){
            System.out.println("Unexpected error occurred whilst registering " + user.getEmail() + ".");
            System.out.println(e);
        }
        finally {
            writeLock.unlock();
        }
    }
}
