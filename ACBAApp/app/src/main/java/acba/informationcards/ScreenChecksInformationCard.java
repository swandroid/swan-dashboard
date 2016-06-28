package acba.informationcards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import acba.acbaapp.DetailsActivity;
import acba.acbaapp.InformationCard;
import acba.acbaapp.InformationCardStrategy;
import acba.acbaapp.MainActivity;
import acba.acbaapp.R;
import acba.acbaapp.SensorResultHandlers;
import acba.acbaapp.ValueExpressionRegistrar;
import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
import interdroid.swan.swansong.TimestampedValue;

/**
 * Created by Alex on 21-Jun-16.
 */
public class ScreenChecksInformationCard extends InformationCard {
    public ScreenChecksInformationCard(int id, final Context context) {
        super(
                id,
                context,
                context.getResources().getString(R.string.activity_title_screen_checks),
                context.getResources().getString(R.string.phone_checks),
                ((MainActivity)context).prefs.getString(
                        context.getResources().getString(R.string.preference_key_screen_checks),
                        context.getResources().getString(R.string.zero)
                ),
                R.drawable.android52,
                new InformationCardStrategy() {
                    MainActivity activity = (MainActivity)context;
                    Resources r = context.getResources();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(r.getString(R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle());
                        intent.putExtra(r.getString(R.string.intent_extra_key_value),
                                activity.data.getTile(positionInGrid).getValue());
                        intent.putExtra(r.getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_SCREEN_SENSOR);
                        Intent sensorConfigIntent = null;
                        try {
                            sensorConfigIntent= ExpressionManager
                                    .getSensor(context, MainActivity.SCREEN_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        sensorConfigIntent.putExtra(
                                "expression",
                                activity.prefs.getString(
                                        r.getString(R.string.preference_key_screen_expression),
                                        r.getString(R.string.screen_expression)
                                )
                        );
                        intent.putExtra(
                                r.getString(R.string.intent_extra_key_sensor_config_intent),
                                sensorConfigIntent
                        );
                        context.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(final Context context, final int positionInGrid) {
                        ValueExpressionRegistrar.getInstance().register(
                                MainActivity.REQUEST_CODE_SCREEN_SENSOR,
                                new SensorResultHandlers() {
                                    MainActivity activity = (MainActivity) context;

                                    @Override
                                    public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                        if (arg1 != null && arg1.length > 0) {
                                            activity.data.getTile(positionInGrid)
                                                    .setValue(
                                                            String.valueOf((arg1.length - 1) / 2)
                                                    );
                                            SharedPreferences.Editor editor = activity.prefs.edit();
                                            editor.putString(
                                                    r.getString(R.string.preference_key_screen_checks),
                                                    String.valueOf((arg1.length - 1) / 2)
                                            );
                                            editor.commit();
                                            activity.adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                        );
                    }
                }
        );
    }
}
