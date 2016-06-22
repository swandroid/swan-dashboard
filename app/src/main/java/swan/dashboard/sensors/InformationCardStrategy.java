package swan.dashboard.sensors;

import android.content.Context;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Interface for defining actions to be performed when an Information Card is clicked
 * or when processing new values
 */
public interface InformationCardStrategy {
    /**
     * Performed when the information card is clicked
     * @param context
     * @param positionInGrid Position of the information card in the GridView.
     *                       Should be unique per information card.
     */
    void onTileClickHandler(Context context, int positionInGrid);

    /**
     * Algorithm to process new values for an information card
     * @param context
     * @param positionInGrid Position of the information card in the GridView.
     *                       Should be unique per information card
     */
    void resultHandler(Context context, int positionInGrid);
}
