package swan.dashboard.sensors.impl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import acba.acbaapp.InformationCard;
import acba.acbaapp.InformationCardStrategy;
import acba.acbaapp.InformationCardsData;
import acba.acbaapp.SensorResultHandlers;
import acba.acbaapp.ValueExpressionRegistrar;
import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swansong.TimestampedValue;
import swan.dashboard.DashboardActivity;
import swan.dashboard.DetailsActivity;
import swan.dashboard.R;

public class WifiStationsSensor extends InformationCard {
    /**
     * @param positionInGrid  The position of the information card in the GridView.
     *                        Should be unique per instance
     * @param context
     */
    public WifiStationsSensor(int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.tileType = InformationCardsData.TILE_TYPE_GROUP_COUNT;
        this.positionInGrid = positionInGrid;
        this.context = context;
        this.title = context.getString(R.string.activity_title_wifi_stations_seen);
        this.descriptionText = context.getString(R.string.wifi_stations_seen);
        this.imageResourceId = R.drawable.wifi1;
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_wifi_stations),
                context.getString(R.string.zero)
        );
        this.strategy = new InformationCardStrategy() {
            DashboardActivity activity = (DashboardActivity)context;

            @Override
            public void onTileClickHandler(Context context, int positionInGrid) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(context.getString(R.string.intent_extra_key_title), getTitle());
                intent.putExtra(context.getString(R.string.intent_extra_key_value), getValue());
                intent.putExtra(context.getString(R.string.intent_extra_key_request_code),
                        DashboardActivity.REQUEST_CODE_WIFI_SENSOR);
                Intent sensorConfigIntent = null;
                try {
                    sensorConfigIntent= ExpressionManager
                            .getSensor(context, DashboardActivity.WIFI_SENSOR_NAME)
                            .getConfigurationIntent();
                } catch (SwanException e) {
                    e.printStackTrace();
                }
                if (sensorConfigIntent != null) {
                    sensorConfigIntent.putExtra(
                            "expression",
                            PreferenceManager.getDefaultSharedPreferences(context).getString(
                                    context.getString(R.string.preference_key_wifi_expression),
                                    context.getString(R.string.wifi_expression)
                            )
                    );
                }
                intent.putExtra(
                        context.getString(R.string.intent_extra_key_sensor_config_intent),
                        sensorConfigIntent
                );
                context.startActivity(intent);
            }

            @Override
            public void resultHandler(final Context context, final int positionInGrid) {
                ValueExpressionRegistrar.getInstance(context).register(
                        DashboardActivity.REQUEST_CODE_WIFI_SENSOR,
                        new SensorResultHandlers() {
                            DashboardActivity activity = (DashboardActivity)context;

                            @Override
                            public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                if (arg1 != null && arg1.length > 0) {
                                    String value = String.valueOf(arg1.length);
                                    setValue(value);
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                    editor.putString(
                                            context.getString(R.string.preference_key_wifi_stations),
                                            value
                                    );
                                    editor.apply();
                                    activity.adapter.notifyDataSetChanged();
                                }
                            }
                        }
                );
            }
        };
    }
}
