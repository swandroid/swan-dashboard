package acba.acbaapp;

import android.content.Context;

/**
 * Created by Alex on 24-May-16.
 */
public class LandmarksInformationCard extends InformationCard {
    private OrderedMapMarkerList nearestMarkers = null;

    public LandmarksInformationCard(
            int positionInGrid,
            Context context,
            String title,
            String description,
            String value,
            int imageResourceId,
            InformationCardStrategy strategy) {
        super(positionInGrid, context, title, description, value, imageResourceId, strategy);

        nearestMarkers = new OrderedMapMarkerList();
    }

    public MapMarkerNode[] getMarkers(int n) {
        return nearestMarkers.get(n);
    }

    public boolean hasMarkers() {
        return nearestMarkers != null && nearestMarkers.size() > 0;
    }

    public void setNearestMarkers(OrderedMapMarkerList markers) {
        nearestMarkers = markers;
    }
}
