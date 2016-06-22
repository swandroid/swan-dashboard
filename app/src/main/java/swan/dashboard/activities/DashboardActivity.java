package swan.dashboard.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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

    RecyclerView groupDistance;
    RecyclerView groupCount;

    public SensorsAdapter adapter;
    public SensorsAdapter adapter1;
    public SensorsAdapter adapter2;

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

    private List<InformationCard> defaultCards;
    private List<InformationCard> groupCards1;
    private List<InformationCard> groupCards2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

//
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setIcon(getResources().getDrawable(R.drawable.swanlake));
        }

        groupCount = (RecyclerView) findViewById(R.id.scroll_view_group1);
        groupDistance = (RecyclerView) findViewById(R.id.scroll_view_group2);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ValueExpressionRegistrar.getInstance(this).unregisterAll();
    }

    public void showInfo() {
        String url = "http://www.cs.vu.nl/SWAN/";
        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void initialize() {

        defaultCards = new ArrayList<>();
        groupCards1 = new ArrayList<>();
        groupCards2 = new ArrayList<>();

        for (int i = 0; i < InformationCardsData.getInstance(this).size(); i++) {
            InformationCard card = InformationCardsData.getInstance(this).getTile(i);
            switch (card.getTileType()) {
                case InformationCardsData.TILE_TYPE_GROUP_COUNT:
                    groupCards1.add(card);
                    break;
                case InformationCardsData.TILE_TYPE_GROUP_DISTANCE:
                    groupCards2.add(card);
                    break;
                case InformationCardsData.TILE_TYPE_NORMAL:
                    defaultCards.add(card);
                    break;
            }
            card.process();
        }

        initializeGridView();
        ValueExpressionRegistrar.getInstance(this).start();
    }

    public void initializeGridView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        adapter = new SensorsAdapter(this, defaultCards, VIEW_TYPE_CARD);
        adapter1 = new SensorsAdapter(this, groupCards1, VIEW_TYPE_CARD_GROUP);
        adapter2 = new SensorsAdapter(this, groupCards2, VIEW_TYPE_CARD_GROUP);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        if (recyclerView != null) {
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(adapter);
        }
        if (groupCount != null) {
            groupCount.setAdapter(adapter1);
        }
        if (groupDistance != null) {
            groupDistance.setAdapter(adapter2);
        }
    }

    Thread uiThread = new Thread() {
        @Override
        public void run() {

            while(!interrupted()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        adapter1.notifyDataSetChanged();
                        adapter2.notifyDataSetChanged();
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
