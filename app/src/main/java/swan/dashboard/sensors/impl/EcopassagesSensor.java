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

import acba.acbaapp.Coordinates;
import acba.acbaapp.InformationCardStrategy;
import acba.acbaapp.LandmarkXMLHandler;
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

public class EcopassagesSensor extends LandmarksInformationCard {

    public EcopassagesSensor(final int positionInGrid, final Context context) {
        super(positionInGrid, context);

        this.imageResourceId = R.drawable.bridge48;
        this.title = context.getString(R.string.activity_title_ecopassages);
        this.descriptionText = context.getString(R.string.ecopassages);
        this.valueText = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.preference_key_ecopassages),
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
                intent.putExtra(context.getString(R.string.intent_extra_key_title),
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
                                context.getString(R.string.ecopassages_api_url),
                                origin,
                                new RequestManagerHandlers() {
                                    @Override
                                    public void onPostExecute(Context context, String result) {
                                        DashboardActivity activity = (DashboardActivity)context;
                                        ArrayList<MapMarker> list;
                                        OrderedMapMarkerList ecopassagesNearby = new OrderedMapMarkerList();

                                        try {
                                            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                                            LandmarkXMLHandler landmarkXMLHandler = new LandmarkXMLHandler();
                                            reader.setContentHandler(landmarkXMLHandler);
                                            reader.parse(new InputSource(new StringReader(result)));
                                            list = landmarkXMLHandler.getMarkers();

                                            for(MapMarker marker : list) {
                                                MapMarkerNode node = new MapMarkerNode(marker, origin.getLatitude(), origin.getLongitude());

                                                if(node.getDistanceFromOrigin() < 5000) {
                                                    ecopassagesNearby.add(node);
                                                }
                                            }

                                            setNearestMarkers(ecopassagesNearby);
                                            String value = Integer.toString(list.size());
                                            setValue(value);
                                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                            editor.putString(
                                                    context.getString(R.string.preference_key_ecopassages),
                                                    value
                                            );
                                            editor.apply();
                                            activity.adapter.notifyDataSetChanged();
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
