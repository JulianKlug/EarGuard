package monsieurwave.earguard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by julian on 8/22/16.
 */


public class Alert extends ContextWrapper{

    private Context context;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    public Alert(Context base) {
        super(base);
    }


//
//    public void Alert(Context context) {
//        this.context=context;
//
//        mBuilder =new NotificationCompat.Builder(this.context)
////                        .setSmallIcon(R.drawable.notification_icon)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!");
//
//// Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this.context, MainActivity.class);
//
//// The stack builder object will contain an artificial back stack for the
//// started Activity.
//// This ensures that navigating backward from the Activity leads out of
//// your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
//
//// Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//
//// Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//
//        mBuilder.setContentIntent(resultPendingIntent);
//        mNotificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//
////
//    }

    public void pop() {
        Log.w("Pop", "yo");
        this.context=context;


        // mId allows you to update the notification later on.
//        int mId = 1;
//        mNotificationManager.notify(mId, mBuilder.build());

        Notification notify = new Notification.Builder(this.context)
                .setContentTitle("Taxi Driver")
                .setContentText("New Mission")
//                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true).build();
        notify.defaults |= Notification.DEFAULT_SOUND;
        notify.defaults |= Notification.DEFAULT_VIBRATE;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(context.NOTIFICATION_SERVICE);

        notificationManager.notify(0,notify);


    }
}
