
package monsieurwave.earguard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.math.BigDecimal;


public class Recording extends Thread {

    public AudioRecord audioRecord;
    public CheckNoiseService context;
    public Double zero;
    public Double powZero;
    public Saving saving;
    public GroupWarning grWarning;


    // Constructor of class (ensures passing on of context from CheckNoiseService to Recording)
    public Recording(CheckNoiseService ctx, Double z, Double pz) {
        context = ctx;
        this.zero = z;
        this.powZero = pz;
    }

    @Override
    public void run() {

//        Setting up warning
        grWarning =  new GroupWarning(context);


//        Setting recording variables

//        Setting audio channels
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;

//        Setting audio encoding
        int encoding = AudioFormat.ENCODING_PCM_16BIT;

        //        Sampling frequency
        int frequency;
        try {
            frequency = getValidSampleRates(channelConfiguration, encoding);
            Log.w("Recording f: ", Integer.toString(frequency));
        } catch (Exception e) {
            Log.w("Recording : ", e);
            return;
        }

        //      Setting buffer size in bytes
        int buffersizebytes = (int) (frequency * 1.25);
//        int buffersizebytes = AudioRecord.getMinBufferSize(frequency,channelConfiguration, encoding);
        Log.w("Buffersize: ", Integer.toString(buffersizebytes));

        //Defining audiorecord
        Recording.this.audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, encoding, buffersizebytes);

//        Get number of channels
        int numChannels = Recording.this.audioRecord.getChannelCount();

//      Create buffer (=array temporaire) to hold audio data
        short[] buffer = new short[buffersizebytes];


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String calibPref = preferences.getString("calibration_method", "1");
        double threshold = new Double(preferences.getString("dBthreshold", "80"));

        double splValue = 0.0;
        double rmsValue = 0.0;
        final double P0 = 0.000002;
        final int CALIB_DEFAULT = -105;
        int mCaliberationValue = CALIB_DEFAULT;
        double mMaxValue = 0.0;


//      //  Doing the work

        Recording.this.audioRecord.startRecording();


