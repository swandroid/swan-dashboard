package swan.dashboard.sensors.impl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import swan.dashboard.models.Coordinates;
import swan.dashboard.sensors.InformationCardStrategy;
import swan.dashboard.services.LandmarkXMLHandler;
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

public class ReligiousMeetingPointsSensor extends LandmarksInformationCard {

    public ReligiousMeetingPointsSensor(final int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.imageResourceId = R.drawable.religie1;
        this.title = context.getString(R.string.activity_title_religious_meeting_points);
        this.descriptionText = context.getString(R.string.religious_meeting_points);
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_religious_meeting_points),
                context.getString(R.string.zero)
        );
        this.strategy = new InformationCardStrategy() {
            Coordinates origin = new Coordinates();

            @Override
            public void onTileClickHandler(Context context, int positionInGrid) {
                DashboardActivity activity = (DashboardActivity)context;

                if(!hasMarkers()) {
                    return;
                }
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(context.getString(
                        R.string.intent_extra_key_title),
                        getTitle()
                );
                intent.putExtra(
                        context.getString(R.string.intent_extra_key_coordinates),
                        getMarkers(context.getResources().getInteger(R.integer.max_maps_activity_markers))
                );
                List<Intent> sensorConfigIntents = new ArrayList<Intent>();
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

            private void processNewLocation(final int positionInGrid) {
                RequestManager requestManager =
                        new RequestManager(
                                context,
                                context.getString(R.string.religious_meeting_points_api_url),
                                origin,
                                new RequestManagerHandlers() {
                                    @Override
                                    public void onPostExecute(Context context, String result) {
                                        final DashboardActivity activity = (DashboardActivity)context;
                                        ArrayList<MapMarker> list;
                                        OrderedMapMarkerList religiousMeetingPointsNearby = new OrderedMapMarkerList();

                                        try {
                                            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                                            LandmarkXMLHandler landmarkXMLHandler = new LandmarkXMLHandler();
                                            reader.setContentHandler(landmarkXMLHandler);
                                            reader.parse(new InputSource(new StringReader(result)));
                                            list = landmarkXMLHandler.getMarkers();

                                            for(MapMarker marker : list) {
                                                MapMarkerNode node = new MapMarkerNode(marker, origin.getLatitude(), origin.getLongitude());

                                                if(node.getDistanceFromOrigin() < 5000) {
                                                    religiousMeetingPointsNearby.add(node);
                                                }
                                            }

                                            setNearestMarkers(religiousMeetingPointsNearby);
                                            String value = Integer.toString(list.size());
                                            setValue(value);
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                            editor.putString(
                                                    context.getString(R.string.preference_key_religious_meeting_points),
                                                    value
                                            );
                                            editor.apply();

//                                            activity.runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    activity.adapter.notifyDataSetChanged();
//                                                }
//                                            });
                                        } catch (Exception e) {
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
