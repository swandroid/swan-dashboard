package swan.dashboard.models;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Implementation of {@link MapMarkerInterface}
 */
public class MapMarker implements MapMarkerInterface {
    private String label;
    private Coordinates coordinates;

    public MapMarker(String label, Coordinates coordinates) {
        this.label = label;
        this.coordinates = coordinates;
    }

    public MapMarker(String label, double latitude, double longitude) {
        this.label = label;
        this.coordinates = new Coordinates(latitude, longitude);
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public Coordinates getCoordinates() {
        return this.coordinates;
    }
}