        while (!Thread.currentThread().isInterrupted()) {

            try {
                Thread.sleep(1000);

                Double dBamplitude;

                if (calibPref.equals("1") ) {

                    // creating these variables here so that
                    // the mode change can be handled
                    int SIZE = buffersizebytes;
                    short[] tempBuffer = new short[SIZE];

                    audioRecord.read(tempBuffer, 0, SIZE);


                    for (int i = 0; i < SIZE - 1; i++) {
                        rmsValue += tempBuffer[i] * tempBuffer[i];
                    }
                    rmsValue = rmsValue / SIZE;
                    rmsValue = Math.sqrt(rmsValue);

                    splValue = 20 * Math.log10(rmsValue / P0);
                    splValue = splValue + mCaliberationValue;
                    splValue = round(splValue, 2);

                    if (mMaxValue < splValue) {
                        mMaxValue = splValue;
                    }

                    dBamplitude = splValue;

                    Log.w("splValue", Double.toString(splValue));
                    Log.w("mMaxValue", Double.toString(mMaxValue));

                } else {
//
                    double amplitude = 0;
                    double bufferMax = 0;

//                Read audioRecord dans buffer
//                And register number of values read to buffer into nSamples
                    int nSamples = audioRecord.read(buffer, 0, buffersizebytes);
                    double sum = 0;

//                Loop through the buffer
                    for (int i = 0; i < nSamples; i++) {
                        final double absbuf = Math.abs(buffer[i]);
                        if (absbuf > bufferMax) {
                            bufferMax = absbuf;
                            sum += absbuf;
                        }
                        amplitude = bufferMax;
                    }

                if (calibPref.equals("3")) {
//              Fit1 log
                    dBamplitude = -93.14 + 24.02*Math.log(amplitude);

                } else if(calibPref.equals("4")) {
//               Fit2 trigo-log
                    double a = -9.197;
                    double b = -0.8016;
                    double c = 73.32;
                    double x = Math.log(amplitude);
                    dBamplitude = a*(Math.sin(x-Math.PI))+b*Math.pow((x-10),2)+c*(1);

                } else {


                    double powerAmplitude = calculatePowerDb(buffer, 0, nSamples);
                    Log.w("P-Amp: ", Double.toString(powerAmplitude));

                    Log.w("Amp: ", Double.toString(amplitude));

//                Normalizing to dB

//                If dBZero was measured with reference
//                double dBZero = 30;
//                double dBamplitude  = 20*Math.log10(amplitude/zero) + dBZero;

//                If dBZero was not measured with a reference
                    dBamplitude = Math.abs(powerAmplitude - powZero);
                }
                }

//                Saving the value
                saving = new Saving(dBamplitude, context);
                this.saving.start();

//                Send data to CheckNoiseService
                Message msg = Message.obtain();
                String formattedAmp = String.format("%.2f",dBamplitude); // Format to 2 decimal string
                msg.obj = formattedAmp;
                context.mHandler.sendMessage(msg);

//                Check for too high amplitudes
                if (dBamplitude > threshold) {
                    Log.w("Danger !", " Level is over 9000!");
                    boolean notifyMe = preferences.getBoolean("userWarning", false);
                    if (notifyMe) {
                        notifyUser();
                    }
                    notifyGroup();

                }

//                Log.w("number of samples : ", Integer.toString(nSamples));
//                Log.w("AMPLITUDE: ", Double.toString(amplitude));
//                Log.w("dB: ", Double.toString(dBamplitude));
//                Log.w("Sum: ", Double.toString(sum));

            } catch (InterruptedException e) {
                break;
            }
        }
        audioRecord.release();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        audioRecord.release();

    }

    public void notifyUser() {

// Create notification
// Set notification activity when clicked on

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction("android.intent.action.MAIN");
        notificationIntent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

// Set notifications behaviours
        Notification notify = new Notification.Builder(context)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setContentTitle("EarGuard")
                .setContentText("Danger ! Noise level is too high!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notify);

    }

    public void notifyGroup(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useWA = preferences.getBoolean("whatsappWarning", false);

        if (useWA) {
            grWarning.whatsApp();
        }
    }

    /**
     * Utility function for rounding decimal values
     */
    public double round(double d, int decimalPlace) {
        // see the Javadoc about why we use a String in the constructor
        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }


    /**
     * Compute the minimal frequency supported by the microphone
     *
     * @param channelConfiguration the configuration of the channel
     * @param encoding             the encoding used
     * @return the frequency found
     * @throws Exception if no frequency available from the list
     */
    public int getValidSampleRates(int channelConfiguration, int encoding) throws Exception {
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfiguration, encoding);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                return rate;
            }
        }
        throw new Exception("No valid sample rate found or no Mic.");
    }


    /**
     * Calculate the power of the given input signal.
     *
     * @param sdata   Buffer containing the input samples to process.
     * @param off     Offset in sdata of the data of interest.
     * @param samples Number of data samples to process.
     * @return The calculated power in dB relative to the maximum
     * input level; hence 0dB represents maximum power,
     * and minimum power is about -95dB.  Particular
     * cases of interest:
     * <ul>
     * <li>A non-clipping full-range sine wave input is
     * about -2.41dB.
     * <li>Saturated input (heavily clipped) approaches
     * 0dB.
     * <li>A low-frequency fully saturated input can
     * get above 0dB, but this would be pretty
     * artificial.
     * <li>A really tiny signal, which only occasionally
     * deviates from zero, can get below -100dB.
     * <li>A completely zero input will produce an
     * output of -Infinity.
     * </ul>
     * <b>You must be prepared to handle this infinite
     * result and results greater than zero,</b> although
     * clipping them off would be quite acceptable in
     * most cases.
     */


    public final static double calculatePowerDb(short[] sdata, int off, int samples) {

// Calculate the sum of the values, and the sum of the squared values.
// We need longs to avoid running out of bits.
        double sum = 0;
        double sqsum = 0;
        for (int i = 0; i < samples; i++) {
            final long v = sdata[off + i];
            sum += v;
            sqsum += v * v;
        }

        double power = (sqsum - sum * sum / samples) / samples;

        // Scale to the range 0 - 1.
        power /= MAX_16_BIT * MAX_16_BIT;

        // Convert to dB, with 0 being max power.  Add a fudge factor to make
        // a "real" fully saturated input come to 0 dB.
        return Math.log10(power) * 10f + FUDGE;

    }


    // ******************************************************************** //
    // Constants.
    // ******************************************************************** //

    // Maximum signal amplitude for 16-bit data.
    private static final float MAX_16_BIT = 32768;

    // This fudge factor is added to the output to make a realistically
    // fully-saturated signal come to 0dB.  Without it, the signal would
    // have to be solid samples of -32768 to read zero, which is not
    // realistic.  This really is a fudge, because the best value depends
    // on the input frequency and sampling rate.  We optimise here for
    // a 1kHz signal at 16,000 samples/sec.
    private static final float FUDGE = 0.6f;

}

