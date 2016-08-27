package monsieurwave.earguard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ToggleButton;



public class MainActivity extends AppCompatActivity {

    private ToggleButton toggleButton1;

    public Intent CheckNoiseServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Remove title bar
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

//        ToggleButton toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);


    }

    public void addListenerOnButton() {

        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton1.setBackgroundResource(R.drawable.button);

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

//    public void onButtonIntentServiceClick(View view){
//        Intent  CheckNoiseIntentServiceIntent = new Intent(this, CheckNoiseIntentService.class);
//        startService(CheckNoiseIntentServiceIntent);
//
//    }

}

