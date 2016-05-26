package acba.acbaapp;

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

        this.distanceFromOrigin = DistanceCalculator.distance(
                origin.getLatitude(),
                origin.getLongitude(),
                coordinates.getLatitude(),
                coordinates.getLongitude()
        );
    }

    public MapMarkerNode(
            MapMarkerInterface marker,
            double originLatitude,
            double originLongitude) {
        this.marker = marker;
        next = null;

        Coordinates coordinates = marker.getCoordinates();

        this.distanceFromOrigin = DistanceCalculator.distance(
                originLatitude,
                originLongitude,
                coordinates.getLatitude(),
                coordinates.getLongitude()
        );
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
