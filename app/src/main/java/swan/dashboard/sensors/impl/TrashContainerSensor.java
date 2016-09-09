package swan.dashboard.sensors.impl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import swan.dashboard.models.Coordinates;
import swan.dashboard.sensors.InformationCardStrategy;
import swan.dashboard.sensors.InformationCardsData;
import swan.dashboard.sensors.LandmarksInformationCard;
import swan.dashboard.models.MapMarker;
import swan.dashboard.models.MapMarkerNode;
import swan.dashboard.activities.MapsActivity;
import swan.dashboard.models.OrderedMapMarkerList;
import swan.dashboard.services.RequestManager;
import swan.dashboard.services.RequestManagerHandlers;
import swan.dashboard.services.SensorResultHandlers;
import swan.dashboard.services.ValueExpressionRegistrar;
import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swansong.TimestampedValue;
import swan.dashboard.activities.DashboardActivity;
import swan.dashboard.R;

public class TrashContainerSensor extends LandmarksInformationCard {
    public TrashContainerSensor(final int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.tileType = InformationCardsData.TILE_TYPE_GROUP_DISTANCE;
        this.imageResourceId = R.drawable.trash;
        this.title = context.getString(R.string.activity_title_glass_container);
        this.descriptionText = context.getString(R.string.trash_deposit);
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_trash_containers),
                context.getString(R.string.not_available)
        );
        this.strategy = new InformationCardStrategy() {
            Coordinates origin = new Coordinates();

            @Override
            public void onTileClickHandler(Context context, int positionInGrid) {
                DashboardActivity activity = (DashboardActivity)context;

                if(!hasMarkers()) {
                    return;
                }
                MapMarkerNode[] coordinates = getMarkers(1);
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(
                        context.getString(R.string.intent_extra_key_title),
                        getTitle()
                );
                intent.putExtra(
                        context.getString(R.string.intent_extra_key_coordinates), coordinates
                );
                List<Intent> sensorConfigIntents = new ArrayList<>();
                try {
                    Intent sensorConfigIntent = ExpressionManager
                            .getSensor(context, DashboardActivity.LOCATION_SENSOR_NAME)
                            .getConfigurationIntent();
                    sensorConfigIntent.putExtra(
                            "expression",
                            PreferenceManager.getDefaultSharedPreferences(context).getString(
                                    context.getString(R.string.preference_key_latitude_expression),
                                    context.getString(R.string.latitude_expression)
                            )
                    );
                    sensorConfigIntent.putExtra(
                            context.getString(R.string.intent_extra_key_stored_preference_key),
                            context.getString(R.string.preference_key_latitude_expression)
                    );
                    sensorConfigIntent.putExtra(
                            context.getString(R.string.intent_extra_key_request_code),
                            DashboardActivity.REQUEST_CODE_LATITUDE_SENSOR
                    );
                    sensorConfigIntent.putExtra(
                            context.getString(R.string.intent_extra_key_menu_item_title),
                            context.getString(R.string.latitude_menu_item_title)
                    );

                    sensorConfigIntents.add(sensorConfigIntent);

                    sensorConfigIntent = ExpressionManager
                            .getSensor(context, DashboardActivity.LOCATION_SENSOR_NAME)
                            .getConfigurationIntent();
                    sensorConfigIntent.putExtra(
                            "expression",
                            PreferenceManager.getDefaultSharedPreferences(context).getString(
                                    context.getString(R.string.preference_key_longitude_expression),
                                    context.getString(R.string.longitude_expression)
                            )
                    );
                    sensorConfigIntent.putExtra(
                            context.getString(R.string.intent_extra_key_stored_preference_key),
                            context.getString(R.string.preference_key_longitude_expression)
                    );
                    sensorConfigIntent.putExtra(
                            context.getString(R.string.intent_extra_key_request_code),
                            DashboardActivity.REQUEST_CODE_LONGITUDE_SENSOR
                    );

                    sensorConfigIntent.putExtra(
                            context.getString(R.string.intent_extra_key_menu_item_title),
                            context.getString(R.string.longitude_menu_item_title)
                    );
                    sensorConfigIntents.add(sensorConfigIntent);
                } catch (SwanException e) {
                    e.printStackTrace();
                }

                intent.putExtra(
                        context.getString(R.string.intent_extra_key_sensor_config_intent),
                        (Serializable)sensorConfigIntents
                );
                activity.startActivity(intent);
            }

            @Override
            public void resultHandler(final Context context, final int positionInGrid) {
                ValueExpressionRegistrar.getInstance(context).register(
                        DashboardActivity.REQUEST_CODE_LATITUDE_SENSOR,
                        new SensorResultHandlers() {
                            @Override
                            public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                if (arg1 != null && arg1.length > 0) {
                                    origin.setLatitude((double) arg1[0].getValue());
                                    if (origin.hasLongitude()) {
                                        processNewLocation(positionInGrid);
                                    }
                                }
                            }
                        }
                );
                ValueExpressionRegistrar.getInstance(context).register(
                        DashboardActivity.REQUEST_CODE_LONGITUDE_SENSOR,
                        new SensorResultHandlers() {
                            @Override
                            public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                if (arg1 != null && arg1.length > 0) {
                                    origin.setLongitude((double) arg1[0].getValue());
                                    if (origin.hasLatitude()) {
                                        processNewLocation(positionInGrid);
                                    }
                                }
                            }
                        }
                );
            }

            private  void processNewLocation(final int positionInGrid) {
                RequestManager requestManager =
                        new RequestManager(
                                context,
                                context.getString(R.string.trash_container_api_url),
                                origin,
                                new RequestManagerHandlers() {
                                    @Override
                                    public void onPostExecute(Context context, String result) {
                                        final DashboardActivity activity = (DashboardActivity)context;
                                        MapMarkerNode nearestGlassContainer = null;

                                        try {
                                            JSONArray trashContainers = (new JSONObject(result)).getJSONArray("features");
                                            double minDistance = Double.MAX_VALUE, distance;
                                            for (int i = 0; i < trashContainers.length(); i++) {
                                                JSONObject containerObj = trashContainers.getJSONObject(i);
                                                JSONObject properties = containerObj.getJSONObject("properties");
                                                String label = properties.getString("titel");
                                                String type = properties.getString("type");

                                                if (type.toLowerCase().equals("glas")) {
                                                    JSONArray coords = containerObj.getJSONObject("geometry").getJSONArray("coordinates");
                                                    Coordinates coordinates = new Coordinates(coords.getDouble(1), coords.getDouble(0));
                                                    LatLng from = new LatLng(origin.getLatitude(), origin.getLongitude()),
                                                            to = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
                                                    distance = SphericalUtil.computeDistanceBetween(from, to);

                                                    if (distance < minDistance) {
                                                        minDistance = distance;
                                                        nearestGlassContainer = new MapMarkerNode(new MapMarker(label, coordinates), origin);
                                                    }
                                                }
                                            }

                                            OrderedMapMarkerList markers = new OrderedMapMarkerList();
                                            markers.add(nearestGlassContainer);
                                            setNearestMarkers(markers);
                                            String value = String.format("%.0f m", minDistance);
                                            setValue(value);
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                            editor.putString(
                                                    context.getString(R.string.preference_key_trash_containers),
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