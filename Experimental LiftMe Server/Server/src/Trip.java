import java.sql.Date;
import java.sql.Time;

/**
 * Created by Francois on 2016/04/13.
 */
public class Trip {

    private float pickupLat;
    private float pickupLong;
    private float destinationLat;
    private float destinationLong;
    private Time pickupTime;
    private Time dropOffTime;
    private Date date;

    public Trip(){

    }

    public Time getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Time pickupTime) {
        this.pickupTime = pickupTime;
    }

    public Time getDropOffTime() {
        return dropOffTime;
    }

    public void setDropOffTime(Time dropOffTime) {
        this.dropOffTime = dropOffTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(float pickupLat) {
        this.pickupLat = pickupLat;
    }

    public float getPickupLong() {
        return pickupLong;
    }

    public void setPickupLong(float pickupLong) {
        this.pickupLong = pickupLong;
    }

    public float getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(float destinationLat) {
        this.destinationLat = destinationLat;
    }

    public float getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(float destinationLong) {
        this.destinationLong = destinationLong;
    }
}
