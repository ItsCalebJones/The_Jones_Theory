package me.calebjones.blogsite.util;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cjones on 8/2/15.
 */
public class ButtonReceiver extends BroadcastReceiver {

    public NotificationManager myNotificationManager;
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("The Jones Theory", "Notification button pushed!");
        myNotificationManager =  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        myNotificationManager.cancelAll();
    }
}
