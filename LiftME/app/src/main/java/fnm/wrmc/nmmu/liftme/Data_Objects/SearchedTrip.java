package fnm.wrmc.nmmu.liftme.Data_Objects;

/**
 * Created by Francois on 2016/05/08.
 */
public class SearchedTrip extends Trip{

    double distanceBetweenPickups;
    double distanceBetweenDropOffs;

    public SearchedTrip(){
    }

    public SearchedTrip(Trip trip,double distanceBetweenPickups, double distanceBetweenDropOffs){
        this.setTripID(trip.getTripID());
        this.setPickupLat(trip.getPickupLat());
        this.setPickupLong(trip.getPickupLong());
        this.setDestinationLat(trip.getDestinationLat());
        this.setDestinationLong(trip.getDestinationLong());
        this.setPickupTime(trip.getPickupTime());
        this.distanceBetweenPickups = distanceBetweenPickups;
        this.distanceBetweenDropOffs = distanceBetweenDropOffs;
    }

    public double getDistanceBetweenPickups() {
        return distanceBetweenPickups;
    }

    public void setDistanceBetweenPickups(double distanceBetweenPickups) {
        this.distanceBetweenPickups = distanceBetweenPickups;
    }

    public double getDistanceBetweenDropOffs() {
        return distanceBetweenDropOffs;
    }

    public void setDistanceBetweenDropOffs(double distanceBetweenDropOffs) {
        this.distanceBetweenDropOffs = distanceBetweenDropOffs;
    }
}
