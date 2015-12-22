package me.calebjones.blogsite.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.ui.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_setting_toolbar);

        //Init the toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.Settingtoolbar);
        setSupportActionBar(mToolbar);

        //Setup the Actionabar backbutton and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        Boolean notificationCheckBox = sharedPreferences
                .getBoolean("notifications_new_message", true);
        Boolean vibrateCheckBox = sharedPreferences
                .getBoolean("notifications_new_message_vibrate", true);
        String ringtoneBox = sharedPreferences
                .getString("notifications_new_message_ringtone", "default ringtone");
        String notificationTimer = sharedPreferences
                .getString("notification_sync_time", "4");

        Log.d("The Jones Theory", "Notification: " + notificationCheckBox +
                " Ringtone: " + ringtoneBox + " Vibrate: " + vibrateCheckBox + " Timer: "
                + notificationTimer);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }
}