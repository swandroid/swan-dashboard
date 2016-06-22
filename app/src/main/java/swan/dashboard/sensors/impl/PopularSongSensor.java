package swan.dashboard.sensors.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import swan.dashboard.sensors.InformationCard;
import swan.dashboard.sensors.InformationCardStrategy;
import swan.dashboard.sensors.InformationCardsData;
import swan.dashboard.models.LastFMTrack;
import swan.dashboard.services.RequestManager;
import swan.dashboard.services.RequestManagerHandlers;
import swan.dashboard.activities.DashboardActivity;
import swan.dashboard.R;

public class PopularSongSensor extends InformationCard {
    /**
     * @param positionInGrid  The position of the information card in the GridView.
     *                        Should be unique per instance
     * @param context
     */
    public PopularSongSensor(int positionInGrid, Context context) {
        super(positionInGrid, context);

        this.tileType = InformationCardsData.TILE_TYPE_NORMAL;
        this.title = context.getString(R.string.activity_title_most_popular_song);
        this.descriptionText = context.getString(R.string.most_popular_song);
        this.imageResourceId = R.drawable.song;
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_song),
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
                                context.getString(R.string.popular_song_url),
                                new RequestManagerHandlers() {
                                    @Override
                                    public void onPostExecute(Context context, String result) {
                                        final DashboardActivity activity = (DashboardActivity) context;
                                        try {
                                            JSONObject jsonObject = new JSONObject(result);
                                            LastFMTrack track =
                                                    new LastFMTrack(
                                                            jsonObject.getJSONObject("tracks")
                                                                    .getJSONArray("track")
                                                                    .getJSONObject(0)
                                                    );

//                                            InformationCard tile = getTile(3);
                                            String value = String.format("\"%s\" by %s", track.getName(), track.getArtistName());
                                            setValue(value);
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                            editor.putString(
                                                    context.getString(R.string.preference_key_song),
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
