package me.calebjones.blogsite.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.network.PostDownloader;

public class DownloadActivity extends ActionBarActivity implements View.OnClickListener{

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
                    caption.setText(Html.fromHtml("Found " + intent.getExtras().getInt(PostDownloader.NUMBER) + " total posts."));
                    progress.setText(String.valueOf(Math.round(intent.getExtras().getDouble(PostDownloader.PROGRESS))) + "%");
                    title.setText(Html.fromHtml(intent.getExtras().getString(PostDownloader.TITLE)));
                    break;
                case PostDownloader.DOWNLOAD_FAIL:
                    progress.setText("Failed :(");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DownloadActivity.this.setResult(RESULT_CANCELED);
                            DownloadActivity.this.finish();
                        }
                    }, 2000);
                    break;
                case PostDownloader.DOWNLOAD_SUCCESS:
                    progress.setText("Success :D");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showLogin();
                        }
                    }, 2000);
                    break;
            }
        }
    };

    private void showLogin() {

        Intent loginIntent = new Intent(this, MainActivity.class);
        startActivity(loginIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPrefs.getInstance().setFirstRun(false);
        setContentView(R.layout.activity_dbtest);

        Log.d("The Jones Theory", "Downloading = " + SharedPrefs.getInstance().isDownloading());

        if (databaseManager == null) {
            databaseManager = new DatabaseManager(this);
        }

        progress = (TextView) findViewById(R.id.progress);
        title = (TextView) findViewById(R.id.titles);
        pageTitle = (TextView) findViewById(R.id.title);
        caption = (TextView) findViewById(R.id.caption);
        button = (Button) findViewById(R.id.start_download);
        downloadUI = findViewById(R.id.download_ui);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.myPrimaryDarkColor), PorterDuff.Mode.SRC_IN);
        progressBar.getProgressDrawable()
                .setColorFilter(getResources()
                        .getColor(R.color.myPrimaryDarkColor), PorterDuff.Mode.SRC_IN);


        downloadUI.setVisibility(View.GONE);
        pageTitle.setText("Download the Science!");
        caption.setText("About ~2MB of text data needs to be downloaded to make the app experience fluid and support the search functionality.");
                button.setOnClickListener(this);


        if (savedInstanceState != null || SharedPrefs.getInstance().isDownloading()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PostDownloader.DOWNLOAD_PROGRESS);
            intentFilter.addAction(PostDownloader.DOWNLOAD_SUCCESS);
            intentFilter.addAction(PostDownloader.DOWNLOAD_FAIL);
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

            pageTitle.setText("Downloading Science");
            caption.setText("Calculating data size.");
            button.setVisibility(View.GONE);
            downloadUI.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("The Jones Theory", "DownloadActivity - onClick - Doing the download thing.");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PostDownloader.DOWNLOAD_PROGRESS);
        intentFilter.addAction(PostDownloader.DOWNLOAD_SUCCESS);
        intentFilter.addAction(PostDownloader.DOWNLOAD_FAIL);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, PostDownloader.class);
        intent.setAction(PostDownloader.DOWNLOAD_ALL);
        startService(intent);

        pageTitle.setText("Downloading Science");
        caption.setText("Calculating data size.");
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
