/*
 * Copyright 2015, Tanmay Parikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.calebjones.blogsite.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.models.Posts;
import me.calebjones.blogsite.util.ButtonReceiver;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostDownloader extends IntentService {

    public static final String LATEST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/" +
            "calebjones.me/posts/?pretty=true&number=10&fields=ID&order_by=ID&order=desc";
    public static final String POST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/" +
            "calebjones.me/posts/%d/?pretty=true&fields=ID,date,author,content,URL,excerpt,tags," +
            "categories,featured_image,title,type";
    public static final String DOWNLOAD_PROGRESS = "me.calebjones.blogsite.DOWNLOAD_PROGRESS";
    public static final String DOWNLOAD_SUCCESS = "me.calebjones.blogsite.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_FAIL = "me.calebjones.blogsite.DOWNLOAD_FAIL";
    public static final String DOWNLOAD_TODAY = "me.calebjones.blogsite.DOWNLOAD_TODAY";
    public static final String DOWNLOAD_ALL = "me.calebjones.blogsite.DOWNLOAD_ALL";
    public static final String DOWNLOAD_SPECIFIC = "me.calebjones.blogsite.DOWNLOAD_SPECIFIC";
    public static final String DOWNLOAD_MISSING = "me.calebjones.blogsite.DOWNLOAD_MISSING";
    public static final String DOWNLOAD_LAST_TEN = "me.calebjones.blogsite.DOWNLOAD_LAST_TEN";
    public static final String DOWNLOAD_NOTIFICATION = "me.calebjones.blogsite." +
            "DOWNLOAD_NOTIFICATION";
    public static final String NEW_POST = "me.calebjones.blogsite.NEW_POST";

    public static final String COMIC_NUM = "itemNum";
    public static final String PROGRESS = "progress";
    public static final String TITLE = "title";
    public static final String NUMBER = "number";

    private static final int NOTIF_ID = 123321;

    public List<String> postID;
    public NotificationManager mNotifyManager;
    public NotificationCompat.Builder mBuilder;
    public PostDownloader() {
        super("PostDownloaderService");
    }
    public PostDownloader(String name) {
        super(name);
    }

    private String newURL = null;
    private int progress;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("The Jones Theory", "PostDownloader onHandleIntent + " + intent.getAction() + " "
                + SharedPrefs.getInstance().isDownloading());
        if (!SharedPrefs.getInstance().isDownloading()){
            SharedPrefs.getInstance().setLastRedownladTime(System.currentTimeMillis());
            switch (intent.getAction()) {
                case DOWNLOAD_ALL:
                    Log.d("The Jones Theory", "DOWNLOAD ALL - Downloading = "
                            + SharedPrefs.getInstance().isDownloading());
                    notificationService();
                    getPostAll();
                    break;
                case DOWNLOAD_MISSING:
                    Log.d("The Jones Theory", "DOWNLOAD MISSING - Downloading = "
                            + SharedPrefs.getInstance().isDownloading());
                    notificationService();
                    getPostMissing();
                    break;
            }
        }
    }

    private void notificationService(){
        //Create an Intent for the BroadcastReceiver
        Intent buttonIntent = new Intent(this, ButtonReceiver.class);
        buttonIntent.putExtra("notificationId", NOTIF_ID);

        //Create the PendingIntent
        PendingIntent cancelNotification = PendingIntent.getBroadcast(this, 0, buttonIntent, 0);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("The Jones Theory")
                .setOngoing(true)
                .setContentText("Preparing to download Science...")
                .addAction(R.drawable.ic_action_close, "Hide", cancelNotification)
                .setSmallIcon(R.drawable.ic_action_file_download)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.ic_launcher));

        // Sets an activity indicator for an operation of indeterminate length
        mBuilder.setProgress(0, 0, true);
        // Issues the notification
        mNotifyManager.notify(NOTIF_ID, mBuilder.build());
    }

    private void getPostAll(){

        //Setup the URLS that I will need
        String firstUrl = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/" +
                "?pretty=true&number=100&fields=ID,title&order_by=ID";
        String nextPage = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/" +
                "?pretty=true&number=100&fields=ID,title&order_by=ID&page_handle=";

        Request request = new Request.Builder().url(firstUrl).build();

        int count = 0;

        try{

            //First make a call to see how many total posts there are and save to 'found'
            final Response response = BlogsiteApplication.getInstance()
                    .client.newCall(request).execute();
            if (!response.isSuccessful()){
                mBuilder.setContentText("Download failed.")
                        .setProgress(0, 0, false)
                        .setAutoCancel(true)
                        .setOngoing(false);
                mNotifyManager.notify(NOTIF_ID, mBuilder.build());
                throw new IOException();
            } else {

                //Take the response and parse the JSON
                JSONObject JObject = new JSONObject(response.body().string());
                JSONArray posts = JObject.optJSONArray("posts");

                //Store the data into the two objects, meta gets the next page later on.
                // Found is total post count.
                String meta = JObject.optString("meta");
                int found = JObject.optInt("found");

                postID = new ArrayList<>();

                //If there are more then 100, which there always will unless something
                // catastrophic happens then set up the newURL.
                if (found > 100) {
                    JSONObject metaLink = new JSONObject(meta);
                    String nextValue = metaLink.optString("next_page");
                    newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
                    Log.d("The Jones Theory", newURL);
                }

                // Loop through the posts and add the post ID to the array.
                // The posts is still from the original call.
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject post = posts.optJSONObject(i);
                    postID.add(post.optString("ID"));
                    count++;
                }

                //Now this logic is in charge of loading the next pages
                // until all posts are loaded into the array.
                while (count != found) {
                    Request newRequest = new Request.Builder().url(newURL).build();
                    Response newResponse = BlogsiteApplication.getInstance()
                            .client.newCall(newRequest).execute();
                    if (!newResponse.isSuccessful()) throw new IOException();

                    JSONObject nJObject = new JSONObject(newResponse.body().string());
                    JSONArray nPosts = nJObject.optJSONArray("posts");
                    String nMeta = nJObject.optString("meta");
                    int newFound = nJObject.optInt("found");

                    if (newFound > 100) {
                        JSONObject metaLink = new JSONObject(nMeta);
                        String nextValue = metaLink.optString("next_page");
                        newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
                    }
                    for (int i = 0; i < nPosts.length(); i++) {
                        JSONObject post = nPosts.optJSONObject(i);
                        postID.add(post.optString("ID"));
                        count++;
                    }
                }
                Collections.reverse(postID);
                download(postID);
                Log.d("The Jones Theory", "getPostAll - Downloading = "
                        + SharedPrefs.getInstance().isDownloading());
            }
        } catch (IOException | JSONException e) {
            if (SharedPrefs.getInstance().isFirstDownload()){
                SharedPrefs.getInstance().setFirstDownload(false);
            }
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
        }
    }

    private void getPostMissing(){
        notificationService();
        SharedPrefs.getInstance().setDownloading(true);
        DatabaseManager databaseManager = new DatabaseManager(this);
        //Setup the URLS that I will need
        String firstUrl = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/" +
                "?pretty=true&number=100&fields=ID,title&order_by=ID";
        String nextPage = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/" +
                "?pretty=true&number=100&fields=ID,title&order_by=ID&page_handle=";
        Request request = new Request.Builder().url(firstUrl).build();
        int count = 0;


        try{

            //First make a call to see how many total posts there are and save to 'found'
            final Response response = BlogsiteApplication.getInstance().client.newCall(request)
                    .execute();
            if (!response.isSuccessful()) throw new IOException();

            //Take the response and parse the JSON
            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray posts = JObject.optJSONArray("posts");

            //Store the data into the two objects, meta gets the next page later on.
            // Found is total post count.
            String meta = JObject.optString("meta");
            int found = JObject.optInt("found");

            postID = new ArrayList<>();

            //If there are more then 100, which there always will unless something
            // catastrophic happens then set up the newURL.
            if (found > 100){
                JSONObject metaLink = new JSONObject(meta);
                String nextValue = metaLink.optString("next_page");
                newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
            }

            // Loop through the posts and add the post ID to the array.
            // The posts is still from the original call.
            for (int i = 0; i < posts.length(); i++){
                JSONObject post = posts.optJSONObject(i);
                if(!databaseManager.idExists(post.optString("ID"))){
                    postID.add(post.optString("ID"));
                }
                count++;
            }

            //Now this logic is in charge of loading the next pages
            // until all posts are loaded into the array.
            while (count != found){
                Request newRequest = new Request.Builder().url(newURL).build();
                Response newResponse = BlogsiteApplication.getInstance().client
                        .newCall(newRequest)
                        .execute();
                if (!newResponse.isSuccessful()) throw new IOException();

                JSONObject nJObject = new JSONObject(newResponse.body().string());
                JSONArray nPosts = nJObject.optJSONArray("posts");
                String nMeta = nJObject.optString("meta");
                int newFound = nJObject.optInt("found");

                if (newFound > 100){
                    JSONObject metaLink = new JSONObject(nMeta);
                    String nextValue = metaLink.optString("next_page");
                    newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");

                }

                for (int i = 0; i < nPosts.length(); i++){
                    JSONObject post = nPosts.optJSONObject(i);
                    if(!databaseManager.idExists(post.optString("ID"))){
                        postID.add(post.optString("ID"));
                    }
                    count++;
                }
            }
        } catch (IOException | JSONException e) {
            if (SharedPrefs.getInstance().isFirstDownload()){
                SharedPrefs.getInstance().setFirstDownload(false);
            }
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
        }
        Collections.reverse(postID);
        download(postID);
    }

    public void download(final List<String> postID) {
        final DatabaseManager databaseManager = new DatabaseManager(this);

        try {
            final int num = postID.size() - 1;
            final CountDownLatch latch = new CountDownLatch(num);
            final Executor executor = Executors.newFixedThreadPool(15);

            for (int i = 1; i <= postID.size() - 1; i++) {
                final int count = i;
                final int index = Integer.parseInt(postID.get(i));
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (index != 404) {
                                String url = String.format(POST_URL, index);
                                Request newReq = new Request.Builder().url(url).build();
                                Response newResp = BlogsiteApplication.getInstance()
                                        .client.newCall(newReq).execute();

                                if (!newResp.isSuccessful() && !(newResp.code() == 404)
                                        && !(newResp.code() == 403)) {
                                    Log.e("The Jones Theory", "Error: " + newResp.code()
                                            + "URL: " + url);
                                    LocalBroadcastManager.getInstance(PostDownloader.this)
                                            .sendBroadcast(new Intent(DOWNLOAD_FAIL));
                                    throw new IOException();
                                }

                                if (newResp.code() == 404) {
                                    return;
                                }

                                String resp = newResp.body().string();

                                JSONObject jObject = new JSONObject(resp);

                                Posts item1 = new Posts();

                                //If the item is not a post break out of loop and ignore it
                                if (!(jObject.optString("type").equals("post"))) {
                                    return;
                                }
                                item1.setTitle(jObject.optString("title"));
                                item1.setContent(jObject.optString("content"));
                                item1.setExcerpt(jObject.optString("excerpt"));
                                item1.setPostID(jObject.optInt("ID"));
                                item1.setURL(jObject.optString("URL"));

                                //Parse the Date!
                                String date = jObject.optString("date");
                                SimpleDateFormat format =
                                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                Date newDate = format.parse(date);

                                format = new SimpleDateFormat("yyyy-MM-dd");
                                date = format.format(newDate);

                                item1.setDate(date);


                                //Cuts out all the Child nodes and gets only the  tags.
                                JSONObject Tobj = new JSONObject(jObject.optString("tags"));
                                JSONArray Tarray = Tobj.names();
                                String tagsList = null;

                                if (Tarray != null && (Tarray.length() > 0)) {

                                    for (int c = 0; c < Tarray.length(); c++) {
                                        if (tagsList != null) {
                                            String thisTag = Tarray.getString(c);
                                            tagsList = tagsList + ", " + thisTag;
                                        } else {
                                            String thisTag = Tarray.getString(c);
                                            tagsList = thisTag;
                                        }
                                    }
                                    item1.setTags(tagsList);
                                } else {
                                    item1.setTags("");
                                }

                                JSONObject Cobj = new JSONObject(jObject.optString("categories"));
                                JSONArray Carray = Cobj.names();
                                String catsList = null;

                                if (Carray != null && (Carray.length() > 0)) {

                                    for (int c = 0; c < Carray.length(); c++) {
                                        if (catsList != null) {
                                            String thisCat = Carray.getString(c);
                                            catsList = catsList + ", " + thisCat;
                                        } else {
                                            String thisCat = Carray.getString(c);
                                            catsList = thisCat;
                                        }
                                    }
                                    item1.setCategories(catsList);
                                } else {
                                    item1.setCategories("");
                                }

                                Integer ImageLength = jObject.optString("featured_image").length();
                                if (ImageLength == 0) {
                                    item1.setFeaturedImage(null);
                                } else {
                                    item1.setFeaturedImage(jObject.optString("featured_image"));
                                }
                                if (item1 != null) {
                                    databaseManager.addPost(item1);
                                    Log.d("PostDownloader", index + " database...");
                                    double progress = ((double) count / num) * 100;
                                    setProgress((int) progress, item1.getTitle(), count);

                                    Intent intent = new Intent(DOWNLOAD_PROGRESS);
                                    intent.putExtra(PROGRESS, progress);
                                    intent.putExtra(NUMBER, num);
                                    intent.putExtra(TITLE, item1.getTitle());

                                    LocalBroadcastManager.getInstance(PostDownloader.this)
                                            .sendBroadcast(intent);
                                }
                            }
                        } catch (IOException | JSONException | ParseException e) {
                            if (SharedPrefs.getInstance().isFirstDownload()){
                                SharedPrefs.getInstance().setFirstDownload(false);
                            }
                            SharedPrefs.getInstance().setDownloading(false);
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                if (SharedPrefs.getInstance().isFirstDownload()){
                    SharedPrefs.getInstance().setFirstDownload(false);
                }
                SharedPrefs.getInstance().setDownloading(false);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                mBuilder.setContentText("Download failed.")
                        .setSmallIcon(R.drawable.ic_action_file_download)
                        .setAutoCancel(true)
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                mNotifyManager.notify(NOTIF_ID, mBuilder.build());
                throw new IOException(e);
            }
            if (SharedPrefs.getInstance().isFirstDownload()){
                SharedPrefs.getInstance().setFirstDownload(false);
            }
            SharedPrefs.getInstance().setDownloading(false);

            Log.d("PostDownloader", "Broadcast Sent!");
            Log.d("The Jones Theory", "download - Downloading = "
                    + SharedPrefs.getInstance().isDownloading());

            Intent mainActIntent = new Intent(this, MainActivity.class);
            PendingIntent clickIntent = PendingIntent.getActivity(this, 57836, mainActIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
            mBuilder.setContentText("Download complete")
                    .setSmallIcon(R.drawable.ic_action_done)
                    .setProgress(0, 0, false)
                    .setContentIntent(clickIntent)
                    .setAutoCancel(true)
                    .setOngoing(false);
            mNotifyManager.notify(NOTIF_ID, mBuilder.build());

        } catch (IOException e) {
            if (SharedPrefs.getInstance().isFirstDownload()){
                SharedPrefs.getInstance().setFirstDownload(false);
            }
            SharedPrefs.getInstance().setLastRedownladTime(System.currentTimeMillis());
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
            mBuilder.setContentText("Download failed.")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setOngoing(false);
            mNotifyManager.notify(NOTIF_ID, mBuilder.build());
        }
    }

    private void setProgress(int newProgress, String title, int number){
        if (newProgress > progress) {
            progress = newProgress;

            mBuilder.setProgress(100, progress, false).setContentText("Downloading Science... " + progress + "%").setNumber(number);
            mNotifyManager.notify(NOTIF_ID, mBuilder.build());
        }
    }

}
