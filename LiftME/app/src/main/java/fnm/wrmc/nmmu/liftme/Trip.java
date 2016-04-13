package fnm.wrmc.nmmu.liftme;

/**
 * Created by Francois on 2016/04/13.
 */
public class Trip {

    private float pickupLat;
    private float pickupLong;
    private float destinationLat;
    private float destinationLong;

    public Trip(){

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
