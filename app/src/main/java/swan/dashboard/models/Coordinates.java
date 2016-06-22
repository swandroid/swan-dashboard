package swan.dashboard.models;

import java.io.Serializable;

/**
 * Created by Alex on 24-May-16.
 */
public class Coordinates implements Serializable {
    private double latitude, longitude;

    public Coordinates() {
        latitude = 0;
        longitude = 0;
    }

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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean hasLatitude() {
        return latitude != 0;
    }

    public boolean hasLongitude() {
        return longitude != 0;
    }
}
