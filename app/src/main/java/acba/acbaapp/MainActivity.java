//package acba.acbaapp;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.View;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import android.widget.GridView;
//
//import java.util.ArrayList;
//
//import interdroid.swancore.swanmain.ExpressionManager;
//import interdroid.swan.SensorInfo;
//import interdroid.swancore.swanmain.SwanException;
//import swan.dashboard.R;
//
//public class MainActivity extends Activity {
//
//    public static final String TAG = "ACBAApp",
//            PREFS_NAME = "ACBAAppPrefs";
//
//    public static final String SCREEN_SENSOR_NAME = "screen",
//            WIFI_SENSOR_NAME = "wifi",
//            STEP_COUNTER_SENSOR_NAME = "step_counter",
//            SOUND_SENSOR_NAME = "sound",
//            LOCATION_SENSOR_NAME = "location";
//
//    /* random id */
//    public static final int REQUEST_CODE_SCREEN_SENSOR = 667,
//            REQUEST_CODE_WIFI_SENSOR = 668,
//            REQUEST_CODE_STEP_COUNTER_SENSOR = 669,
//            REQUEST_CODE_SOUND_SENSOR = 670,
//            REQUEST_CODE_LOCATION_SENSOR = 671,
//            REQUEST_CODE_LATITUDE_SENSOR = 672,
//            REQUEST_CODE_LONGITUDE_SENSOR = 673;
//
//    SharedPreferences prefs;
//
//    GridView gridView;
//    GridViewItemAdapter adapter;
//
//    public InformationCardsData data;
//
////    private ProgressDialog progressDialog;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        prefs = getSharedPreferences(PREFS_NAME, 0);
//
//        //Remove title bar
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        //Remove notification bar
//        this.getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );
//
//        setContentView(R.layout.activity_main);
//
//        initialize();
//    }
//
//    public void initialize() {
//        ValueExpressionRegistrar registrar = ValueExpressionRegistrar.getInstance();
//        registrar.initialize(MainActivity.this);
//        data = InformationCardsData.getInstance();
//        data.initialize(MainActivity.this);
//        initializeGridView();
//
////        if(!isNetworkAvailable()) {
////            AlertDialog.Builder builder = new AlertDialog.Builder(this);
////            builder.setTitle(getString(R.string.alert_dialog_title))
////                    .setMessage(getString(R.string.alert_dialog_text))
////                    .setCancelable(false)
////                    .setPositiveButton(getString(R.string.alert_dialog_button), new DialogInterface.OnClickListener() {
////                        public void onClick(DialogInterface dialog, int id) {
////                            finish();
////                        }
////                    });
////            AlertDialog alert = builder.create();
////            alert.show();
////        } else {
//
//        for(int i=0; i<data.size(); i++) {
//            data.getTile(i).process();
//        }
//
//        ValueExpressionRegistrar.getInstance().start();
//    }
//
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }
//
//    /* Invoked on pressing back key from the sensor configuration activity */
////    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
////
////        super.onActivityResult(requestCode, resultCode, data);
////
////        if (resultCode == RESULT_OK) {
////
////            String myExpression;
////
////            switch (requestCode) {
////                case REQUEST_CODE_SCREEN_SENSOR:
////                    myExpression = data.getStringExtra(getString(R.string.expression));
////                    /*Based on sensor configuration an expression will be created*/
////                    Log.d(TAG, "expression: " + myExpression);
////
////                    screenExpression = myExpression;
////
////                    registerScreenSensor(screenExpression);
////
////                    break;
////                case REQUEST_CODE_WIFI_SENSOR:
////                    myExpression = data.getStringExtra(getString(R.string.expression));
////					/*Based on sensor configuration an expression will be created*/
////                    Log.d(TAG, "expression: " + myExpression);
////
////                    wifiExpression = myExpression;
////
////                    registerWifiSensor(wifiExpression);
////
////                    break;
////                case REQUEST_CODE_STEP_COUNTER_SENSOR:
////                    myExpression = data.getStringExtra(getString(R.string.expression));
////					/*Based on sensor configuration an expression will be created*/
////                    Log.d(TAG, "expression: " + myExpression);
////
////                    stepCounterExpression = myExpression;
////
////                    startActivityForResult(soundSensor.getConfigurationIntent(),
////                            REQUEST_CODE_SOUND_SENSOR);
////                    break;
////                case REQUEST_CODE_SOUND_SENSOR:
////                    myExpression = data.getStringExtra(getString(R.string.expression));
////					/*Based on sensor configuration an expression will be created*/
////                    Log.d(TAG, "expression: " + myExpression);
////
////                    soundExpression = myExpression;
////
////                    registerSoundSensor(soundExpression);
////
////                    break;
////                default:
////                    break;
////            }
////        }
////
////
////    }
//
////    public GridViewItemAdapter.ViewHolder getViewAtPosition(int position) {
////        int firstPosition = this.gridView.getFirstVisiblePosition();
////        int lastPosition = this.gridView.getLastVisiblePosition();
////
////        if (lastPosition < 0) {
////            lastPosition = 0;
////        }
////        if ((position < firstPosition) || (position > lastPosition)) {
////            return null;
////        }
////
////
////        return (GridViewItemAdapter.ViewHolder) this
////                .gridView.getChildAt(position - firstPosition).getTag();
////    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        ValueExpressionRegistrar.getInstance().unregister(
//                new int[]
//                        {
////                                REQUEST_CODE_LOCATION_SENSOR,
////                                REQUEST_CODE_SOUND_SENSOR
//                        }
//        );
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        ValueExpressionRegistrar.getInstance().reregister(
//                new int[]
//                        {
////                                REQUEST_CODE_LOCATION_SENSOR,
////                                REQUEST_CODE_SOUND_SENSOR
//                        }
//        );
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        ValueExpressionRegistrar.getInstance().unregister(
//                new int[]
//                        {
////                                REQUEST_CODE_LOCATION_SENSOR,
////                                REQUEST_CODE_SOUND_SENSOR
//                        }
//        );
//    }
//
//    public void initializeGridView() {
////        gridView = (GridView) findViewById(R.id.content);
////        adapter = new GridViewItemAdapter(this, data);
////
////        gridView.setAdapter(adapter);
////
////        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
////            @Override
////            public void onScrollStateChanged(AbsListView view, int scrollState) {
////                if (scrollState == SCROLL_STATE_IDLE) {
////                    View itemView = view.getChildAt(0);
////                    int top = Math.abs(itemView.getTop());
////                    int bottom = Math.abs(itemView.getBottom());
////                    int scrollBy = top >= bottom ? bottom : -top;
////                    if (scrollBy == 0) {
////                        return;
////                    }
////                    smoothScrollDeferred(scrollBy, (GridView) view);
////                }
////            }
////
////            @Override
////            public void onScroll(
////                    AbsListView view,
////                    int firstVisibleItem,
////                    int visibleItemCount,
////                    int totalItemCount) {
////
////            }
////
////            private void smoothScrollDeferred(final int scrollByF,
////                                              final GridView viewF) {
////                final Handler h = new Handler();
////                h.post(new Runnable() {
////
////                    @Override
////                    public void run() {
////                        // TODO Auto-generated method stub
////                        viewF.smoothScrollBy(scrollByF, 500);
////                    }
////                });
////            }
////        });
////
////        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////            @Override
////            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                data.getTile(position).executeOnClickHandler();
////            }
////        });
//    }
//}
