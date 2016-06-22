package swan.dashboard.sensors.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

import swan.dashboard.sensors.InformationCard;
import swan.dashboard.sensors.InformationCardStrategy;
import swan.dashboard.sensors.InformationCardsData;
import swan.dashboard.services.RequestManager;
import swan.dashboard.services.RequestManagerHandlers;
import swan.dashboard.activities.DashboardActivity;
import swan.dashboard.R;

public class PopulationCountSensor extends InformationCard {
    public PopulationCountSensor(final int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.imageResourceId = R.drawable.population;
        this.title = context.getString(R.string.activity_title_population);
        this.descriptionText = context.getString(R.string.population);
        this.tileType = InformationCardsData.TILE_TYPE_NORMAL;
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_population),
                context.getString(R.string.not_available)
        );
        this.strategy = new InformationCardStrategy() {
            @Override
            public void onTileClickHandler(Context context, int positionInGrid) {
//                Intent intent = new Intent(context, DetailsActivity.class);
//                intent.putExtra(context.getString(R.string.intent_extra_key_title), getTitle());
//                intent.putExtra(context.getString(R.string.intent_extra_key_value), getValue());
//                context.startActivity(intent);
            }

            @Override
            public void resultHandler(Context context, int positionInGrid) {
                RequestManager requestManager =
                        new RequestManager(
                                context,
                                context.getString(R.string.population_api_url),
                                new RequestManagerHandlers() {
                                    @Override
                                    public void onPostExecute(Context context, String result) {
                                        final DashboardActivity activity = (DashboardActivity)context;
                                        try {
                                            JSONArray populationByYear = new JSONObject(result).getJSONArray("value");
                                            int population =
                                                    populationByYear.getJSONObject(populationByYear.length() - 1).getInt("TotaleBevolking_1");
                                            String value = NumberFormat.getNumberInstance(Locale.FRANCE).format(population);
                                            setValue(value);
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                            editor.putString(
                                                    context.getString(R.string.preference_key_population),
                                                    value
                                            );
                                            editor.apply();

//                                            activity.runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    activity.adapter.notifyDataSetChanged();
//                                                }
//                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );
                requestManager.execute();
            }
        };
    }
}
