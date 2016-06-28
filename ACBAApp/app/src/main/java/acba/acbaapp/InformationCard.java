package acba.acbaapp;

import android.content.Context;

import acba.acbaapp.InformationCardStrategy;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Base class holding information specific to an information card
 */
public class InformationCard {
    private Context context;
    private String title;
    private String descriptionText;
    private String valueText;
    private InformationCardStrategy strategy;
    private int positionInGrid;
    private int imageResourceId;

    /**
     *
     * @param positionInGrid The position of the information card in the GridView.
     *                       Should be unique per instance
     * @param context
     * @param title The title of the activity which should open and offer more details
     *              about the information presented by the respectiv information card
     * @param description String containing the text describing the information
     *                    presented by the information card
     * @param value The value of the information card
     * @param strategy Instance of {@link InformationCardStrategy} defining actions
     *                 to perform when processing new values of the information card
     *                 and when the card is clicked
     */
    public InformationCard(
            int positionInGrid,
            Context context,
            String title,
            String description,
            String value,
            int imageResourceId,
            InformationCardStrategy strategy) {
        this.positionInGrid = positionInGrid;
        this.context = context;
        this.title = title;
        this.descriptionText = description;
        this.valueText = value;
        this.imageResourceId = imageResourceId;
        this.strategy = strategy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return descriptionText;
    }

    public String getValue() {
        return valueText;
    }

    public int getImageResourceId() {return this.imageResourceId;}

    public void setValue(String value) {
        this.valueText = value;
    }

    public void process() {
        strategy.resultHandler(context, positionInGrid);
    }

    public void executeOnClickHandler() {
        strategy.onTileClickHandler(context, positionInGrid);
    }

    private String getString(int id) {
        return context.getResources().getString(id);
    }
    private int getInt(int id) {
        return context.getResources().getInteger(id);
    }
    private String getStoredPreferenceString(String key, String defaultValue) {
        return ((MainActivity)context).prefs.getString(key, defaultValue);
    }
}
