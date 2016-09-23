package monsieurwave.earguard;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private ToggleButton toggleButton1;
    private TextView mainMessage;
    private TextView instantExposure;

    public Intent CheckNoiseServiceIntent;
    public double zero;
    public double powZero;
//    public String calibPref;
    public boolean recRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);
        Log.w("MainActivity", "onCreate called");

//        Initialise buttons and listener
        addListenerOnButton();
        toggleButton1.setChecked(true);

//        Initialise receiver to get data from CheckNoiseService
        registerReceiver(uiUpdated, new IntentFilter("AMP_UPDATED"));

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("MainActivity", "onStart called");

        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        double defaultZeroValue = 1;
        this.zero = getPref(sharedPref, "CalibratedZero", defaultZeroValue);
        this.powZero = getPref(sharedPref, "CalibratedPowZero", defaultZeroValue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uiUpdated);
    }


    public void addListenerOnButton() {

        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton1.setBackgroundResource(R.drawable.button);
        mainMessage = (TextView) findViewById(R.id.mainMessage);

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Log.w("THresssj","");

//                    Check if noise threshold is valid
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                try {

                    double threshold = new Double(preferences.getString("dBthreshold", "80"));

                    Log.w("THresssj",Double.toString(threshold));
                } catch (Exception e){
                    instantExposure = (TextView) findViewById(R.id.instantExposure);
                    instantExposure.setText("Invalid Noise threshold");
                    return;
                }

                MainActivity.this.CheckNoiseServiceIntent = new Intent(MainActivity.this, CheckNoiseService.class);
                MainActivity.this.CheckNoiseServiceIntent.putExtra("zero", MainActivity.this.zero);
                MainActivity.this.CheckNoiseServiceIntent.putExtra("powZero", MainActivity.this.powZero);

                if (isChecked) {
                    // The toggle is actually disabled
                    stopService(MainActivity.this.CheckNoiseServiceIntent);

                    recRunning = false;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("recRunning", recRunning);
                    editor.commit();

                    mainMessage.setVisibility(View.VISIBLE);

                } else {
                    // The toggle is actually enabled

                    MainActivity.this.CheckNoiseServiceIntent.putExtra("zero", MainActivity.this.zero);
                    MainActivity.this.CheckNoiseServiceIntent.putExtra("powZero", MainActivity.this.powZero);
                    startService(CheckNoiseServiceIntent);

                    recRunning = true;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("recRunning", recRunning);
                    editor.commit();

                    mainMessage.setVisibility(View.GONE);
                }
            }


        });

    }


    //    Get calibrated zero from sharedPreferences
    double getPref(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public void accessSettings(View view){

        Intent SettingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingsActivityIntent);
    }

//Get Data from CheckNoiseService
    private BroadcastReceiver uiUpdated= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("MainActivity",intent.getExtras().getString("dbAmp"));

            String outputString = intent.getExtras().getString("dbAmp");
            instantExposure = (TextView) findViewById(R.id.instantExposure);
            instantExposure.setText(outputString);
        }
    };



} // End of MainActivity

