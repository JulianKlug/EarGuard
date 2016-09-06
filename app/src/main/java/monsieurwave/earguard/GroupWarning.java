package monsieurwave.earguard;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.util.LogWriter;
import android.util.Log;


public class GroupWarning {

    public CheckNoiseService context;

    public GroupWarning (CheckNoiseService ctx) {
        context = ctx;
    }


    public void whatsApp() {

        PackageManager pm = context.getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "@string/GroupWarning";

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            context.startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
//            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
//                    .show();

            Log.w("WhatsappWarning","not installed");
        }

    }


}
