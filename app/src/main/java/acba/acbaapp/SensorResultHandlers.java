package acba.acbaapp;

/**
 * Created by Alex on 24-May-16.
 */

import interdroid.swancore.swansong.TimestampedValue;

/**
 * Defines algorithm to be run upon new values from {@link interdroid.swancore.swanmain.ValueExpressionListener}
 */
public interface SensorResultHandlers {
    void onNewValues(String arg0, TimestampedValue[] arg1);
}
