package acba.acbaapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;

public class RouteActivity extends Activity {

    private WebView mWebView;
    private Intent locationSensorConfigurationIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra(getString(R.string.intent_extra_key_title)));
        locationSensorConfigurationIntent =
                (Intent)intent.getExtras().get(
                        getString(R.string.intent_extra_key_sensor_config_intent)
                );

        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        final ScrollView scrollView = (ScrollView)findViewById(R.id.routeScrollView);
        ViewTreeObserver vto = scrollView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16)
                    scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                else
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = scrollView.getMeasuredHeight();

                ViewGroup.LayoutParams lp = mWebView.getLayoutParams();
                lp.height = height;
                mWebView.setLayoutParams(lp);
            }
        });

        Coordinates coordinates =
                (Coordinates)intent.getExtras().getSerializable(
                        getString(R.string.intent_extra_key_destination)
                );
        String url = String.format(
                getString(R.string.maps_direction_url),
                coordinates.getLatitude(),
                coordinates.getLongitude()
        );
        mWebView.loadUrl(url);
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
