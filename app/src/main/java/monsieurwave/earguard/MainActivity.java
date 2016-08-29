package monsieurwave.earguard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity {

    private ToggleButton toggleButton1;
    private Button calibrationButton;
    Calibration calibration = new Calibration(this);

    public Intent CheckNoiseServiceIntent;
    public double zero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);
        Log.w("MainActivity", "onCreate called");
        addListenerOnButton();
        toggleButton1.setChecked(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("MainActivity", "onStart called");

        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        double defaultZeroValue = 1;
        this.zero = getZero(sharedPref, "CalibratedZero", defaultZeroValue);

    }

    public void addListenerOnButton() {

        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton1.setBackgroundResource(R.drawable.button);
        calibrationButton = (Button) findViewById(R.id.calibActButton);

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.CheckNoiseServiceIntent = new Intent(MainActivity.this, CheckNoiseService.class);
                MainActivity.this.CheckNoiseServiceIntent.putExtra("zero", MainActivity.this.zero);

                if (isChecked) {
                    // The toggle is actually disabled
                    stopService(MainActivity.this.CheckNoiseServiceIntent);


                } else {
                    // The toggle is actually enabled
                    MainActivity.this.CheckNoiseServiceIntent.putExtra("zero", MainActivity.this.zero);
                    startService(CheckNoiseServiceIntent);



                }
            }


        });

    }



    //    Get calibrated zero from sharedPreferences
    double getZero(final SharedPreferences prefs, final String key, final double defaultValue) {
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
        MainActivity.this.zero = getZero(sharedPref, "CalibratedZero", defaultZeroValue);

        return;

    }




}

