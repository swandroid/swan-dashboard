package acba.acbaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapMarkerNode[] markers;
    private Intent locationSensorConfigurationIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString(getString(R.string.intent_extra_key_title)));
        markers = (MapMarkerNode[])extras.getSerializable(getString(R.string.intent_extra_key_coordinates));
        locationSensorConfigurationIntent =
                (Intent)extras.get(getString(R.string.intent_extra_key_sensor_config_intent));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final ScrollView scrollView = (ScrollView)findViewById(R.id.mapScrollView);
        ViewTreeObserver vto = scrollView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16)
                    scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                else
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = scrollView.getMeasuredHeight();

                ViewGroup.LayoutParams lp = mapFragment.getView().getLayoutParams();
                lp.height = height;
                mapFragment.getView().setLayoutParams(lp);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng marker;
        Coordinates coordinates;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for(MapMarkerNode m : markers) {
            coordinates = m.getMarker().getCoordinates();
            marker = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
            mMap.addMarker((new MarkerOptions().position(marker).title(m.getMarker().getLabel())));
            boundsBuilder.include(marker);
        }

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen


        LatLngBounds bounds = boundsBuilder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_location_card, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = locationSensorConfigurationIntent;
        String expression;

        if(item.getItemId() == R.id.latitude_settings) {
            expression = intent.getStringExtra("latitude_expression");
            intent.putExtra("expression", expression);

            startActivityForResult(
                    intent,
                    MainActivity.REQUEST_CODE_LATITUDE_SENSOR
            );

            return true;
        }

        if(item.getItemId() == R.id.longitude_settings) {
            expression = intent.getStringExtra("longitude_expression");
            intent.putExtra("expression", expression);

            startActivityForResult(
                    intent,
                    MainActivity.REQUEST_CODE_LONGITUDE_SENSOR
            );

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, 0);
            SharedPreferences.Editor editor = prefs.edit();

            if(requestCode == MainActivity.REQUEST_CODE_LATITUDE_SENSOR) {
                editor.putString(
                        getString(R.string.preference_key_latitude_expression),
                        data.getStringExtra(getString(R.string.latitude_expression))
                );
            } else if(requestCode == MainActivity.REQUEST_CODE_LONGITUDE_SENSOR) {
                editor.putString(
                        getString(R.string.preference_key_longitude_expression),
                        data.getStringExtra(getString(R.string.longitude_expression))
                );
            }

            editor.commit();
        }
    }
}
