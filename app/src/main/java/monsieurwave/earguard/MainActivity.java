package monsieurwave.earguard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity {

    private ToggleButton toggleButton1;
    private Button calibrationButton;

    public Intent CheckNoiseServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w("MainActivity", "onCreate called");
        addListenerOnButton();
        toggleButton1.setChecked(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("MainActivity", "onStart called");
    }

    public void addListenerOnButton() {

        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
        calibrationButton = (Button) findViewById(R.id.calibActButton);

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.CheckNoiseServiceIntent = new Intent(MainActivity.this, CheckNoiseService.class);

                if (isChecked) {
                    // The toggle is actually disabled
                    stopService(MainActivity.this.CheckNoiseServiceIntent);


                } else {
                    // The toggle is actually enabled
                    startService(CheckNoiseServiceIntent);

                }
            }


        });

    }

    public void gotoCalib(View view) {
        Intent intent = new Intent(MainActivity.this, CalibrateActivity.class);
        startActivity(intent);
    }


}

