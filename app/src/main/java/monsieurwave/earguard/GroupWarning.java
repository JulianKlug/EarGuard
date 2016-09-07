package monsieurwave.earguard;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.util.LogWriter;
import android.util.Log;

import java.net.URI;


public class GroupWarning {

    public CheckNoiseService context;

    public GroupWarning (CheckNoiseService ctx) {
        context = ctx;
    }


    public void whatsApp() {

//        Log.i("tag",Uri.parse("content://com.android.contacts/data/").toString());

        PackageManager pm = context.getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Danger ! Noise level is too high!";

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            waIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(waIntent);

        } catch (PackageManager.NameNotFoundException e) {
//            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
//                    .show();

            Log.w("WhatsappWarning","not installed");
        }

    }


}
