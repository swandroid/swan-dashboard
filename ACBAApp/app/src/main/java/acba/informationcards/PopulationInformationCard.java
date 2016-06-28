package acba.informationcards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import acba.acbaapp.DetailsActivity;
import acba.acbaapp.InformationCard;
import acba.acbaapp.InformationCardStrategy;
import acba.acbaapp.MainActivity;
import acba.acbaapp.R;
import acba.acbaapp.RequestManager;
import acba.acbaapp.RequestManagerHandlers;

/**
 * Created by Alex on 21-Jun-16.
 */
public class PopulationInformationCard extends InformationCard {

    public PopulationInformationCard(int id, final Context context) {

        super(id,
                context,
                context.getResources().getString(R.string.activity_title_population),
                context.getResources().getString(R.string.population),
                ((MainActivity)context).prefs.getString(
                        context.getResources().getString(R.string.preference_key_population),
                        context.getResources().getString(R.string.not_available)
                ),
                R.drawable.gender48,
                new InformationCardStrategy() {
                    Resources r = context.getResources();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(r.getString(R.string.intent_extra_key_title),
                                ((MainActivity) context).data.getTile(positionInGrid).getTitle());
                        intent.putExtra(r.getString(R.string.intent_extra_key_value),
                                ((MainActivity) context).data.getTile(positionInGrid).getValue());
                        context.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(Context context, int positionInGrid) {
                        RequestManager requestManager =
                                new RequestManager(
                                        context,
                                        r.getString(R.string.population_api_url),
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                InformationCard tile = activity.data.getTile(11);
                                                try {
                                                    JSONArray populationByYear = new JSONObject(result).getJSONArray("value");
                                                    int population =
                                                            populationByYear.getJSONObject(populationByYear.length() - 1).getInt("TotaleBevolking_1");
                                                    String value = String.format(String.format("%d", population));
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            r.getString(R.string.preference_key_population),
                                                            value
                                                    );
                                                    editor.commit();
                                                    activity.adapter.notifyDataSetChanged();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                );
                        requestManager.execute();
                    }
                });
    }
}
