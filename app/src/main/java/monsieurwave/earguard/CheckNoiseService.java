package monsieurwave.earguard;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


public class CheckNoiseService extends Service {

    public Intent intent;
    public Recording recording;
    public double zero;
    public String currentRec;

    public CheckNoiseService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.w("NoiseCheckService", "onBind callback called");
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("NoiseCheckService", "onStartCommand callback called");
        super.onStartCommand(intent, flags, startId);

//        Get the calibrated zero
        this.zero = intent.getDoubleExtra("zero", 1);
        Log.w("Zero :", Double.toString(zero));


//        Start recording thread (Recording class)
        recording = new Recording(this,this.zero);
        this.recording.start();

//        Return service running method - only stops when stopped by user
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.w("NoiseCheckService", "onCreate callback called");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("NoiseCheckService", "onDestroy callback called");
        recording.interrupt();
    }

    public Intent getIntent() {
        return intent;
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // Act on the message
            super.handleMessage(msg);
            CheckNoiseService.this.currentRec = msg.obj.toString();
            Log.w("CheckNoiseService",CheckNoiseService.this.currentRec);

//            Send data to MainActivity
            Intent i = new Intent("AMP_UPDATED");
            i.putExtra("dbAmp",CheckNoiseService.this.currentRec);
            sendBroadcast(i);
        }
    };
}
