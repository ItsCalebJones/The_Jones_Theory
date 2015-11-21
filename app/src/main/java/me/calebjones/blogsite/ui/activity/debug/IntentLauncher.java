package me.calebjones.blogsite.ui.activity.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.util.receivers.BootReceiver;

public class IntentLauncher extends AppCompatActivity {

    public Button loginButton;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginButton = (Button) findViewById(R.id.BootReceived);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("The Jones Theory-Button", "Hello World!");
                Intent intent = new Intent(getApplicationContext(), BootReceiver.class);
                sendBroadcast(intent);
            }
        });
    }
}
