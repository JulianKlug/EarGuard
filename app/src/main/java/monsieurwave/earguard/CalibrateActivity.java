package monsieurwave.earguard;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;



public class CalibrateActivity extends Activity{

    private Button CalibNowButton;
    public Calibration calibration = new Calibration(this);

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

    public void calibrate(View view) {
//        Intent intent = new Intent(CalibrateActivity.this, Calibration.class);
//        startActivity(intent);
        this.calibration.start();
    }


}
