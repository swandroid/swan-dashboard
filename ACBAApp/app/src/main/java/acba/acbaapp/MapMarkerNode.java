package acba.acbaapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;

/**
 * Created by Alex on 24-May-16.
 */
public class MapMarkerNode implements Serializable {
    private MapMarkerInterface marker;
    private MapMarkerNode next;
    private double distanceFromOrigin;

    public MapMarkerNode(
            MapMarkerInterface marker,
            Coordinates origin) {
        this.marker = marker;
        next = null;

        Coordinates coordinates = marker.getCoordinates();

        LatLng from = new LatLng(origin.getLatitude(), origin.getLongitude()),
                to = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
        this.distanceFromOrigin = SphericalUtil.computeDistanceBetween(from, to);
    }

    public MapMarkerNode(
            MapMarkerInterface marker,
            double originLatitude,
            double originLongitude) {
        this.marker = marker;
        next = null;

        Coordinates coordinates = marker.getCoordinates();
        LatLng from = new LatLng(originLatitude, originLongitude);
        LatLng to = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());

        this.distanceFromOrigin = SphericalUtil.computeDistanceBetween(from, to);
    }

    public MapMarkerInterface getMarker() {
        return marker;
    }

    public void setNext(MapMarkerNode next) {
        this.next = next;
    }

    public MapMarkerNode getNext() {
        return next;
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public double getDistanceFromOrigin() {
        return distanceFromOrigin;
    }
}
