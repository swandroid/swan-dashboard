package swan.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import acba.acbaapp.InformationCard;
import acba.acbaapp.InformationCardsData;
import acba.acbaapp.ValueExpressionRegistrar;

public class DashboardActivity extends AppCompatActivity {

    LinearLayout groupDistance;
    LinearLayout groupCount;

    public SensorsAdapter adapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

//
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
//            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
//            ab.setDisplayHomeAsUpEnabled(true);
            ab.setIcon(getResources().getDrawable(R.drawable.swanlake));
        }

        groupCount = (LinearLayout) findViewById(R.id.scroll_view_group1);
        groupDistance = (LinearLayout) findViewById(R.id.scroll_view_group2);
        initialize();
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
        for(int i=0; i<InformationCardsData.getInstance(this).size(); i++) {
            InformationCard card = InformationCardsData.getInstance(this).getTile(i);
            switch (card.getTileType()) {
                case InformationCardsData.TILE_TYPE_GROUP_COUNT:
                    View itemView = LayoutInflater.from(this).inflate(R.layout.card_view_header, groupCount, false);
                    loadCard(itemView, card);
                    groupCount.addView(itemView);
                    break;
                case InformationCardsData.TILE_TYPE_GROUP_DISTANCE:
                    itemView = LayoutInflater.from(this).inflate(R.layout.card_view_header, groupDistance, false);
                    loadCard(itemView, card);
                    groupDistance.addView(itemView);
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
        adapter = new SensorsAdapter(this, defaultCards);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        if (recyclerView != null) {
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(adapter);
        }
    }

    public void loadCard(View view, InformationCard card) {
        TextView description = (TextView) view.findViewById(R.id.itemDescriptionTextView);
        TextView value = (TextView) view.findViewById(R.id.itemValueTextView);
        ImageView image = (ImageView) view.findViewById(R.id.imageView);

        description.setText(card.getDescription());
        value.setText(card.getValue());
        image.setImageResource(card.getImageResourceId());
    }

}
