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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.models.Posts;

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

    public static final String LATEST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=10&fields=ID&order_by=ID&order=desc";
    public static final String POST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/%d/?pretty=true&fields=ID,date,author,content,URL,excerpt,tags,categories,featured_image,title,type";
    public static final String DOWNLOAD_PROGRESS = "me.calebjones.blogsite.DOWNLOAD_PROGRESS";
    public static final String DOWNLOAD_SUCCESS = "me.calebjones.blogsite.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_FAIL = "me.calebjones.blogsite.DOWNLOAD_FAIL";
    public static final String DOWNLOAD_TODAY = "me.calebjones.blogsite.DOWNLOAD_TODAY";
    public static final String DOWNLOAD_ALL = "me.calebjones.blogsite.DOWNLOAD_ALL";
    public static final String DOWNLOAD_SPECIFIC = "me.calebjones.blogsite.DOWNLOAD_SPECIFIC";
    public static final String DOWNLOAD_MISSING = "me.calebjones.blogsite.DOWNLOAD_MISSING";
    public static final String DOWNLOAD_LAST_TEN = "me.calebjones.blogsite.DOWNLOAD_LAST_TEN";
    public static final String POST_SUCCESS = "me.calebjones.blogsite.POST_SUCCESS";

    public static final String COMIC_NUM = "itemNum";
    public static final String PROGRESS = "progress";
    public static final String TITLE = "title";

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
        switch (intent.getAction()) {
            case DOWNLOAD_TODAY:
                downloadToday();
                break;
            case DOWNLOAD_ALL:
                getPostAll();
                break;
            case DOWNLOAD_SPECIFIC:
                downloadSpecific(intent.getExtras().getInt(COMIC_NUM));
                break;
            case DOWNLOAD_MISSING:
                getPostMissing();
                break;
            case DOWNLOAD_LAST_TEN:
                redownloadLastTen();
        }
    }

    private void notificationService(){
        int id = 1;

        //Create an Intent for the BroadcastReceiver
        Intent buttonIntent = new Intent(this, ButtonReceiver.class);
        buttonIntent.putExtra("notificationId", id);

        //Create the PendingIntent
        PendingIntent cancelNotification = PendingIntent.getBroadcast(this, 0, buttonIntent,0);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("The Jones Theory")
                .setOngoing(true)
                .setContentText("Download in progress...")
                .addAction(R.drawable.ic_action_close, "Hide Notification...", cancelNotification)
                .setSmallIcon(R.drawable.ic_action_file_download)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.ic_launcher));

        // Sets an activity indicator for an operation of indeterminate length
        mBuilder.setProgress(0, 0, true);
        // Issues the notification
        mNotifyManager.notify(id, mBuilder.build());
    }

    private void downloadSpecific(int i) {
        Gson gson = new Gson();
        DatabaseManager databaseManager = new DatabaseManager(this);
        String url = String.format(POST_URL, i);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            Posts item = gson.fromJson(response.body().string(), Posts.class);
            databaseManager.addPost(item);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private void downloadToday() {
        DatabaseManager databaseManager = new DatabaseManager(this);
        Gson gson = new Gson();
        Request request = new Request.Builder().url(LATEST_URL).build();
        try {
            Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            Posts item = gson.fromJson(response.body().string(), Posts.class);
            if (!databaseManager.itemExists(item))
                databaseManager.addPost(item);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private void redownloadLastTen() {
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            Request todayReq = new Request.Builder().url(LATEST_URL).build();
            Response response = BlogsiteApplication.getInstance().client.newCall(todayReq).execute();
            if (!response.isSuccessful()) throw new IOException();
            Posts item = gson.fromJson(response.body().string(), Posts.class);
            final CountDownLatch latch = new CountDownLatch(10);
            final Executor executor = Executors.newFixedThreadPool(5);
            int num = item.getID();
            for (int i = num - 9; i <= num; i++) {
                final int index = i;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = String.format(POST_URL, index);
                            Request request = new Request.Builder().url(url).build();
                            Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
                            if (!response.isSuccessful()) throw new IOException();
                            String responseBody = response.body().string();
                            Posts item = null;
                            try {
                                item = gson.fromJson(responseBody, Posts.class);
                            } catch (JsonSyntaxException e) {
                            }
                            if (item != null) {
                                if (databaseManager.itemExists(item)) {
                                    databaseManager.updatePost(item);
                                } else {
                                    databaseManager.addPost(item);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            LocalBroadcastManager.getInstance(PostDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                LocalBroadcastManager.getInstance(PostDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
            }

            SharedPrefs.getInstance().setLastRedownladTime(System.currentTimeMillis());
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));

        } catch (IOException e) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private void getPostAll(){
        SharedPrefs.getInstance().setDownloading(true);
        //Setup the URLS that I will need
        String firstUrl = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=100&fields=ID,title&order_by=ID";
        String nextPage = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=100&fields=ID,title&order_by=ID&page_handle=";
        Request request = new Request.Builder().url(firstUrl).build();
        int count = 0;


        try{

            //First make a call to see how many total posts there are and save to 'found'
            final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();

            //Take the response and parse the JSON
            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray posts = JObject.optJSONArray("posts");

            //Store the data into the two objects, meta gets the next page later on. Found is total post count.
            String meta = JObject.optString("meta");
            int found = JObject.optInt("found");

            postID = new ArrayList<>();

            //If there are more then 100, which there always will unless something catastrophic happens then set up the newURL.
            if (found > 100){
                JSONObject metaLink = new JSONObject(meta);
                String nextValue = metaLink.optString("next_page");
                newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
                Log.d("The Jones Theory", newURL);
            }

            // Loop through the posts and add the post ID to the array. The posts is still from the original call.
            for (int i = 0; i < posts.length(); i++){
                JSONObject post = posts.optJSONObject(i);
                postID.add(post.optString("ID"));
                count++;
                Log.d("The Jones Theory", "Count: " + count + " Array: " + postID.get(i));
            }

            //Now this logic is in charge of loading the next pages until all posts are loaded into the array.
            while (count != found){
                Request newRequest = new Request.Builder().url(newURL).build();
                Response newResponse = BlogsiteApplication.getInstance().client.newCall(newRequest).execute();
                if (!newResponse.isSuccessful()) throw new IOException();

                JSONObject nJObject = new JSONObject(newResponse.body().string());
                JSONArray nPosts = nJObject.optJSONArray("posts");
                String nMeta = nJObject.optString("meta");
                int newFound = nJObject.optInt("found");

                if (newFound > 100){
                    JSONObject metaLink = new JSONObject(nMeta);
                    String nextValue = metaLink.optString("next_page");
                    newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
                    Log.d("The Jones Theory", newURL);

                }

                for (int i = 0; i < nPosts.length(); i++){
                    JSONObject post = nPosts.optJSONObject(i);
                    postID.add(post.optString("ID"));
                    count++;
                    Log.d("The Jones Theory", "Count: " + count + " Array: " + postID.get(count - 1));
                }
            }
        } catch (IOException | JSONException e) {
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
        }
        Collections.reverse(postID);
        download(postID);
    }

    private void getPostMissing(){
        SharedPrefs.getInstance().setDownloading(true);
        DatabaseManager databaseManager = new DatabaseManager(this);
        //Setup the URLS that I will need
        String firstUrl = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=100&fields=ID,title&order_by=ID";
        String nextPage = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=100&fields=ID,title&order_by=ID&page_handle=";
        Request request = new Request.Builder().url(firstUrl).build();
        int count = 0;


        try{

            //First make a call to see how many total posts there are and save to 'found'
            final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();

            //Take the response and parse the JSON
            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray posts = JObject.optJSONArray("posts");

            //Store the data into the two objects, meta gets the next page later on. Found is total post count.
            String meta = JObject.optString("meta");
            int found = JObject.optInt("found");

            postID = new ArrayList<>();

            //If there are more then 100, which there always will unless something catastrophic happens then set up the newURL.
            if (found > 100){
                JSONObject metaLink = new JSONObject(meta);
                String nextValue = metaLink.optString("next_page");
                newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
                Log.d("The Jones Theory", newURL);
            }

            // Loop through the posts and add the post ID to the array. The posts is still from the original call.
            for (int i = 0; i < posts.length(); i++){
                JSONObject post = posts.optJSONObject(i);
                if(!databaseManager.idExists(post.optString("ID"))){
                    postID.add(post.optString("ID"));
                }
                count++;
            }

            //Now this logic is in charge of loading the next pages until all posts are loaded into the array.
            while (count != found){
                Request newRequest = new Request.Builder().url(newURL).build();
                Response newResponse = BlogsiteApplication.getInstance().client.newCall(newRequest).execute();
                if (!newResponse.isSuccessful()) throw new IOException();

                JSONObject nJObject = new JSONObject(newResponse.body().string());
                JSONArray nPosts = nJObject.optJSONArray("posts");
                String nMeta = nJObject.optString("meta");
                int newFound = nJObject.optInt("found");

                if (newFound > 100){
                    JSONObject metaLink = new JSONObject(nMeta);
                    String nextValue = metaLink.optString("next_page");
                    newURL = nextPage + URLEncoder.encode(nextValue, "UTF-8");
                    Log.d("The Jones Theory", newURL);

                }

                for (int i = 0; i < nPosts.length(); i++){
                    JSONObject post = nPosts.optJSONObject(i);
                    Log.d("The Jones Theory", post.optString("ID"));
                    if(!databaseManager.idExists(post.optString("ID"))){
                        postID.add(post.optString("ID"));
                    }
                    count++;
                }
            }
        } catch (IOException | JSONException e) {
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
        }
        Collections.reverse(postID);
        download(postID);
    }

    private void download(final List<String> postID) {
        notificationService();

        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();



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
                            Log.d("PostDownloader", "Index: " + index);
                            if (index != 404) {
                                String url = String.format(POST_URL, index);
                                Request newReq = new Request.Builder().url(url).build();
                                Response newResp = BlogsiteApplication.getInstance().client.newCall(newReq).execute();

                                if (!newResp.isSuccessful() && !(newResp.code() == 404) && !(newResp.code() == 403)) {
                                    Log.e("The Jones Theory", "Error: " + newResp.code() + "URL: " + url);
                                    LocalBroadcastManager.getInstance(PostDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
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
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                Date newDate = format.parse(date);

                                Log.d("The Jones Theory", "Date - " + date);

                                format = new SimpleDateFormat("yyyy-MM-dd");
                                date = format.format(newDate);

                                Log.d("The Jones Theory", "Date - " + date);
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
                                    setProgress((int) progress, item1.getTitle(), progress);
                                }
                            }
                        } catch (IOException | JSONException | ParseException e) {
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
                SharedPrefs.getInstance().setDownloading(false);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                mBuilder.setContentText("Download failed.")
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                mNotifyManager.notify(1, mBuilder.build());
                throw new IOException(e);
            }
            SharedPrefs.getInstance().setDownloading(false);
            Log.d("PostDownloader", "Broadcast Sent!");

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));

            mBuilder.setContentText("Download complete")
                    .setProgress(0, 0, false)
                    .setOngoing(false);
            mNotifyManager.notify(1, mBuilder.build());


            // Start a lengthy operation in a background thread
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5*1000);
                                // When the loop is finished, updates the notification
                                mBuilder.setContentText("Download complete")
                                        // Removes the progress bar
                                        .setProgress(0,0,false);
                                mNotifyManager.cancel(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

            ).start();
        } catch (IOException e) {
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
            mBuilder.setContentText("Download failed.")
                    .setProgress(0, 0, false)
                    .setOngoing(false);
            mNotifyManager.notify(1, mBuilder.build());
        }
    }

    private void setProgress(int newProgress, String title, double mProgress){
        if (newProgress > progress){
            progress = newProgress;

            mBuilder.setProgress(100, progress, false);
            mNotifyManager.notify(1, mBuilder.build());

            Intent intent = new Intent(DOWNLOAD_PROGRESS);
            intent.putExtra(PROGRESS, mProgress);
            intent.putExtra(TITLE, title);

            LocalBroadcastManager.getInstance(PostDownloader.this).sendBroadcast(intent);
        }
    }

    public class ButtonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("The Jones Theory", "Notification button pushed!");

            mNotifyManager.cancel(1);
        }
    }

}
