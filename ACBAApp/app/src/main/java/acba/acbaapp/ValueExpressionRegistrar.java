package acba.acbaapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
import interdroid.swan.ValueExpressionListener;
import interdroid.swan.swansong.ExpressionFactory;
import interdroid.swan.swansong.ExpressionParseException;
import interdroid.swan.swansong.TimestampedValue;
import interdroid.swan.swansong.ValueExpression;

/**
 * Created by Alex on 24-May-16.
 */

/**
 * Singleton manager handling registration of SWAN {@link ValueExpression}
 */
public class ValueExpressionRegistrar {
    private static ValueExpressionRegistrar instance = null;
    private Context context;

    /**
     * Maps SWAN sensor request codes to an {@link ArrayList} of {@link SensorResultHandlers} as
     * different information cards may perform different operations based on the result of
     * the same sensor
     */
    private Map<Integer,ArrayList<SensorResultHandlers>> handlers;

    public static ValueExpressionRegistrar getInstance() {
        if(instance == null) {
            instance = new ValueExpressionRegistrar();
        }

        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
    }

    /**
     * Adds a {@link SensorResultHandlers} implementation for the SWAN sensor of the specified
     * <code>id</code>
     * @param id Identifier of the SWAN sensor
     * @param handler Algorithm to be run upon new values of the SWAN sensor of the given
     *                <code>id</code>
     */
    public void register(int id, SensorResultHandlers handler) {
        if(!handlers.containsKey(id)) {
            handlers.put(id, new ArrayList<SensorResultHandlers>());
        }

        handlers.get(id).add(handler);
    }

    public void start() {
        for(final Integer sensorId : handlers.keySet()) {
            try {
                ExpressionManager.registerValueExpression(
                        context,
                        String.valueOf(sensorId),
                        (ValueExpression) ExpressionFactory.parse(
                                getExpression((MainActivity) context, sensorId)
                        ),
                        new ValueExpressionListener() {
                            @Override
                            public void onNewValues(String id, TimestampedValue[] newValues) {
                                for (SensorResultHandlers handler : handlers.get(sensorId)) {
                                    handler.onNewValues(id, newValues);
                                }
                            }
                        }
                );
            } catch (ExpressionParseException e) {
                e.printStackTrace();
            } catch (SwanException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregister(int[] ids) {
        for(int id : ids) {
            ExpressionManager.unregisterExpression(context, String.valueOf(id));
        }
    }

    public void reregister(int[] ids) {
        for(final int sensorId : ids) {
            try {
                ExpressionManager.registerValueExpression(
                        context,
                        String.valueOf(sensorId),
                        (ValueExpression) ExpressionFactory.parse(
                                getExpression((MainActivity) context, sensorId)
                        ),
                        new ValueExpressionListener() {
                            @Override
                            public void onNewValues(String id, TimestampedValue[] newValues) {
                                for(SensorResultHandlers handler : handlers.get(sensorId)) {
                                    handler.onNewValues(id, newValues);
                                }
                            }
                        }
                );
            } catch (ExpressionParseException e) {
                e.printStackTrace();
            } catch (SwanException e) {
                e.printStackTrace();
            }
        }
    }

    private ValueExpressionRegistrar() {
        handlers = new HashMap<>();
    }

    private String getExpression(MainActivity activity, int sensorId) {
        String preferenceKey, expression;

        switch(sensorId) {
            case MainActivity.REQUEST_CODE_SCREEN_SENSOR: {
                expression = activity.getString(R.string.screen_expression);
                preferenceKey = activity.getString(R.string.preference_key_screen_expression);
                break;
            }
            case MainActivity.REQUEST_CODE_WIFI_SENSOR: {
                expression = activity.getString(R.string.wifi_expression);
                preferenceKey = activity.getString(R.string.preference_key_wifi_expression);
                break;
            }
            case MainActivity.REQUEST_CODE_STEP_COUNTER_SENSOR: {
                expression = activity.getString(R.string.step_counter_expression);
                preferenceKey = activity.getString(R.string.preference_key_step_counter_expression);
                break;
            }
            case MainActivity.REQUEST_CODE_SOUND_SENSOR: {
                expression = activity.getString(R.string.sound_expression);
                preferenceKey = activity.getString(R.string.preference_key_sound_expression);
                break;
            }
            case MainActivity.REQUEST_CODE_LOCATION_SENSOR: {
                expression = activity.getString(R.string.location_expression);
                preferenceKey = activity.getString(R.string.preference_key_location_expression);
                break;
            }
            default: {
                throw new IllegalArgumentException("Not implemented for given sensor");
            }
        }

        return activity.prefs.getString(
                preferenceKey,
                expression
        );
    }
}
