package acba.informationcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

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
import acba.acbaapp.MainActivity;
import acba.acbaapp.MapMarker;
import acba.acbaapp.MapMarkerNode;
import acba.acbaapp.MapsActivity;
import acba.acbaapp.OrderedMapMarkerList;
import acba.acbaapp.R;
import acba.acbaapp.RequestManager;
import acba.acbaapp.RequestManagerHandlers;
import acba.acbaapp.SensorResultHandlers;
import acba.acbaapp.ValueExpressionRegistrar;
import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
import interdroid.swan.swansong.TimestampedValue;

/**
 * Created by Alex on 28-Jun-16.
 */
public class EcopassagesInformationCard extends LandmarksInformationCard {

    public EcopassagesInformationCard(int id, final Context context) {
        super(id,
                context,
                context.getResources().getString(R.string.activity_title_ecopassages),
                context.getResources().getString(R.string.ecopassages),
                ((MainActivity)context).prefs.getString(
                        context.getResources().getString(R.string.preference_key_ecopassages),
                        context.getResources().getString(R.string.zero)
                ),
                R.drawable.bridge48,
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();
                    Resources r = context.getResources();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        LandmarksInformationCard tile =
                                (LandmarksInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarkers()) {
                            new AlertDialog.Builder(context)
                                    .setTitle(r.getString(R.string.dialog_no_data_title))
                                    .setMessage(r.getString(R.string.dialog_no_data_message))
                                    .setCancelable(true)
                                    .setPositiveButton(
                                            r.getString(R.string.dialog_dismiss_button),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }
                                    )
                                    .show();
                            return;
                        }

                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(r.getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                r.getString(R.string.intent_extra_key_coordinates),
                                ((LandmarksInformationCard)
                                        activity.data.getTile(positionInGrid)).getMarkers(
                                        r.getInteger(R.integer.max_maps_activity_markers)
                                )
                        );

                        List<Intent> sensorConfigIntents = new ArrayList<Intent>();
                        try {
                            Intent sensorConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                            sensorConfigIntent.putExtra(
                                    "expression",
                                    activity.prefs.getString(
                                            r.getString(R.string.preference_key_latitude_expression),
                                            r.getString(R.string.latitude_expression)
                                    )
                            );
                            sensorConfigIntent.putExtra(
                                    r.getString(R.string.intent_extra_key_stored_preference_key),
                                    r.getString(R.string.preference_key_latitude_expression)
                            );
                            sensorConfigIntent.putExtra(
                                    r.getString(R.string.intent_extra_key_request_code),
                                    MainActivity.REQUEST_CODE_LATITUDE_SENSOR
                            );
                            sensorConfigIntent.putExtra(
                                    r.getString(R.string.intent_extra_key_menu_item_title),
                                    r.getString(R.string.latitude_menu_item_title)
                            );

                            sensorConfigIntents.add(sensorConfigIntent);

                            sensorConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                            sensorConfigIntent.putExtra(
                                    "expression",
                                    activity.prefs.getString(
                                            r.getString(R.string.preference_key_longitude_expression),
                                            r.getString(R.string.longitude_expression)
                                    )
                            );
                            sensorConfigIntent.putExtra(
                                    r.getString(R.string.intent_extra_key_stored_preference_key),
                                    r.getString(R.string.preference_key_longitude_expression)
                            );
                            sensorConfigIntent.putExtra(
                                    r.getString(R.string.intent_extra_key_request_code),
                                    MainActivity.REQUEST_CODE_LONGITUDE_SENSOR
                            );

                            sensorConfigIntent.putExtra(
                                    r.getString(R.string.intent_extra_key_menu_item_title),
                                    r.getString(R.string.longitude_menu_item_title)
                            );
                            sensorConfigIntents.add(sensorConfigIntent);
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }

                        intent.putExtra(
                                r.getString(R.string.intent_extra_key_sensor_config_intent),
                                (Serializable)sensorConfigIntents
                        );
                        activity.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(final Context context, final int positionInGrid) {
                        ValueExpressionRegistrar.getInstance().register(
                                MainActivity.REQUEST_CODE_LATITUDE_SENSOR,
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
                        ValueExpressionRegistrar.getInstance().register(
                                MainActivity.REQUEST_CODE_LONGITUDE_SENSOR,
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
                                        r.getString(R.string.ecopassages_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                LandmarksInformationCard tile = (LandmarksInformationCard)activity.data.getTile(positionInGrid);
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

                                                    tile.setNearestMarkers(ecopassagesNearby);
                                                    String value = Integer.toString(list.size());
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            r.getString(R.string.preference_key_ecopassages),
                                                            value
                                                    );
                                                    editor.commit();
                                                    activity.adapter.notifyDataSetChanged();
                                                } catch (Exception e) {
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
