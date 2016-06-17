package acba.acbaapp;

import android.content.Context;
import java.util.ArrayList;
import swan.dashboard.sensors.impl.BikeSpotsSensor;
import swan.dashboard.sensors.impl.EcopassagesSensor;
import swan.dashboard.sensors.impl.MilkFarmSensor;
import swan.dashboard.sensors.impl.MonumentalTreesSensor;
import swan.dashboard.sensors.impl.NearestGPSensor;
import swan.dashboard.sensors.impl.ParkingSpotsSensor;
import swan.dashboard.sensors.impl.PopularSongSensor;
import swan.dashboard.sensors.impl.PopulationCountSensor;
import swan.dashboard.sensors.impl.PublicUrinalSensor;
import swan.dashboard.sensors.impl.ReligiousMeetingPointsSensor;
import swan.dashboard.sensors.impl.ScreenChecksSensor;
import swan.dashboard.sensors.impl.SoundLevelSensor;
import swan.dashboard.sensors.impl.TrashContainerSensor;
import swan.dashboard.sensors.impl.WifiStationsSensor;

/**
 * Created by Alex on 24-May-16.
 */
public class InformationCardsData {
    public static final int TILE_TYPE_NORMAL = 0;
    public static final int TILE_TYPE_GROUP = 1;

    private static InformationCardsData instance = null;
    private ArrayList<InformationCard> tiles = null;

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
        tiles.add(new ScreenChecksSensor(tiles.size(), context));
        tiles.add(new ParkingSpotsSensor(tiles.size(), context));
        tiles.add(new PopularSongSensor(tiles.size(),context));
        tiles.add(new WifiStationsSensor(tiles.size(), context));
        tiles.add(new PublicUrinalSensor(tiles.size(), context));
        tiles.add(new ReligiousMeetingPointsSensor(tiles.size(), context));
        tiles.add(new MilkFarmSensor(tiles.size(), context));
        tiles.add(new BikeSpotsSensor(tiles.size(), context));
        tiles.add(new NearestGPSensor(tiles.size(), context));
        tiles.add(new PopulationCountSensor(tiles.size(), context));
        tiles.add(new SoundLevelSensor(tiles.size(), context));
        tiles.add(new TrashContainerSensor(tiles.size(), context));
        tiles.add(new EcopassagesSensor(tiles.size(), context));
        tiles.add(new MonumentalTreesSensor(tiles.size(), context));
    }
}
