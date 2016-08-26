package monsieurwave.earguard;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class CheckNoiseService extends Service {

    public Intent intent;
    public Recording recording = new Recording(this);
    public double zero;

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

//        Start recording thread (Recording class)
        this.recording.start();

//        Return service running method - only stops when stopped by user
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.intent = getIntent();
        this.intent.getDoubleExtra("zero",this.zero);
        Log.w("NoiseCheckService", "onCreate callback called");
        Log.w("Zero :", Double.toString(zero));
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
}
