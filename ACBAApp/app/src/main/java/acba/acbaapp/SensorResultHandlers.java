package acba.acbaapp;

import interdroid.swan.swansong.TimestampedValue;

/**
 * Created by Alex on 24-May-16.
 */
/**
 * Defines algorithm to be run upon new values from {@link interdroid.swan.ValueExpressionListener}
 */
public interface SensorResultHandlers {
    void onNewValues(String arg0, TimestampedValue[] arg1);
}
