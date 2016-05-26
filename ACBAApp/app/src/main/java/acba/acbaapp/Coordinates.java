package acba.acbaapp;

import java.io.Serializable;

/**
 * Created by Alex on 24-May-16.
 */
public class Coordinates implements Serializable {
    private double latitude, longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
