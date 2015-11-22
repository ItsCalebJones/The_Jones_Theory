package me.calebjones.blogsite.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.activity.PostSelectedActivity;
import me.calebjones.blogsite.activity.debug.IntentLauncher;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.models.Posts;
import me.calebjones.blogsite.network.PostDownloader;
import me.calebjones.blogsite.network.UpdateCheckService;

/**
 * Created by cjones on 11/10/15.
 * Need to implement a service and confirm downloaded before showing this notificaiton.
 * Then need to open directly into DetailView activity.
 */
public class NewPostReceiver extends BroadcastReceiver {

    private static final int NOTIF_ID = 23443;
    private Posts post;

        public void onReceive(Context context, Intent intent){
            Log.d("The Jones Theory", "NewPost - Hello World!");
            if (intent.getAction().equals(PostDownloader.NEW_POST)){

                //Init a DB connection
                DatabaseManager databaseManager = new DatabaseManager(context);

                //Grab the new postID and Bitmap
                int postID = intent.getExtras().getInt(UpdateCheckService.EXTRA_NUM);
                byte[] bytes = intent.getByteArrayExtra("BitmapImage");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Log.d("The Jones Theory-NPR", "Post - ID: " + postID );

                post = databaseManager.getPostByID(postID);

                // Specify the 'big view' content to display the long
                // event description that may not fit the normal content text.
                android.support.v4.app.NotificationCompat.BigPictureStyle
                        bigStyle = new NotificationCompat.BigPictureStyle();

                //Get information about the post that was selected and start activity.
                Intent mainActIntent = new Intent(context, PostSelectedActivity.class);
                mainActIntent.putExtra("PostTitle", post.getTitle());
                mainActIntent.putExtra("PostImage", post.getFeaturedImage());
                mainActIntent.putExtra("PostText", post.getContent());
                mainActIntent.putExtra("PostURL", post.getURL());
                mainActIntent.putExtra("PostID", post.getPostID());
                PendingIntent clickIntent = PendingIntent.getActivity(context, 57836,
                        mainActIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                //Set up the PendingIntent for the Share action button
                Intent sendThisIntent = new Intent();
                sendThisIntent.setAction(Intent.ACTION_SEND);
                sendThisIntent.putExtra(Intent.EXTRA_TEXT, post.getURL());
                sendThisIntent.setType("text/plain");

                PendingIntent sharePendingIntent = PendingIntent
                        .getActivity(context, 0, sendThisIntent, 0);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setContentTitle("New " + post.getCategories() + " post available!")
                        .setContentText(post.getTitle())
                        .setContentIntent(clickIntent)
                        .setSmallIcon(R.drawable.ic_notificaiton)
                        .setLargeIcon(BitmapFactory
                                .decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setStyle(bigStyle
                                .bigPicture(bitmap)
                                .setSummaryText(Html.fromHtml(post.getExcerpt()))
                                .setBigContentTitle(post.getTitle()))
                        .setPriority(1)
                        .addAction(R.drawable.ic_action_share, "Share", sharePendingIntent)
                        .setAutoCancel(true);

                // Issues the notification
                NotificationManager mNotifyManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.notify(NOTIF_ID, mBuilder.build());
                Log.d("The Jones Theory", "Notification issued for new post.");
            }
        }
    }