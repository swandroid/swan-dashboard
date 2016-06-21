package swan.dashboard.sensors.impl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import acba.acbaapp.Coordinates;
import acba.acbaapp.InformationCardStrategy;
import acba.acbaapp.LandmarksInformationCard;
import acba.acbaapp.MapMarker;
import acba.acbaapp.MapMarkerNode;
import acba.acbaapp.MapsActivity;
import acba.acbaapp.OrderedMapMarkerList;
import acba.acbaapp.RequestManager;
import acba.acbaapp.RequestManagerHandlers;
import acba.acbaapp.SensorResultHandlers;
import acba.acbaapp.ValueExpressionRegistrar;
import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
import interdroid.swan.swansong.TimestampedValue;
import swan.dashboard.DashboardActivity;
import swan.dashboard.R;

public class ParkingSpotsSensor extends LandmarksInformationCard {
    public ParkingSpotsSensor(int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.imageResourceId = R.drawable.park;
        this.title = context.getString(R.string.activity_title_parkings);
        this.descriptionText = context.getString(R.string.parking_spots);
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_parking_spots),
                context.getString(R.string.not_available)
        );
        this.strategy = new InformationCardStrategy() {
            Coordinates origin = new Coordinates();

            @Override
            public void onTileClickHandler(Context context, int positionInGrid) {
                DashboardActivity activity = (DashboardActivity) context;

                if(!hasMarkers()) {
                    return;
                }
                MapMarkerNode[] coordinates = getMarkers(1);
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(context.getString(
                                R.string.intent_extra_key_title),
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
                                if(arg1 != null && arg1.length > 0) {
                                    origin.setLatitude((double) arg1[0].getValue());
                                    if(origin.hasLongitude()) {
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
                                if(arg1 != null && arg1.length > 0) {
                                    origin.setLongitude((double) arg1[0].getValue());
                                    if(origin.hasLatitude()) {
                                        processNewLocation(positionInGrid);
                                    }
                                }
                            }
                        }
                );
            }

            private void processNewLocation(final int positionInGrid) {
                RequestManager requestManager =
                        new RequestManager(
                                context,
                                context.getString(R.string.parkings_api_url),
                                origin,
                                new RequestManagerHandlers() {
                                    @Override
                                    public void onPostExecute(Context context, String result) {
                                        DashboardActivity activity = (DashboardActivity) context;
                                        MapMarkerNode nearestParking = null;
                                        int freeSpaces = 0;

                                        try {
                                            JSONObject jObj = new JSONObject(result);
                                            JSONArray features = jObj.getJSONArray("features");
                                            for (int i = 0; i < features.length(); i++) {
                                                JSONObject parking = features.getJSONObject(i);
                                                JSONObject data = parking
                                                        .getJSONObject("properties")
                                                        .getJSONObject("layers")
                                                        .getJSONObject("parking.garage")
                                                        .getJSONObject("data");
                                                String label = data.getString("Name");
                                                JSONArray coords = parking
                                                        .getJSONObject("geometry")
                                                        .getJSONArray("coordinates");
                                                MapMarkerNode parkingNode = new MapMarkerNode(
                                                        new MapMarker(
                                                                label,
                                                                new Coordinates(
                                                                        coords.getDouble(1),
                                                                        coords.getDouble(0)
                                                                )
                                                        ),
                                                        origin.getLatitude(),
                                                        origin.getLongitude()
                                                );

                                                if(nearestParking == null) {
                                                    nearestParking = parkingNode;
                                                    freeSpaces =
                                                            data.optInt("FreeSpaceShort") +
                                                                    data.optInt("FreeSpaceLong");
                                                } else if(
                                                        parkingNode
                                                                .getDistanceFromOrigin()
                                                                < nearestParking
                                                                .getDistanceFromOrigin()) {
                                                    nearestParking = parkingNode;
                                                    freeSpaces =
                                                            data.optInt("FreeSpaceShort") +
                                                                    data.optInt("FreeSpaceLong");
                                                }

                                                OrderedMapMarkerList list =
                                                        new OrderedMapMarkerList();
                                                list.add(nearestParking);
                                                setNearestMarkers(list);
                                                setValue(String.format("%d", freeSpaces));
                                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                                editor.putString(
                                                        context.getString(R.string.preference_key_parking_spots),
                                                        String.format("%d", freeSpaces)
                                                );
                                                editor.apply();
                                                activity.adapter.notifyDataSetChanged();
                                            }
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
