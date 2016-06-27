package swan.dashboard.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import java.util.ArrayList;
import java.util.List;

import swan.dashboard.sensors.InformationCard;
import swan.dashboard.sensors.InformationCardsData;
import swan.dashboard.services.ValueExpressionRegistrar;
import swan.dashboard.R;
import swan.dashboard.adapters.SensorsAdapter;

public class DashboardActivity extends AppCompatActivity {

    public static final int VIEW_TYPE_CARD = 0;
    public static final int VIEW_TYPE_CARD_GROUP = 1;

    public static final int REQUEST_CODE_SCREEN_SENSOR = 667,
            REQUEST_CODE_WIFI_SENSOR = 668,
            REQUEST_CODE_STEP_COUNTER_SENSOR = 669,
            REQUEST_CODE_SOUND_SENSOR = 670,
            REQUEST_CODE_LOCATION_SENSOR = 671,
            REQUEST_CODE_LATITUDE_SENSOR = 672,
            REQUEST_CODE_LONGITUDE_SENSOR = 673;

    public static final String SCREEN_SENSOR_NAME = "screen",
            WIFI_SENSOR_NAME = "wifi",
            STEP_COUNTER_SENSOR_NAME = "step_counter",
            SOUND_SENSOR_NAME = "sound",
            LOCATION_SENSOR_NAME = "location";

    private RecyclerView mDistanceRecyclerView;
    private RecyclerView mCountRecyclerView;
    private RecyclerView mDefaultRecyclerView;

    private SensorsAdapter mDefaultAdapter;
    private SensorsAdapter mCountAdapter;
    private SensorsAdapter mDistanceAdapter;

    private List<InformationCard> mDefaultCardsList;
    private List<InformationCard> mCountCardsList;
    private List<InformationCard> mDistanceCardsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setIcon(getResources().getDrawable(R.drawable.swanlake));
        }

        initialize();

        uiThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ValueExpressionRegistrar.getInstance(this).unregisterAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ValueExpressionRegistrar.getInstance(this).start();
    }

    public void showInfo() {
        String url = getString(R.string.swan_website);
        Uri uri = Uri.parse(url);

        Intent intent = new Intent();
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    public void initialize() {

        mCountRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_count);
        mDistanceRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_distance);
        mDefaultRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_default);

        mDefaultCardsList = new ArrayList<>();
        mCountCardsList = new ArrayList<>();
        mDistanceCardsList = new ArrayList<>();

        // Process cards data
        for (int i = 0; i < InformationCardsData.getInstance(this).size(); i++) {
            InformationCard card = InformationCardsData.getInstance(this).getTile(i);
            switch (card.getTileType()) {
                case InformationCardsData.TILE_TYPE_GROUP_COUNT:
                    mCountCardsList.add(card);
                    break;
                case InformationCardsData.TILE_TYPE_GROUP_DISTANCE:
                    mDistanceCardsList.add(card);
                    break;
                case InformationCardsData.TILE_TYPE_NORMAL:
                    mDefaultCardsList.add(card);
                    break;
            }
            card.process();
        }

        setupAdapters();
    }

    public void setupAdapters() {

        mDefaultAdapter = new SensorsAdapter(this, mDefaultCardsList, VIEW_TYPE_CARD);
        mCountAdapter = new SensorsAdapter(this, mCountCardsList, VIEW_TYPE_CARD_GROUP);
        mDistanceAdapter = new SensorsAdapter(this, mDistanceCardsList, VIEW_TYPE_CARD_GROUP);

        if (mDefaultRecyclerView != null) {
            mDefaultRecyclerView.setAdapter(mDefaultAdapter);
        }
        if (mCountRecyclerView != null) {
            mCountRecyclerView.setAdapter(mCountAdapter);
        }
        if (mDistanceRecyclerView != null) {
            mDistanceRecyclerView.setAdapter(mDistanceAdapter);
        }
    }

    Thread uiThread = new Thread() {
        @Override
        public void run() {

            while (!interrupted()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDefaultAdapter.notifyDataSetChanged();
                        mCountAdapter.notifyDataSetChanged();
                        mDistanceAdapter.notifyDataSetChanged();
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    };

}
