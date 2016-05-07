package fnm.wrmc.nmmu.liftme;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by Francois on 2016/04/13.
 */
public class Trip implements Serializable {

    private int tripID;
    private double pickupLat;
    private double pickupLong;
    private double destinationLat;
    private double destinationLong;
    private Timestamp pickupTime;

    public Trip(){

    }

    public Timestamp getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Timestamp pickupTime) {
        this.pickupTime = pickupTime;
    }

    public double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public double getPickupLong() {
        return pickupLong;
    }

    public void setPickupLong(double pickupLong) {
        this.pickupLong = pickupLong;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(double destinationLong) {
        this.destinationLong = destinationLong;
    }

    public String getDayOfWeek(){
        if(pickupTime == null){
            return "";
        }

        SimpleDateFormat formatter = new SimpleDateFormat(
                "EEEE");
        return formatter.format(pickupTime);
    }

    public String getTime(){
        if(pickupTime == null){
            return "";
        }

        SimpleDateFormat formatter = new SimpleDateFormat(
                "HH:mm");
        return formatter.format(pickupTime);
    }

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
    }

}
