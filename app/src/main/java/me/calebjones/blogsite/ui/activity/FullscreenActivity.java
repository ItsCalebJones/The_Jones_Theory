package me.calebjones.blogsite.ui.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import me.calebjones.blogsite.R;
import uk.co.senab.photoview.PhotoView;


public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = "The Jones Theory - F";
    private Toolbar mToolbar;
    private ProgressDialog dialog;
    ProgressBar progressBar;
    public String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/TheJonesTheory/temp";
    public String mfile_path;
    public String mPostTitle;
    public String bitmapKey;
    public String PostTitle;
    public String PostImage;
    public String PostText;
    public String PostURL;
    public BitmapInfo bi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("The Jones Theory", "Intent received...");

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementsUseOverlay(false);
        }
        setContentView(R.layout.activity_fullscreen);

        progressBar = (ProgressBar)findViewById(R.id.progress);

        //Get data from PostSelectedActivity
        Intent intent = getIntent();
        PostTitle = intent.getExtras().getString("PostTitle");
        PostImage = intent.getExtras().getString("PostImage");
        PostText = intent.getExtras().getString("PostText");
        PostURL = intent.getExtras().getString("PostURL");

        PostTitle = stripHtml(PostTitle);

        //Setup the Actionabar with parameters
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(PostTitle);

        //Init the PhotoView
        final PhotoView photoView = (PhotoView)findViewById(R.id.image);
        photoView.setMaximumScale(16);

        //Set up the bottom bar icons
        final ImageView shareView = (ImageView)findViewById(R.id.shareFooter);
        final ImageView downloadView = (ImageView)findViewById(R.id.downloadFooter);
        final ImageView browserView = (ImageView)findViewById(R.id.browserFooter);
        final ImageView copyView = (ImageView)findViewById(R.id.copyFooter);

        //OnClick listeners for the icons
        View.OnClickListener sClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(shareView)) {
                    shareIntent();
                }
            }
        };
        View.OnClickListener dClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(downloadView)) {
                    downloadFile();
                }
            }
        };
        View.OnClickListener bClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(browserView)) {
                    browserIntent();
                }
            }
        };
        View.OnClickListener cClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(copyView)) {
                    clipboardAdd();
                }
            }
        };


        shareView.setOnClickListener(sClickListener);
        downloadView.setOnClickListener(dClickListener);
        browserView.setOnClickListener(bClickListener);
        copyView.setOnClickListener(cClickListener);

        // Load the bitmap from the intent
        bitmapKey = getIntent().getStringExtra("bitmapInfo");
        PostURL = getIntent().getStringExtra("PostURL");

        //Downloads bitmap to cache
        bi = Ion.getDefault(this)
                .getBitmapCache()
                .get(bitmapKey);
        photoView.setImageBitmap(bi.bitmap);

        Palette.from(bi.bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch Swatch = palette.getDarkMutedSwatch();
                if (Swatch != null) {
                    photoView.setBackgroundColor(Swatch.getRgb());

                }
            }
        });

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    bitMapToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(r);
        t.start();

        // load the full version, crossfading from the thumbnail image
        Ion.with(photoView)
                .crossfade(true)
                .deepZoom()
                .load(PostImage);
    }

    private void clipboardAdd() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("Post Title", PostTitle + " " + PostURL);

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);
        Toast.makeText(FullscreenActivity.this, "Text copied to clipboard.", Toast.LENGTH_LONG).show();

    }

    private void downloadFile() {
        mfile_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/TheJonesTheory/";
        mPostTitle = PostTitle.replaceAll("[^a-zA-Z]", "");
        mfile_path = mfile_path.replaceAll("\\s+","");
        mPostTitle = mPostTitle.replaceAll("\\s+","");

        Ion.with(FullscreenActivity.this)
                .load(PostImage)
                        // attach the percentage report to a progress bar.
                        // can also attach to a ProgressDialog with progressDialog.
                .progressBar(progressBar)
                        // write to a file
                .write(new File(mfile_path + mPostTitle + ".png"))
                        // run a callback on completion
                        .setCallback(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e, File result) {
                                resetDownload();
                                if (e != null) {
                                    Log.e(TAG, "Error downloading " + mPostTitle + " - " + e);
                                    Toast.makeText(FullscreenActivity.this, "Error downloading file", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                NotfifyDownload();
                            }
                        });

    }

    private void NotfifyDownload() {
        Context mContext = getApplicationContext();

        //Get Unique Notification ID
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        int notificationId = Integer.valueOf(last4Str);

        //Setup the Notificaiton Intent
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File dir = new File(mfile_path);

        //Set up the PendingIntent for the Open File action button
        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
        fileIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TheJonesTheory/")), "file/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        PendingIntent filePendingIntent = PendingIntent.getActivity(this, 0, fileIntent, 0);

        //Set up the PendingIntent for the Share action button
        Intent sendThisIntent = new Intent();
        sendThisIntent.setAction(Intent.ACTION_SEND);
        sendThisIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TheJonesTheory/" + mPostTitle + ".png")));
        sendThisIntent.putExtra(Intent.EXTRA_TEXT, PostURL);
        sendThisIntent.setType("image/*");

        PendingIntent sharePendingIntent = PendingIntent.getActivity(this, 0, sendThisIntent, 0);

        //Check to see if the download directory exist
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, mPostTitle + ".png");
        intent.setDataAndType(Uri.fromFile(file), "image/*");

        Bitmap mBitmap = getCroppedBitmap(bi.bitmap);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.BigPictureStyle bNoti = new NotificationCompat.BigPictureStyle();

        mPostTitle = PostTitle.replaceAll("[^a-zA-Z :]", "");


        //Load bitmap locally in APK
        Bitmap srcBitmapLocal = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.mipmap.ic_launcher);

        bNoti.setBigContentTitle("Download Completed")
                .bigPicture(bi.bitmap)
                .bigLargeIcon(srcBitmapLocal)
                .setSummaryText(mPostTitle);


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setContentText("Download Completed")
                        .setContentTitle(mPostTitle)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setLargeIcon(mBitmap)
                        .addAction(R.drawable.ic_action_file_download, "Open", filePendingIntent)
                        .addAction(R.drawable.ic_action_share, "Share", sharePendingIntent)

                .setStyle(bNoti);

//        noti.flags |= Notification.FLAG_AUTO_CANCEL;
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notificationBuilder);
        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);

// Issue the notification with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    void resetDownload() {
            progressBar.setProgress(0);
        }
    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }


    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    private void bitMapToFile() throws IOException {
        File dir = new File(file_path);
        if(!dir.exists()){
            dir.mkdirs();
        } else {
            deleteDir(dir);
            dir.mkdirs();
        }
        File file = new File(dir, "temp.png");
        FileOutputStream fOut = new FileOutputStream(file);

        bi.bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
    private void browserIntent() {
        String url = PostURL;
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse(url));
        startActivity(sendIntent);
    }

    private void backIntent() {
        FullscreenActivity.this.finish();
    }

    public void shareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TheJonesTheory/temp/temp.png")));
        sendIntent.putExtra(Intent.EXTRA_TEXT, PostURL);
        sendIntent.setType("image/*");
        startActivity(sendIntent);
    }
}
