package me.calebjones.blogsite.util.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.calebjones.blogsite.util.services.UpdateCheckService;

/**
 * Created by cjones on 11/10/15.
 */
public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("The Jones Theory-Boot", "Hello World!");
        context.startService(new Intent(context, UpdateCheckService.class));
    }
}
