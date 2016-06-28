package acba.acbaapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import acba.informationcards.BikeSpotsInformationCard;
import acba.informationcards.EcopassagesInformationCard;
import acba.informationcards.FarmsInformationCard;
import acba.informationcards.GeneralPractitionerInformationCard;
import acba.informationcards.ParkingSpotsInformationCard;
import acba.informationcards.PopularSongInformationCard;
import acba.informationcards.PublicUrinalsInformationCard;
import acba.informationcards.ReligiousMeetingPointsInformationCard;
import acba.informationcards.ScreenChecksInformationCard;
import acba.informationcards.SoundLevelInformationCard;
import acba.informationcards.TrashContainersInformationCard;
import acba.informationcards.TreesInformationCard;
import acba.informationcards.WifiStationsInformationCard;
import interdroid.swan.ExpressionManager;
import interdroid.swan.SwanException;
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

        tiles.add(new ScreenChecksInformationCard(tiles.size(), context));

        tiles.add(new ParkingSpotsInformationCard(tiles.size(), context));

//        tiles.add(new InformationCard(
//                tiles.size(),
//                context,
//                getString(R.string.activity_title_kilometers_traveled),
//                getString(R.string.distance_traveled),
//                getStoredPreferenceString(
//                        getString(R.string.preference_key_distance_traveled),
//                        getString(R.string.zero)
//                ),
//                new InformationCardStrategy() {
//                    @Override
//                    public void onTileClickHandler(Context context, int positionInGrid) {
//
//                    }
//
//                    @Override
//                    public void resultHandler(Context context, int positionInGrid) {
//
//                    }
//                }
//        ));

        tiles.add(new PopularSongInformationCard(tiles.size(), context));

        tiles.add(new WifiStationsInformationCard(tiles.size(), context));

        tiles.add(new PublicUrinalsInformationCard(tiles.size(), context));

//        tiles.add(new InformationCard(
//                tiles.size(),
//                context,
//                "",
//                getString(R.string.product_price_abroad),
//                getStoredPreferenceString(
//                        getString(R.string.preference_key_product_price),
//                        getString(R.string.not_available)
//                ),
//                new InformationCardStrategy() {
//                    @Override
//                    public void onTileClickHandler(Context context, int positionInGrid) {
//
//                    }
//
//                    @Override
//                    public void resultHandler(Context context, int positionInGrid) {
//
//                    }
//                }
//        ));

        tiles.add(new ReligiousMeetingPointsInformationCard(tiles.size(), context));

        tiles.add(new FarmsInformationCard(tiles.size(), context));

        tiles.add(new BikeSpotsInformationCard(tiles.size(), context));

        tiles.add(new GeneralPractitionerInformationCard(tiles.size(), context));

        tiles.add(new PopularSongInformationCard(tiles.size(), context));

        tiles.add(new SoundLevelInformationCard(tiles.size(), context));

        tiles.add(new TrashContainersInformationCard(tiles.size(), context));

        tiles.add(new EcopassagesInformationCard(tiles.size(), context));

        tiles.add(new TreesInformationCard(tiles.size(), context));
    }
}
