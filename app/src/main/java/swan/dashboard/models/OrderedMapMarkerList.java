package swan.dashboard.models;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Linked list of instances of {@link MapMarkerNode}
 * Elements are ordered on-the-fly as they are added to the list in increasing value of
 * distance from origin
 */
public class OrderedMapMarkerList {
    private MapMarkerNode head;
    private int size;

    public OrderedMapMarkerList() {
        head = null;
        size = 0;
    }

    public void add(MapMarkerNode marker) {
        if(head == null) {
            head = marker;
            size++;
        } else if(head.getDistanceFromOrigin() >= marker.getDistanceFromOrigin()) {
            marker.setNext(head);
            head = marker;
            size++;
        } else {
            MapMarkerNode currentNode = head;

            while(currentNode.hasNext() &&
                    currentNode.getNext().getDistanceFromOrigin()
                            < marker.getDistanceFromOrigin()) {
                currentNode = currentNode.getNext();
            }

            marker.setNext(currentNode.getNext());
            currentNode.setNext(marker);
            size++;
        }
    }

    /**
     * Returns the first <code>n</code> instances of {@link MapMarkerNode} from the linked list.
     * Required because of the size limit on {@link android.content.Intent} extras
     *
     * @param n Number of instances of {@link MapMarkerNode} to return
     * @return The first <code>n</code> instances of {@link MapMarkerNode} in the linked list
     */
    public MapMarkerNode[] get(int n) {
        MapMarkerNode[] result = new MapMarkerNode[n];
        MapMarkerNode currentNode = head;

        for(int i=0; i<n && currentNode != null; i++) {
            result[i] = currentNode;
            currentNode = currentNode.getNext();
        }

        return result;
    }

    public int size() {
        return size;
    }
}
