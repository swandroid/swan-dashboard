package acba.acbaapp;

import android.content.Context;

/**
 * Created by Alex on 24-May-16.
 */
public class MapMarkerInformationCard extends InformationCard {
    private MapMarkerInterface marker;

    public MapMarkerInformationCard(
            int positionInGrid,
            Context context,
            String title,
            String description,
            String value,
            InformationCardStrategy strategy) {
        super(positionInGrid, context, title, description, value, strategy);
    }

    public MapMarkerInterface getMarker() {
        return marker;
    }

    public boolean hasMarker() {
        return marker != null;
    }

    public void setMarker(MapMarkerInterface marker) {
        this.marker = marker;
    }
}
