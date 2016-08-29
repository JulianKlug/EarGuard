package monsieurwave.earguard;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Saving extends Thread {

    public CheckNoiseService context;
    public String fileName;
    public String dirName;
    public double amplitude;

    public Saving(Double amp, CheckNoiseService ctx) {
        // store parameter for later user
        amplitude = amp;
        context = ctx;
    }

    @Override
    public void run() {

        fileName = "NoiseLog";

// If external storage is not writable, save to internal (default)
        File dirPath = context.getFilesDir();

        // Check if external is writable
        if (isExternalStorageWritable()) {
            // If External storage is writable, write to external
            dirPath = context.getExternalFilesDir(null);
        }


            String filePath = dirPath + fileName;


        Log.w("filePath:", filePath);

            FileOutputStream outputStream;
            File file = new File(dirPath, fileName);

        Long rightnow = System.currentTimeMillis();
        String output = Long.toString(rightnow) + " : " + Double.toString(amplitude) ;


        try {
            //create a filewriter and set append modus to true
            FileWriter fw = new FileWriter(file, true);
            fw.append("\n");
            fw.append(output);
            fw.close();

        } catch (IOException e) {
            Log.w("Storage", "Error writing " + file, e);
        }

//            try {
//                outputStream = new FileOutputStream(file);
//                String output = Double.toString(amplitude);
//                outputStream.write(output.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

    }

    @Override
    public void interrupt() {
        super.interrupt();

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


}
