package acba.acbaapp;

import java.io.Serializable;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Interface for GoogleMaps markers.
 * Extends {@link Serializable} in order to be passed in an {@link android.content.Intent}
 */
public interface MapMarkerInterface extends Serializable {
    /**
     *
     * @return Label of the Google Maps marker
     */
    String getLabel();

    /**
     *
     * @return {@link Coordinates} for the Google Maps marker
     */
    Coordinates getCoordinates();
}
