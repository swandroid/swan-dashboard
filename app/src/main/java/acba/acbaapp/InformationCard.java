package acba.acbaapp;

import android.content.Context;

/**
 * Base class holding information specific to an information card
 */
public class InformationCard {
    protected Context context;
    protected String title;
    protected String descriptionText;
    protected String valueText;
    protected int tileType;
    protected InformationCardStrategy strategy;
    protected int positionInGrid;
    protected int imageResourceId;

    public InformationCard(final int positionInGrid, final Context context){
        this.positionInGrid = positionInGrid;
        this.context = context;
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

    public int getTileType() {
        return tileType;
    }

    public void process() {
        strategy.resultHandler(context, positionInGrid);
    }

    public void executeOnClickHandler() {
        strategy.onTileClickHandler(context, positionInGrid);
    }
}
