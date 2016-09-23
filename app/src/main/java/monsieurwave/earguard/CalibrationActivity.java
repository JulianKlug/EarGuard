package monsieurwave.earguard;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by julian on 9/23/16.
 */
public class CalibrationActivity extends Activity {

    private Button calibrationButton;

    public boolean recRunning;
    public double zero;
    public double powZero;
    public Calibration calibration = new Calibration(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_calibration);
    }

    @Override
    protected void onStart() {
        super.onStart();


        SharedPreferences sharedPref = CalibrationActivity.this.getPreferences(Context.MODE_PRIVATE);
        double defaultZeroValue = 1;
        this.zero = getPref(sharedPref, "CalibratedZero", defaultZeroValue);
        this.powZero = getPref(sharedPref, "CalibratedPowZero", defaultZeroValue);
        this.recRunning = sharedPref.getBoolean("recRunning", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void addListenerOnButton() {

        calibrationButton = (Button) findViewById(R.id.calibActButton);
    }


    public void calibrate(View view) {

//        Check if still recording

//        if(recRunning) {
//            stopService(MainActivity.this.CheckNoiseServiceIntent);
//            recRunning = false;
//
//            mainMessage = (TextView) findViewById(R.id.mainMessage);
//            mainMessage.setVisibility(View.VISIBLE);
//        }

        if(!recRunning) {


            if (!calibration.isAlive()) {
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
            SharedPreferences sharedPref = CalibrationActivity.this.getPreferences(Context.MODE_PRIVATE);
            double defaultZeroValue = 1;
            CalibrationActivity.this.zero = getPref(sharedPref, "CalibratedZero", defaultZeroValue);
            CalibrationActivity.this.powZero = getPref(sharedPref, "CalibratedPowZero", defaultZeroValue);

        };
        return;
    }

    //    Get calibrated zero from sharedPreferences
    double getPref(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
