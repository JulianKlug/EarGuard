package monsieurwave.earguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by julian on 9/23/16.
 */
public class CalibrationActivity extends Activity {

    private Button calibrationButton;
    private TextView functionDisplay;

    private double dBinput;
    public boolean recRunning;
    public double zero, powZero;
    public Calibration calibration = new Calibration(this, "none", "none");

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

        functionDisplay = (TextView) findViewById(R.id.Function);
        functionDisplay.setVisibility(View.GONE);


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

        getCalibratedValue(view, "CalibratedZero", "CalibratedPowZero");

        //       Get new zero into MainActivity.this.zero variable
        SharedPreferences sharedPref = CalibrationActivity.this.getPreferences(Context.MODE_PRIVATE);
        double defaultZeroValue = 1;
        CalibrationActivity.this.zero = getPref(sharedPref, "CalibratedZero", defaultZeroValue);
        CalibrationActivity.this.powZero = getPref(sharedPref, "CalibratedPowZero", defaultZeroValue);

    }

    public void measure1 (View view) {
        getCalibratedValue(view, "Measure1", "none");

        setInput("dBInput1", "Enter current noise level in dB");
    }

    public void measure2 (View view) {
        getCalibratedValue(view, "Measure2", "none");

        setInput("dBInput2", "Enter current noise level in dB");

    }

    public void compute (View view) {
        SharedPreferences preferences = CalibrationActivity.this.getPreferences(Context.MODE_PRIVATE);
        final double defaultValue = -404;
        double input1 = getPref(preferences, "dBInput1", defaultValue);
        double input2 = getPref(preferences, "dBInput2", defaultValue);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CalibrationActivity.this);
        double measure1 = getPref(sharedPref, "Measure1", defaultValue);
        double measure2 = getPref(sharedPref, "Measure2", defaultValue);

        double slope;
        double manualZero;

        if (input1 == defaultValue || input2 == defaultValue || measure1 == defaultValue || measure2 == defaultValue) { // one of the values has not been correctly read

//            Use default values
            slope = 24.02;
            manualZero = -93.14;

        } else {
//        Compute log equation
            slope = (input1 - input2) / (Math.log(measure1) - Math.log(measure2));
            manualZero = input1 - slope * Math.log(measure1);
        }

        Log.d("M1", Double.toString(measure1));
        Log.d("M2", Double.toString(measure2));
        Log.d("I1", Double.toString(input1));
        Log.d("I2", Double.toString(input2));
        Log.d("slope", Double.toString(slope));
        Log.d("manualZero", Double.toString(manualZero));

//        Save coefficients
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("slope", Double.doubleToRawLongBits(slope));
        editor.putLong("manualZero", Double.doubleToRawLongBits(manualZero));
        editor.commit();

// Display computed equation
        String function = "dB = " + Double.toString(manualZero).substring(0, 5)
                + " + " + Double.toString(slope).substring(0, 4) + "*log(rawInput)";
        functionDisplay = (TextView) findViewById(R.id.Function);
        functionDisplay.setText(function);
        functionDisplay.setVisibility(View.VISIBLE);
    }

    public void setInput (final String inputName, String dialogTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dialogTitle);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    dBinput = Double.parseDouble(input.getText().toString());
                } catch (final NumberFormatException e) {
                    dBinput = 1.0;
                }
                Log.d(inputName, Double.toString(dBinput));
                SharedPreferences sharedPref = CalibrationActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(inputName, Double.doubleToRawLongBits(dBinput));
                editor.commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void getCalibratedValue(View view, String saveTo, String powSaveTo) {

//        Check if still recording

//        if(recRunning) {
//            stopService(MainActivity.this.CheckNoiseServiceIntent);
//            recRunning = false;
//
//            mainMessage = (TextView) findViewById(R.id.mainMessage);
//            mainMessage.setVisibility(View.VISIBLE);
//        }

        if(!recRunning) {

//TODO simplify : only interrupt is needed, do not repeat the rest
            if (!calibration.isAlive()) {
                calibration = new Calibration(this, saveTo, powSaveTo);
                calibration.start();
            } else {
                calibration.interrupt();
                calibration = new Calibration(this, saveTo, powSaveTo);
                calibration.start();
            }

//        Wait for calibration to end
            try {
                calibration.join();
            } catch (InterruptedException e) {
                Log.w("Recording : ", e);
                return;
            }
        };
        return;
    }

    //    Get calibrated zero from sharedPreferences
    double getPref(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
