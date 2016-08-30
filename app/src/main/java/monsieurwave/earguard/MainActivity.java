package monsieurwave.earguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private Button calibrationButton;
    private TextView mainMessage;
    private TextView instantExposure;

    public Calibration calibration = new Calibration(this);
    public Intent CheckNoiseServiceIntent;
    public double zero;
    public double powZero;

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
        calibrationButton = (Button) findViewById(R.id.calibActButton);
        mainMessage = (TextView) findViewById(R.id.mainMessage);

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.CheckNoiseServiceIntent = new Intent(MainActivity.this, CheckNoiseService.class);
                MainActivity.this.CheckNoiseServiceIntent.putExtra("zero", MainActivity.this.zero);
                MainActivity.this.CheckNoiseServiceIntent.putExtra("powZero", MainActivity.this.powZero);

                if (isChecked) {
                    // The toggle is actually disabled
                    stopService(MainActivity.this.CheckNoiseServiceIntent);

                    mainMessage.setVisibility(View.VISIBLE);

                } else {
                    // The toggle is actually enabled
                    MainActivity.this.CheckNoiseServiceIntent.putExtra("zero", MainActivity.this.zero);
                    MainActivity.this.CheckNoiseServiceIntent.putExtra("powZero", MainActivity.this.powZero);
                    startService(CheckNoiseServiceIntent);

                    mainMessage.setVisibility(View.GONE);
                }
            }


        });

    }


    //    Get calibrated zero from sharedPreferences
    double getPref(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public void calibrate(View view) {

        if(!calibration.isAlive()){
            calibration = new Calibration(this);
            calibration.start();
        } else {
            calibration.interrupt();
            calibration = new Calibration(this);
            calibration.start();
        }

//        Wait for calibration to end
        try {
            calibration.join();
        } catch (InterruptedException e) {
            Log.w("Recording : ", e);
            return;
        }

//       Get new zero into MainActivity.this.zero variable
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        double defaultZeroValue = 1;
        MainActivity.this.zero = getPref(sharedPref, "CalibratedZero", defaultZeroValue);
        MainActivity.this.powZero = getPref(sharedPref, "CalibratedPowZero", defaultZeroValue);

        return;
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

