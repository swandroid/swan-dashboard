package acba.acbaapp;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SensorInfo;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swanmain.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;
import swan.dashboard.DashboardActivity;
import swan.dashboard.R;

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

    private ValueExpressionRegistrar(Context context) {
        this.context = context;
        handlers = new HashMap<>();
    }

    public static ValueExpressionRegistrar getInstance(Context context) {
        if(instance == null) {
            instance = new ValueExpressionRegistrar(context);
        }

        return instance;
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
                                getExpression((DashboardActivity) context, sensorId)
                        ),
                        new ValueExpressionListener() {
                            @Override
                            public void onNewValues(final String id, final TimestampedValue[] newValues) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        for (SensorResultHandlers handler : handlers.get(sensorId)) {
                                            handler.onNewValues(id, newValues);
                                        }
                                    }
                                }.start();

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
                                getExpression((DashboardActivity) context, sensorId)
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

    private String getExpression(DashboardActivity activity, int sensorId) {
        String preferenceKey, expression;

        switch(sensorId) {
            case DashboardActivity.REQUEST_CODE_SCREEN_SENSOR: {
                expression = activity.getString(R.string.screen_expression);
                preferenceKey = activity.getString(R.string.preference_key_screen_expression);
                break;
            }
            case DashboardActivity.REQUEST_CODE_WIFI_SENSOR: {
                expression = activity.getString(R.string.wifi_expression);
                preferenceKey = activity.getString(R.string.preference_key_wifi_expression);
                break;
            }
            case DashboardActivity.REQUEST_CODE_STEP_COUNTER_SENSOR: {
                expression = activity.getString(R.string.step_counter_expression);
                preferenceKey = activity.getString(R.string.preference_key_step_counter_expression);
                break;
            }
            case DashboardActivity.REQUEST_CODE_SOUND_SENSOR: {
                expression = activity.getString(R.string.sound_expression);
                preferenceKey = activity.getString(R.string.preference_key_sound_expression);
                break;
            }
            case DashboardActivity.REQUEST_CODE_LATITUDE_SENSOR: {
                expression = activity.getString(R.string.latitude_expression);
                preferenceKey = activity.getString(R.string.preference_key_latitude_expression);
                break;
            }
            case DashboardActivity.REQUEST_CODE_LONGITUDE_SENSOR: {
                expression = activity.getString(R.string.longitude_expression);
                preferenceKey = activity.getString(R.string.preference_key_longitude_expression);
                break;
            }
            default: {
                throw new IllegalArgumentException("Not implemented for given sensor");
            }
        }

        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                preferenceKey,
                expression
        );
    }
}
