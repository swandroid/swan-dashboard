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
import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
import interdroid.swan.swansong.TimestampedValue;
import swan.dashboard.DashboardActivity;
import swan.dashboard.DetailsActivity;
import swan.dashboard.R;

public class ScreenChecksSensor extends InformationCard {

    /**
     * @param positionInGrid  The position of the information card in the GridView.
     *                        Should be unique per instance
     * @param context
     */
    public ScreenChecksSensor(int positionInGrid, Context context) {
        super(positionInGrid, context);

        this.title = context.getString(R.string.activity_title_screen_checks);
        this.descriptionText = context.getString(R.string.phone_checks);
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_screen_checks),
                context.getString(R.string.zero)
        );
        this.imageResourceId = R.drawable.android52;
        this.tileType = InformationCardsData.TILE_TYPE_GROUP_COUNT;
        this.strategy = new InformationCardStrategy() {

            @Override
            public void onTileClickHandler(Context context, int positionInGrid) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(context.getString(R.string.intent_extra_key_title), getTitle());
                intent.putExtra(context.getString(R.string.intent_extra_key_value), getValue());
                intent.putExtra(context.getString(R.string.intent_extra_key_request_code),
                        DashboardActivity.REQUEST_CODE_SCREEN_SENSOR);
                Intent sensorConfigIntent = null;
                try {
                    sensorConfigIntent= ExpressionManager
                            .getSensor(context, DashboardActivity.SCREEN_SENSOR_NAME)
                            .getConfigurationIntent();
                } catch (SwanException e) {
                    e.printStackTrace();
                }
                if (sensorConfigIntent != null) {
                    sensorConfigIntent.putExtra(
                            "expression",
                            PreferenceManager.getDefaultSharedPreferences(context).getString(
                                    context.getString(R.string.preference_key_screen_expression),
                                    context.getString(R.string.screen_expression)
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
                        DashboardActivity.REQUEST_CODE_SCREEN_SENSOR,
                        new SensorResultHandlers() {
                            DashboardActivity activity = (DashboardActivity) context;

                            @Override
                            public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                if (arg1 != null && arg1.length > 0) {
                                    setValue(String.valueOf((arg1.length - 1) / 2));
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                    editor.putString(
                                            context.getString(R.string.preference_key_screen_checks),
                                            String.valueOf((arg1.length - 1) / 2)
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
