package acba.acbaapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailsActivity extends Activity {

    private int mSensorRequestCode;
    private Intent mSensorConfigurationIntent;
    private String mStoredPreferenceExpressionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        TextView tv = (TextView)findViewById(R.id.infoCardValue);
        tv.setText(intent.getStringExtra(getString(R.string.intent_extra_key_value)));

        mSensorRequestCode =
                intent.getIntExtra(getString(R.string.intent_extra_key_request_code), 0);

        if(intent.hasExtra(getString(R.string.intent_extra_key_sensor_config_intent))) {
            mSensorConfigurationIntent =
                    (Intent)intent.getExtras().get(
                            getString(R.string.intent_extra_key_sensor_config_intent)
                    );
        }

        mStoredPreferenceExpressionKey =
                intent.getStringExtra(getString(R.string.intent_extra_key_stored_preference_key));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mSensorRequestCode > 0 && mSensorConfigurationIntent != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_card, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sensor_settings) {
            startActivityForResult(
                    mSensorConfigurationIntent,
                    mSensorRequestCode
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
            editor.putString(
                    mStoredPreferenceExpressionKey,
                    data.getStringExtra(getString(R.string.expression))
            );
            editor.commit();
        }
    }
}
