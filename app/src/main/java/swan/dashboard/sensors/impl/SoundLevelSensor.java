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

public class SoundLevelSensor extends InformationCard {

    public SoundLevelSensor(final int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.imageResourceId = R.drawable.sound1;
        this.title = context.getString(R.string.activity_title_sound_level);
        this.descriptionText = context.getString(R.string.sound);
        this.tileType = InformationCardsData.TILE_TYPE_NORMAL;
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_sound_level),
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
                        DashboardActivity.REQUEST_CODE_SOUND_SENSOR);
                Intent sensorConfigIntent = null;
                try {
                    sensorConfigIntent= ExpressionManager
                            .getSensor(context, DashboardActivity.SOUND_SENSOR_NAME)
                            .getConfigurationIntent();
                } catch (SwanException e) {
                    e.printStackTrace();
                }
                if (sensorConfigIntent != null) {
                    sensorConfigIntent.putExtra(
                            "latitude_expression",
                            PreferenceManager.getDefaultSharedPreferences(context).getString(
                                    context.getString(R.string.preference_key_latitude_expression),
                                    context.getString(R.string.latitude_expression)
                            )
                    );
                    sensorConfigIntent.putExtra(
                            "longitude_expression",
                            PreferenceManager.getDefaultSharedPreferences(context).getString(
                                    context.getString(R.string.preference_key_longitude_expression),
                                    context.getString(R.string.longitude_expression)
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
                        DashboardActivity.REQUEST_CODE_SOUND_SENSOR,
                        new SensorResultHandlers() {
                            @Override
                            public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                DashboardActivity activity = (DashboardActivity)context;

                                if (arg1 != null && arg1.length > 0) {
                                    String value = String.format("%.2f", arg1[0].getValue());
                                    setValue(value);
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                    editor.putString(
                                            context.getString(R.string.preference_key_sound_level),
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
