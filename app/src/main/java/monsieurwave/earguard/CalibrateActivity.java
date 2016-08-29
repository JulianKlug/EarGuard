package monsieurwave.earguard;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class CalibrateActivity extends Activity{

    private Button CalibNowButton;
//    Calibration calibration = new Calibration(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        Log.w("CalibrateActivity", "onCreate called");
        addListenerOnButton();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("CalibrateActivity", "onStart called");

    }


    public void addListenerOnButton() {
        CalibNowButton = (Button) findViewById(R.id.calibButton);

    }

//    public void calibrate(View view) {
//
//        if(!calibration.isAlive()){
//            calibration = new Calibration(this);
//            calibration.start();
//        } else {
//            calibration.interrupt();
//            calibration = new Calibration(this);
//            calibration.start();
//        }
//
//        return;
//
//    }


}
