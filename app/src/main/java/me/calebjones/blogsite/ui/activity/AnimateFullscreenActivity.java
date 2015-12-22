package me.calebjones.blogsite.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.michaldrabik.tapbarmenulib.TapBarMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.database.DatabaseManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import uk.co.senab.photoview.PhotoView;

@RuntimePermissions
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AnimateFullscreenActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog dialog;
    private File file;
    private TapBarMenu tapbar;

    ProgressBar progressBar;
    public String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/TheJonesTheory/temp";
    public String mfile_path, mPostTitle, bitmapKey, PostTitle, PostImage, PostText,PostURL;
    public BitmapInfo bi;
    public Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(new Fade());
        }

        Log.d("The Jones Theory", "Intent received...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        progressBar = (ProgressBar)findViewById(R.id.progress);

        //Get data from DetailActivity
        Intent intent = getIntent();
        PostTitle = intent.getExtras().getString("PostTitle");
        PostImage = intent.getExtras().getString("PostImage");
        PostText = intent.getExtras().getString("PostText");
        PostURL = intent.getExtras().getString("PostURL");

        int PostBG = intent.getExtras().getInt("PostBG");

        if (PostText.length() > 3){
            PostText = PostText.replaceAll("\\]", "").replaceAll("\\[", "");
        }

        stripHtml(PostTitle);

        //Setup the Actionabar with parameters
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(Html.fromHtml(PostTitle));

        //Init the PhotoView
        final PhotoView photoView = (PhotoView)findViewById(R.id.image);

        //Set up the bottom bar icons
        final ImageView shareView = (ImageView)findViewById(R.id.shareFooter);
        final ImageView downloadView = (ImageView)findViewById(R.id.downloadFooter);
        final ImageView browserView = (ImageView)findViewById(R.id.browserFooter);
        final ImageView copyView = (ImageView)findViewById(R.id.copyFooter);
        tapbar = (TapBarMenu)findViewById(R.id.footer);

        tapbar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                tapbar.toggle();
            }
        });

        photoView.setBackgroundColor(PostBG);

        //OnClick listeners for the icons
        View.OnClickListener sClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(shareView)) {
                    try {
                        shareIntent(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        View.OnClickListener dClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.equals(downloadView)) {
                    AnimateFullscreenActivityPermissionsDispatcher.getWritePermissionWithCheck(AnimateFullscreenActivity.this);
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
        byte[] byteArray = getIntent().getByteArrayExtra("bitmap");
        PostURL = getIntent().getStringExtra("PostURL");

        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        //Downloads bitmap to cache
        bi = Ion.getDefault(this)
                .getBitmapCache()
                .get(bitmapKey);
        photoView.setImageBitmap(bitmap);
        photoView.setMaximumScale(16);

        getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                getWindow().getEnterTransition().removeListener(this);

                // load the full version, crossfading from the thumbnail image
                Ion.with(photoView)
                        .crossfade(true)
                        .deepZoom()
                        .load(PostImage);

            }
        });
        //Setup the Action Bar back button and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(25);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        AnimateFullscreenActivityPermissionsDispatcher.onRequestPermissionsResult(this,
                requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void getWritePermission(){
        downloadFile();
    }

    // Option
    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onContactDenied() {
        Toast.makeText(this, "Unable to download file without permissions.",
                Toast.LENGTH_SHORT).show();
    }

    private void clipboardAdd() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("Post Title", PostTitle + " " + PostURL);

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);
        Toast.makeText(AnimateFullscreenActivity.this, "Text copied to clipboard.",
                Toast.LENGTH_LONG).show();


    }

    private void downloadFile() {
        mfile_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/TheJonesTheory/";
        mPostTitle = PostTitle.replaceAll("[^a-zA-Z]", "");
        mfile_path = mfile_path.replaceAll("\\s+","");
        mPostTitle = mPostTitle.replaceAll("\\s+","");

        Ion.with(AnimateFullscreenActivity.this)
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
                                    Log.e("The Jones Theory", "Error downloading "
                                            + mPostTitle + " - " + e);
                                    Toast.makeText(AnimateFullscreenActivity.this,
                                            "Error downloading file", Toast.LENGTH_LONG).show();
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
        fileIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/TheJonesTheory/")), "file/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        PendingIntent filePendingIntent = PendingIntent.getActivity(this, 0, fileIntent, 0);

        //Set up the PendingIntent for the Share action button
        Intent sendThisIntent = new Intent();
        sendThisIntent.setAction(Intent.ACTION_SEND);
        sendThisIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/TheJonesTheory/"
                + mPostTitle + ".png")));
        sendThisIntent.putExtra(Intent.EXTRA_TEXT, PostURL);
        sendThisIntent.setType("image/*");

        PendingIntent sharePendingIntent = PendingIntent.getActivity(this, 0, sendThisIntent, 0);

        //Check to see if the download directory exist
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, mPostTitle + ".png");
        intent.setDataAndType(Uri.fromFile(file), "image/*");

        Bitmap mBitmap = getCroppedBitmap(bitmap);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.BigPictureStyle bNoti = new NotificationCompat.BigPictureStyle();

        mPostTitle = PostTitle.replaceAll("[^a-zA-Z :]", "");


        //Load bitmap locally in APK
        Bitmap srcBitmapLocal = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.mipmap.ic_launcher);

        bNoti.setBigContentTitle("Download Completed")
                .bigPicture(bitmap)
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

    private File bitMapToFile(Bitmap mBitmap) throws IOException {
        file = new File(getApplicationContext().getCacheDir(), "temp.png");
        FileOutputStream fOut = new FileOutputStream(file);

        mBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
        file.setReadable(true, false);
        return file;
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
        AnimateFullscreenActivity.this.finish();
    }

    public void shareIntent(Bitmap mBitmap) throws IOException {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(bitMapToFile(mBitmap)));
        sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(PostText) + " \n\n Find more content here: " + PostURL);
        sendIntent.setType("image/*");
        startActivity(sendIntent);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                //User clicked home, do whatever you want
                this.onBackPressed();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }
}
