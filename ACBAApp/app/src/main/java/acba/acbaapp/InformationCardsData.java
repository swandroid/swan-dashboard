package acba.acbaapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
import interdroid.swan.ValueExpressionListener;
import interdroid.swan.swansong.TimestampedValue;

/**
 * Created by Alex on 24-May-16.
 */
public class InformationCardsData {
    private static InformationCardsData instance = null;
    private ArrayList<InformationCard> tiles = null;
    private Context context = null;

    protected InformationCardsData() {
        tiles = new ArrayList<>();
    };

    public static InformationCardsData getInstance() {
        if(instance == null) {
            instance = new InformationCardsData();
        }

        return instance;
    }

    public int size() {
        return tiles.size();
    }

    public InformationCard getTile(int position) {
        return tiles.get(position);
    }

    public void initialize(final Context context) {
        this.context = context;
        SharedPreferences prefs = ((MainActivity)context).prefs;
        ValueExpressionRegistrar registrar = ValueExpressionRegistrar.getInstance();

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_screen_checks),
                getString(R.string.phone_checks),
                getStoredPreferenceString(
                        getString(R.string.preference_key_screen_checks),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    MainActivity activity = (MainActivity)context;

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(getString(R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle());
                        intent.putExtra(getString(R.string.intent_extra_key_value),
                                activity.data.getTile(positionInGrid).getValue());
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
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
                                        getString(R.string.preference_key_screen_expression),
                                        getString(R.string.screen_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
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
                                                    getString(R.string.preference_key_screen_checks),
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
        ));



        tiles.add(new MapMarkerInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_parkings),
                getString(R.string.parking_spots),
                getStoredPreferenceString(
                        getString(R.string.preference_key_parking_spots),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity) context;
                        MapMarkerInformationCard tile =
                                (MapMarkerInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarker()) {
                            return;
                        }
                        Coordinates coordinates =
                                ((MapMarkerInformationCard)
                                        ((MainActivity) context).data.getTile(positionInGrid)
                                ).getMarker().getCoordinates();
                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_destination), coordinates
                        );
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        if(arg1 != null && arg1.length > 0) {
                                            origin.setLatitude((double) arg1[0].getValue());
                                            if(origin.hasLongitude()) {
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
                                        getString(R.string.parkings_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity) context;
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

                                                        MapMarkerInformationCard card =
                                                                ((MapMarkerInformationCard)
                                                                        activity.data
                                                                                .getTile(positionInGrid));
                                                        card.setMarker(nearestParking.getMarker());
                                                        card.setValue(String.format("%d", freeSpaces));
                                                        SharedPreferences.Editor editor = activity.prefs.edit();
                                                        editor.putString(
                                                                getString(R.string.preference_key_parking_spots),
                                                                String.format("%d", freeSpaces)
                                                        );
                                                        editor.commit();
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
                }
        ));

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_kilometers_traveled),
                getString(R.string.distance_traveled),
                getStoredPreferenceString(
                        getString(R.string.preference_key_distance_traveled),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {

                    }

                    @Override
                    public void resultHandler(Context context, int positionInGrid) {

                    }
                }
        ));

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_most_popular_song),
                getString(R.string.most_popular_song),
                getStoredPreferenceString(
                        getString(R.string.preference_key_song),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(getString(R.string.intent_extra_key_title),
                                ((MainActivity) context).data.getTile(positionInGrid).getTitle());
                        intent.putExtra(getString(R.string.intent_extra_key_value),
                                ((MainActivity) context).data.getTile(positionInGrid).getValue());
                        context.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(Context context, int positionInGrid) {
                        RequestManager requestManager =
                                new RequestManager(
                                        context,
                                        getString(R.string.popular_song_url),
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity) context;
                                                try {
                                                    JSONObject jsonObject = new JSONObject(result);
                                                    LastFMTrack track =
                                                            new LastFMTrack(
                                                                    jsonObject.getJSONObject("tracks")
                                                                            .getJSONArray("track")
                                                                            .getJSONObject(0)
                                                            );

                                                    InformationCard tile = activity.data.getTile(3);
                                                    String value =
                                                            String.format("\"%s\" by %s", track.getName(), track.getArtistName());
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            getString(R.string.preference_key_song),
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
                }
        ));

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_wifi_stations_seen),
                getString(R.string.wifi_stations_seen),
                getStoredPreferenceString(
                        getString(R.string.preference_key_wifi_stations),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    MainActivity activity = (MainActivity)context;

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(getString(R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle());
                        intent.putExtra(getString(R.string.intent_extra_key_value),
                                activity.data.getTile(positionInGrid).getValue());
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_WIFI_SENSOR);
                        Intent sensorConfigIntent = null;
                        try {
                            sensorConfigIntent= ExpressionManager
                                    .getSensor(context, MainActivity.WIFI_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        sensorConfigIntent.putExtra(
                                "expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_wifi_expression),
                                        getString(R.string.wifi_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                sensorConfigIntent
                        );
                        context.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(final Context context, final int positionInGrid) {
                        ValueExpressionRegistrar.getInstance().register(
                                MainActivity.REQUEST_CODE_WIFI_SENSOR,
                                new SensorResultHandlers() {
                                    MainActivity activity = (MainActivity)context;

                                    @Override
                                    public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                        if (arg1 != null && arg1.length > 0) {
                                            String value = String.valueOf(arg1.length);
                                            activity.data.getTile(positionInGrid)
                                                    .setValue(value);
                                            SharedPreferences.Editor editor = activity.prefs.edit();
                                            editor.putString(
                                                    getString(R.string.preference_key_wifi_stations),
                                                    value
                                            );
                                            editor.commit();
                                            activity.adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                        );
                    }
                }
        ));

        tiles.add(new MapMarkerInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_public_urinal),
                getString(R.string.nearest_public_urinal),
                getStoredPreferenceString(
                        getString(R.string.preference_key_public_urinal),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MapMarkerInformationCard tile =
                                (MapMarkerInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarker()) {
                            return;
                        }
                        Coordinates coordinates =
                                ((MapMarkerInformationCard)
                                        ((MainActivity)context).data.getTile(positionInGrid)
                                ).getMarker().getCoordinates();
                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_destination), coordinates
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        if(arg1 != null && arg1.length > 0) {
                                            origin.setLatitude((double) arg1[0].getValue());
                                            if(origin.hasLongitude()) {
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
                                        getString(R.string.public_urinal_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                LatLng from = new LatLng(origin.getLatitude(), origin.getLongitude());

                                                try {
                                                    JSONArray publicUrinals = new JSONObject(result).getJSONArray("features");
                                                    double minDistance = Double.MAX_VALUE, distance;
                                                    MapMarker nearestPublicUrinal = null;

                                                    for (int i = 0; i < publicUrinals.length(); i++) {
                                                        JSONObject urinal =
                                                                publicUrinals.getJSONObject(i);
                                                        String label = urinal.getJSONObject("properties").getString("titel");
                                                        JSONArray coordinatesJSON = urinal.getJSONObject("geometry").getJSONArray("coordinates");
                                                        Coordinates coordinates = new Coordinates(coordinatesJSON.getDouble(1), coordinatesJSON.getDouble(0));
                                                        MapMarker marker = new MapMarker(label, coordinates);
                                                        LatLng to = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
                                                        distance = SphericalUtil.computeDistanceBetween(from, to);

                                                        if (distance < minDistance) {
                                                            minDistance = distance;
                                                            nearestPublicUrinal = marker;
                                                        }
                                                    }
                                                    MapMarkerInformationCard tile = (MapMarkerInformationCard)activity.data.getTile(positionInGrid);
                                                    tile.setMarker(nearestPublicUrinal);
                                                    String value = String.format(String.format("%.2f", minDistance));
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            getString(R.string.preference_key_public_urinal),
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
                }
        ));

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                "",
                getString(R.string.product_price_abroad),
                getStoredPreferenceString(
                        getString(R.string.preference_key_product_price),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {

                    }

                    @Override
                    public void resultHandler(Context context, int positionInGrid) {

                    }
                }
        ));

        tiles.add(new MultipleMarkersInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_religious_meeting_points),
                getString(R.string.religious_meeting_points),
                getStoredPreferenceString(
                        getString(R.string.preference_key_religious_meeting_points),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MultipleMarkersInformationCard tile =
                                (MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarkers()) {
                            return;
                        }
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_coordinates),
                                ((MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid)).getMarkers(getInt(R.integer.max_maps_activity_markers))
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        getString(R.string.religious_meeting_points_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
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

                                                    MultipleMarkersInformationCard tile = (MultipleMarkersInformationCard)activity.data.getTile(positionInGrid);
                                                    tile.setNearestMarkers(religiousMeetingPointsNearby);
                                                    String value = Integer.toString(list.size());
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            getString(R.string.preference_key_religious_meeting_points),
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
                }
        ));

        tiles.add(new MapMarkerInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_farm),
                getString(R.string.nearest_farm_selling_milk),
                getStoredPreferenceString(
                        getString(R.string.preference_key_local_farms),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MapMarkerInformationCard tile =
                                (MapMarkerInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarker()) {
                            return;
                        }
                        Coordinates coordinates =
                                ((MapMarkerInformationCard)
                                        ((MainActivity)context).data.getTile(positionInGrid)
                                ).getMarker().getCoordinates();
                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_destination), coordinates
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        getString(R.string.local_farms_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity) context;
                                                MapMarkerInformationCard tile = (MapMarkerInformationCard) activity.data.getTile(positionInGrid);
                                                ArrayList<MapMarker> list;
                                                MapMarkerNode nearestFarmWrapper = null;

                                                try {
                                                    XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                                                    LandmarkXMLHandler landmarkXMLHandler = new LandmarkXMLHandler();
                                                    reader.setContentHandler(landmarkXMLHandler);
                                                    reader.parse(new InputSource(new StringReader(result)));
                                                    list = landmarkXMLHandler.getMarkers();

                                                    double minDistance = Double.MAX_VALUE, distance;

                                                    for (MapMarker mapMarker : list) {
                                                        MapMarkerNode node = new MapMarkerNode(mapMarker, origin.getLatitude(), origin.getLongitude());
                                                        distance = node.getDistanceFromOrigin();

                                                        if (distance < minDistance) {
                                                            minDistance = distance;
                                                            nearestFarmWrapper = node;
                                                        }
                                                    }

                                                    tile.setMarker(nearestFarmWrapper.getMarker());
                                                    String value = String.format("%.2f", minDistance);
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            getString(R.string.preference_key_local_farms),
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
                }
        ));

        tiles.add(new MultipleMarkersInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_bike_spots),
                getString(R.string.guarded_bike_places),
                getStoredPreferenceString(
                        getString(R.string.preference_key_bike_spots),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MultipleMarkersInformationCard tile =
                                (MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarkers()) {
                            return;
                        }
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_coordinates),
                                ((MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid)).getMarkers(getInt(R.integer.max_maps_activity_markers))
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        getString(R.string.bike_spots_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                MultipleMarkersInformationCard tile =
                                                        (MultipleMarkersInformationCard)activity.data.getTile(positionInGrid);
                                                OrderedMapMarkerList bikeSpotsNearby = new OrderedMapMarkerList();
                                                try {
                                                    JSONArray bs = new JSONObject(result).getJSONArray("parkeerlocaties");
                                                    for (int i = 0; i < bs.length(); i++) {
                                                        JSONObject bikeSpotObject = bs.getJSONObject(i).getJSONObject("parkeerlocatie");
                                                        String label = bikeSpotObject.getString("title");
                                                        JSONObject location = new JSONObject(bikeSpotObject.getString("Locatie"));
                                                        JSONArray coordinates = location.getJSONArray("coordinates");
                                                        Coordinates coords = new Coordinates(coordinates.getDouble(1), coordinates.getDouble(0));
                                                        MapMarkerNode node = new MapMarkerNode(new MapMarker(label, coords), origin.getLatitude(), origin.getLongitude());
                                                        if (node.getDistanceFromOrigin() <= 5000) {
                                                            bikeSpotsNearby.add(node);
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                tile.setNearestMarkers(bikeSpotsNearby);
                                                String value = Integer.toString(bikeSpotsNearby.size());
                                                tile.setValue(value);
                                                SharedPreferences.Editor editor = activity.prefs.edit();
                                                editor.putString(
                                                        getString(R.string.preference_key_bike_spots),
                                                        value
                                                );
                                                editor.commit();
                                                activity.adapter.notifyDataSetChanged();
                                            }
                                        }
                                );

                        requestManager.execute();
                    }
                }
        ));

        tiles.add(new MapMarkerInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_gp),
                getString(R.string.nearest_general_practitioner),
                getStoredPreferenceString(
                        getString(R.string.preference_key_general_practitioners),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MapMarkerInformationCard tile =
                                (MapMarkerInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarker()) {
                            return;
                        }
                        Coordinates coordinates =
                                ((MapMarkerInformationCard)
                                        ((MainActivity)context).data.getTile(positionInGrid)
                                ).getMarker().getCoordinates();
                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_destination), coordinates
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        getString(R.string.general_practitioner_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                MapMarkerInformationCard tile = (MapMarkerInformationCard)activity.data.getTile(positionInGrid);
                                                MapMarker nearestGeneralPractitioner = null;

                                                try {
                                                    JSONArray generalPractitioners = new JSONObject(result).getJSONArray("features");
                                                    double minDistance = Double.MAX_VALUE, distance;

                                                    for (int i = 0; i < generalPractitioners.length(); i++) {
                                                        JSONObject gp = generalPractitioners.getJSONObject(i);
                                                        JSONArray coordinatesJson =
                                                                gp.getJSONObject("geometry").getJSONArray("coordinates");
                                                        Coordinates coords = new Coordinates(coordinatesJson.getDouble(1), coordinatesJson.getDouble(0));
                                                        String label = gp.getJSONObject("properties").getString("titel");

                                                        LatLng from = new LatLng(origin.getLatitude(), origin.getLongitude());
                                                        LatLng to = new LatLng(coords.getLatitude(), coords.getLongitude());
                                                        distance = SphericalUtil.computeDistanceBetween(from, to);

                                                        if (distance < minDistance) {
                                                            minDistance = distance;
                                                            nearestGeneralPractitioner = new MapMarker(label, coords);
                                                        }
                                                    }

                                                    tile.setMarker(nearestGeneralPractitioner);
                                                    String value = String.format(String.format("%.2f", minDistance));
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            getString(R.string.preference_key_general_practitioners),
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
                }
        ));

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_population),
                getString(R.string.population),
                getStoredPreferenceString(
                        getString(R.string.preference_key_population),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(getString(R.string.intent_extra_key_title),
                                ((MainActivity) context).data.getTile(positionInGrid).getTitle());
                        intent.putExtra(getString(R.string.intent_extra_key_value),
                                ((MainActivity) context).data.getTile(positionInGrid).getValue());
                        context.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(Context context, int positionInGrid) {
                        RequestManager requestManager =
                                new RequestManager(
                                        context,
                                        getString(R.string.population_api_url),
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
                                                            getString(R.string.preference_key_population),
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
                }
        ));

        tiles.add(new InformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_sound_level),
                getString(R.string.sound),
                getStoredPreferenceString(
                        getString(R.string.preference_key_sound_level),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    MainActivity activity = (MainActivity)context;

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra(getString(R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle());
                        intent.putExtra(getString(R.string.intent_extra_key_value),
                                activity.data.getTile(positionInGrid).getValue());
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_SOUND_SENSOR);
                        Intent sensorConfigIntent = null;
                        try {
                            sensorConfigIntent= ExpressionManager
                                    .getSensor(context, MainActivity.SOUND_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        sensorConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        sensorConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                sensorConfigIntent
                        );
                        context.startActivity(intent);
                    }

                    @Override
                    public void resultHandler(final Context context, final int positionInGrid) {
                        ValueExpressionRegistrar.getInstance().register(
                                MainActivity.REQUEST_CODE_SOUND_SENSOR,
                                new SensorResultHandlers() {
                                    @Override
                                    public void onNewValues(String arg0, TimestampedValue[] arg1) {
                                        MainActivity activity = (MainActivity)context;
                                        InformationCard tile = activity.data.getTile(positionInGrid);

                                        if (arg1 != null && arg1.length > 0) {
                                            String value = String.format("%.2f", arg1[0].getValue());
                                            tile.setValue(value);
                                            SharedPreferences.Editor editor = activity.prefs.edit();
                                            editor.putString(
                                                    getString(R.string.preference_key_sound_level),
                                                    value
                                            );
                                            editor.commit();
                                            activity.adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                        );
                    }
                }
        ));

        tiles.add(new MapMarkerInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_glass_container),
                getString(R.string.trash_deposit),
                getStoredPreferenceString(
                        getString(R.string.preference_key_trash_containers),
                        getString(R.string.not_available)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MapMarkerInformationCard tile =
                                (MapMarkerInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarker()) {
                            return;
                        }
                        Coordinates coordinates =
                                ((MapMarkerInformationCard)
                                        ((MainActivity)context).data.getTile(positionInGrid)
                                ).getMarker().getCoordinates();
                        Intent intent = new Intent(context, RouteActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_destination), coordinates
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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

                    private  void processNewLocation(final int positionInGrid) {
                        RequestManager requestManager =
                                new RequestManager(
                                        context,
                                        getString(R.string.trash_container_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                MapMarkerInformationCard tile = (MapMarkerInformationCard)activity.data.getTile(positionInGrid);
                                                MapMarker nearestGlassContainer = null;

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
                                                                nearestGlassContainer = new MapMarker(label, coordinates);
                                                            }
                                                        }
                                                    }

                                                    tile.setMarker(nearestGlassContainer);
                                                    String value = String.format("%.2f", minDistance);
                                                    tile.setValue(value);
                                                    SharedPreferences.Editor editor = activity.prefs.edit();
                                                    editor.putString(
                                                            getString(R.string.preference_key_trash_containers),
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
                }
        ));

        tiles.add(new MultipleMarkersInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_ecopassages),
                getString(R.string.ecopassages),
                getStoredPreferenceString(
                        getString(R.string.preference_key_ecopassages),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MultipleMarkersInformationCard tile =
                                (MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarkers()) {
                            return;
                        }
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_coordinates),
                                ((MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid)).getMarkers(getInt(R.integer.max_maps_activity_markers))
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        getString(R.string.ecopassages_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                MultipleMarkersInformationCard tile = (MultipleMarkersInformationCard)activity.data.getTile(positionInGrid);
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
                                                            getString(R.string.preference_key_ecopassages),
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
                }
        ));

        tiles.add(new MultipleMarkersInformationCard(
                tiles.size(),
                context,
                getString(R.string.activity_title_monumental_trees),
                getString(R.string.monumental_trees),
                getStoredPreferenceString(
                        getString(R.string.preference_key_monumental_trees),
                        getString(R.string.zero)
                ),
                new InformationCardStrategy() {
                    Coordinates origin = new Coordinates();

                    @Override
                    public void onTileClickHandler(Context context, int positionInGrid) {
                        MainActivity activity = (MainActivity)context;
                        MultipleMarkersInformationCard tile =
                                (MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid);

                        if(!tile.hasMarkers()) {
                            return;
                        }

                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(getString(
                                        R.string.intent_extra_key_title),
                                activity.data.getTile(positionInGrid).getTitle()
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_coordinates),
                                ((MultipleMarkersInformationCard)
                                        activity.data.getTile(positionInGrid)).getMarkers(getInt(R.integer.max_maps_activity_markers))
                        );
                        intent.putExtra(getString(R.string.intent_extra_key_request_code),
                                MainActivity.REQUEST_CODE_LOCATION_SENSOR);
                        Intent locationConfigIntent = null;
                        try {
                            locationConfigIntent = ExpressionManager
                                    .getSensor(context, MainActivity.LOCATION_SENSOR_NAME)
                                    .getConfigurationIntent();
                        } catch (SwanException e) {
                            e.printStackTrace();
                        }
                        locationConfigIntent.putExtra(
                                "latitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_latitude_expression),
                                        getString(R.string.latitude_expression)
                                )
                        );
                        locationConfigIntent.putExtra(
                                "longitude_expression",
                                activity.prefs.getString(
                                        getString(R.string.preference_key_longitude_expression),
                                        getString(R.string.longitude_expression)
                                )
                        );
                        intent.putExtra(
                                getString(R.string.intent_extra_key_sensor_config_intent),
                                locationConfigIntent
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
                                        getString(R.string.monumental_trees_api_url),
                                        origin,
                                        new RequestManagerHandlers() {
                                            @Override
                                            public void onPostExecute(Context context, String result) {
                                                MainActivity activity = (MainActivity)context;
                                                MultipleMarkersInformationCard tile = (MultipleMarkersInformationCard)activity.data.getTile(positionInGrid);
                                                OrderedMapMarkerList treesNearby = new OrderedMapMarkerList();

                                                try {
                                                    JSONObject jObj = new JSONObject(result);
                                                    JSONArray features = jObj.getJSONArray("features");
                                                    for (int i = 0; i < features.length(); i++) {
                                                        JSONObject treeObj = features.getJSONObject(i);
                                                        String label = treeObj.getJSONObject("properties").getString("title");
                                                        JSONArray coords = treeObj.getJSONObject("geometry").getJSONArray("coordinates");
                                                        Coordinates coordinates = new Coordinates(coords.getDouble(1), coords.getDouble(0));
                                                        MapMarkerNode node = new MapMarkerNode(
                                                                new MapMarker(label, coordinates),
                                                                origin.getLatitude(),
                                                                origin.getLongitude()
                                                        );

                                                        if(node.getDistanceFromOrigin() < 5000) {
                                                            treesNearby.add(node);
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                tile.setNearestMarkers(treesNearby);
                                                String value = Integer.toString(treesNearby.size());
                                                tile.setValue(value);
                                                SharedPreferences.Editor editor = activity.prefs.edit();
                                                editor.putString(
                                                        getString(R.string.preference_key_monumental_trees),
                                                        value
                                                );
                                                editor.commit();
                                                activity.adapter.notifyDataSetChanged();
                                            }
                                        }
                                );

                        requestManager.execute();
                    }
                }
        ));
    }

    private String getString(int id) {
        return context.getResources().getString(id);
    }
    private int getInt(int id) {
        return context.getResources().getInteger(id);
    }
    private String getStoredPreferenceString(String key, String defaultValue) {
        return ((MainActivity)context).prefs.getString(key, defaultValue);
    }
}
