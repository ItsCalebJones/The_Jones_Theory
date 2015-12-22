package me.calebjones.blogsite.ui.activity.debug;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;

import com.koushikdutta.ion.Ion;

import java.io.ByteArrayOutputStream;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.database.DatabaseManager;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.util.receivers.BootReceiver;
import me.calebjones.blogsite.util.BlipUtils;

public class IntentLauncher extends AppCompatActivity {

    public static final String EXTRA_NUM = "number";
    public static final String NEW_POST = "me.calebjones.blogsite.NEW_POST";
    public Button loginButton, notificationButton;
    private Context context;
    private Posts post;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseManager = new DatabaseManager(this);
        setContentView(R.layout.debug_activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.wallpaper);

        int num = 0;
        if (savedInstanceState != null)
            num = savedInstanceState.getInt("NUM");
        //
        boolean running = true;
        while (running)
        {
            if (databaseManager.getCount() != 0 ) {
                loadPost(num);
            } else {
                notificationButton.setVisibility(View.GONE);
                break;
            }
            if (post.getFeaturedImage() != null & post.getFeaturedImage().length() > 0)
            {
                break;
            }
        }

        loginButton = (Button) findViewById(R.id.BootReceived);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("The Jones Theory-Button", "Hello World!");
                Intent intent = new Intent(getApplicationContext(), BootReceiver.class);
                sendBroadcast(intent);
            }
        });

        notificationButton = (Button) findViewById(R.id.NotifLaunch);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent notifIntent = new Intent(NEW_POST);
                Log.d("The Jones Theory-Button", "Post - ID: " + post.getID() + " Title: " + post.getTitle());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] bytes = stream.toByteArray();
                notifIntent.putExtra("BitmapImage", bytes);

                notifIntent.putExtra(EXTRA_NUM, post.getPostID());
                sendBroadcast(notifIntent);
            }
        });
    }

    private void loadPost(int num) {
        int random;
        int max = databaseManager.getMax() - 20;

        if (databaseManager.getCount() != 0){
            if (max != 0 && num == 0){
                random = BlipUtils.randInt(1, max);
                post = databaseManager.getPost(random);
            } else {
                random = num;
                post = databaseManager.getPost(random);
            }
        }
        Log.d("The Jones Theory-Button", "Post - ID: " + post.getID() + " Title: " + post.getTitle());
    }
}
