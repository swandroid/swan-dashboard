package acba.acbaapp;

import android.content.Context;

public class LandmarksInformationCard extends InformationCard {
    private OrderedMapMarkerList nearestMarkers = null;

    public LandmarksInformationCard(int positionInGrid, Context context) {
        super(positionInGrid, context);

        nearestMarkers = new OrderedMapMarkerList();
        tileType = InformationCardsData.TILE_TYPE_GROUP;
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
