package monsieurwave.earguard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;


public class Recording extends Thread {

    public AudioRecord audioRecord;
    public CheckNoiseService context;

    // Constructor of class (ensures passing on of context from CheckNoiseService to Recording)
    public Recording(CheckNoiseService ctx) {
        context = ctx;
    }

    @Override
    public void run() {

//        Setting variables
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

//      Create buffer (=array temporaire) to hold audio data
        short[] buffer = new short[buffersizebytes];


//        Doing the work

        Recording.this.audioRecord.startRecording();

        int count = 0;

        while (!Thread.currentThread().isInterrupted()) {

            try {
//                Log.w("Recording count :", Integer.toString(count));
                count += 1;
                Thread.sleep(1000);
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

                double dBamplitude = calculatePowerDb(buffer, 0, nSamples);

//                Check for too high amplitudes
                if (dBamplitude > -30) {
                    Log.w("Danger !", " Level is over 9000!");

// Create notification
// Set notification activity when clicked on

                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.setAction("android.intent.action.MAIN");
                    notificationIntent.addCategory("android.intent.category.LAUNCHER");
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    Notification.FLAG_AUTO_CANCEL);

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

//                Log.w("number of samples : ", Integer.toString(nSamples));
//                Log.w("AMPLITUDE: ", Double.toString(amplitude));
                Log.w("dB: ", Double.toString(dBamplitude));
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
