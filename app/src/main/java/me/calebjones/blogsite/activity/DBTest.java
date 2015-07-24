package me.calebjones.blogsite.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.loader.PostDownloader;

public class DBTest extends ActionBarActivity implements View.OnClickListener{

    private ProgressBar progressBar;
    private TextView progress, title, pageTitle, caption, databaseSize;
    private Button button, updateButton, deleteButton;
    private View downloadUI;
    private DatabaseManager databaseManager;
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PostDownloader.DOWNLOAD_PROGRESS:
                    if (progressBar.isIndeterminate()) {
                        progressBar.setIndeterminate(false);
                    }
                    progressBar.setProgress((int) intent.getExtras().getDouble(PostDownloader.PROGRESS));
                    progress.setText(String.valueOf(Math.round(intent.getExtras().getDouble(PostDownloader.PROGRESS))) + "%");
                    title.setText(Html.fromHtml(intent.getExtras().getString(PostDownloader.TITLE)));
                    databaseSize.setText("Database Size = " + databaseManager.getCount());
                    break;
                case PostDownloader.DOWNLOAD_FAIL:
                    progress.setText("Failed :(");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DBTest.this.setResult(RESULT_CANCELED);
                            DBTest.this.finish();
                        }
                    }, 2000);
                    break;
                case PostDownloader.DOWNLOAD_SUCCESS:
                    progress.setText("Success :D");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DBTest.this.setResult(RESULT_OK);
                            DBTest.this.finish();
                        }
                    }, 2000);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);

        if (databaseManager == null) {
            databaseManager = new DatabaseManager(this);
        }

        progress = (TextView) findViewById(R.id.progress);
        title = (TextView) findViewById(R.id.title);
        button = (Button) findViewById(R.id.start_download);
        downloadUI = findViewById(R.id.download_ui);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        databaseSize = (TextView) findViewById(R.id.databaseSize);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        updateButton = (Button) findViewById(R.id.updateButton);

        downloadUI.setVisibility(View.GONE);
        button.setOnClickListener(this);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    databaseSize.setText("Database Size = " + databaseManager.getCount());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PostDownloader.class);
                intent.setAction(PostDownloader.DOWNLOAD_MISSING);
                startService(intent);
            }
        });

        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.regi_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PostDownloader.DOWNLOAD_PROGRESS);
        intentFilter.addAction(PostDownloader.DOWNLOAD_SUCCESS);
        intentFilter.addAction(PostDownloader.DOWNLOAD_FAIL);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, PostDownloader.class);
        intent.setAction(PostDownloader.DOWNLOAD_ALL);
        startService(intent);

        title.setText("Getting it done!");
        button.setVisibility(View.GONE);
        downloadUI.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dbtest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
