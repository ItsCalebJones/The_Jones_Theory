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

package me.calebjones.blogsite.loader;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.models.Posts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostDownloader extends IntentService {

    public static final String LATEST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=1&fields=ID,date,author,content,URL,excerpt,tags,categories,featured_image,title";
    public static final String POST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/%d/?pretty=true&fields=ID,date,author,content,URL,excerpt,tags,categories,featured_image,title,type";

    public static final String DOWNLOAD_PROGRESS = "me.calebjones.blogsite.DOWNLOAD_PROGRESS";
    public static final String DOWNLOAD_SUCCESS = "me.calebjones.blogsite.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_FAIL = "me.calebjones.blogsite.DOWNLOAD_FAIL";

    public static final String DOWNLOAD_TODAY = "me.calebjones.blogsite.DOWNLOAD_TODAY";
    public static final String DOWNLOAD_ALL = "me.calebjones.blogsite.DOWNLOAD_ALL";
    public static final String DOWNLOAD_SPECIFIC = "me.calebjones.blogsite.DOWNLOAD_SPECIFIC";
    public static final String DOWNLOAD_MISSING = "me.calebjones.blogsite.DOWNLOAD_MISSING";
    public static final String DOWNLOAD_LAST_TEN = "me.calebjones.blogsite.DOWNLOAD_LAST_TEN";

    public static final String COMIC_NUM = "itemNum";
    public static final String PROGRESS = "progress";
    public static final String TITLE = "title";

    public List<String> postID;

    public PostDownloader() {
        super("PostDownloaderService");
    }

    public PostDownloader(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case DOWNLOAD_TODAY:
                downloadToday();
                break;
            case DOWNLOAD_ALL:
                downloadAll();
                break;
            case DOWNLOAD_SPECIFIC:
                downloadSpecific(intent.getExtras().getInt(COMIC_NUM));
                break;
            case DOWNLOAD_MISSING:
                downloadMissing();
                break;
            case DOWNLOAD_LAST_TEN:
                redownloadLastTen();
        }
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

    private void downloadAllMissingTranscripts() {
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        List<Integer> nums = databaseManager.getAllMissingTranscripts();

        final CountDownLatch latch = new CountDownLatch(nums.size());
        final Executor executor = Executors.newFixedThreadPool(nums.size() / 2);
        for (int i : nums) {
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
                            databaseManager.updatePost(item);
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
        SharedPrefs.getInstance().setLastTranscriptCheckTime(System.currentTimeMillis());
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));

    }

    private void getPostID(){
        String firstUrl = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=100&fields=ID";
        String nextPage = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/?pretty=true&number=100&fields=ID&page_handle=";
        String newURL = null;
        Request request = new Request.Builder().url(firstUrl).build();
        int count = 0;

        try{
            final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();

            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray posts = JObject.optJSONArray("posts");
            String meta = JObject.optString("meta");
            int found = JObject.optInt("found");
            postID = new ArrayList<>();

            if (found > 100){
                JSONObject metaLink = new JSONObject(meta);
                String nextValue = metaLink.optString("next_page");
                newURL = nextPage + nextValue;
                Log.d("The Jones Theory", nextPage);

            }

            for (int i = 0; i < posts.length(); i++){
                JSONObject post = posts.optJSONObject(i);
                postID.add(post.optString("ID"));
                count++;
                Log.d("The Jones Theory", "Count: " + count + "Array: " + postID.get(i));
            }

            while (count != found){
                Request newRequest = new Request.Builder().url(newURL).build();
                Response newResponse = BlogsiteApplication.getInstance().client.newCall(newRequest).execute();
                if (!newResponse.isSuccessful()) throw new IOException();

                JSONObject nJObject = new JSONObject(newResponse.body().string());
                JSONArray nposts = JObject.optJSONArray("posts");
                String nmeta = JObject.optString("meta");

                if (found > 100){
                    JSONObject metaLink = new JSONObject(meta);
                    String nextValue = metaLink.optString("next_page");
                    newURL = nextPage + nextValue;
                    Log.d("The Jones Theory", nextPage);

                }

                for (int i = 0; i < posts.length(); i++){
                    JSONObject post = posts.optJSONObject(i);
                    postID.add(post.optString("ID"));
                    count++;
                    Log.d("The Jones Theory", postID.get(i));
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    private void downloadMissing(){
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();

        List<Posts> count = databaseManager.getAllPosts();
        int counter = count.get(count.size() -1).getPostID();

        Log.d("The Jones Theory", "Counter: " + String.valueOf(counter));

        try {
            final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();

            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray posts = JObject.optJSONArray("posts");

            Log.d("PostDownloader", posts.toString());

            JSONObject post = posts.optJSONObject(0);


            Posts item = new Posts();
            item.setTitle(post.optString("title"));
            item.setContent(post.optString("content"));
            item.setExcerpt(post.optString("excerpt"));
            item.setPostID(post.optInt("ID"));
            item.setCategories(post.optString("categories"));
            item.setTags(post.optString("tags"));
            item.setURL(post.optString("URL"));
            Integer ImageLength = post.optString("featured_image").length();
            if (ImageLength == 0) {
                item.setFeaturedImage(null);
            } else {
                item.setFeaturedImage(post.optString("featured_image"));
            }

            final int num = item.getPostID();
            Log.d("The Jones Theory", "Latest: " + String.valueOf(num));
            final CountDownLatch latch = new CountDownLatch(num - counter);
            final Executor executor = Executors.newFixedThreadPool(10);

            for (int i = counter; i <= num; i++) {
                final int index = i;
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


                                //Cuts out all the Child nodes and gets only the  tags.
                                JSONObject  Tobj = new JSONObject(jObject.optString("tags"));
                                JSONArray Tarray = Tobj.names();
                                String tagsList = null;

                                if (Tarray != null) {

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

                                JSONObject  Cobj = new JSONObject(jObject.optString("categories"));
                                JSONArray Carray = Cobj.names();
                                String catsList = null;

                                if (Tarray != null) {

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
                                }
                            }
                        } catch (IOException | JSONException e) {
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
                throw new IOException(e);
            }
            Log.d("PostDownloader", "Complete!");
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadAll() {
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();

        //This gets the latest post count so that we can increment the counter somewhat accurately.
        try {
            final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();

            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray posts = JObject.optJSONArray("posts");

            Log.d("PostDownloader", posts.toString());

            JSONObject post = posts.optJSONObject(0);


            Posts item = new Posts();
            item.setTitle(post.optString("title"));
            item.setContent(post.optString("content"));
            item.setExcerpt(post.optString("excerpt"));
            item.setPostID(post.optInt("ID"));
            item.setCategories(post.optString("categories"));
            item.setTags(post.optString("tags"));
            item.setURL(post.optString("URL"));
            Integer ImageLength = post.optString("featured_image").length();
            if (ImageLength == 0) {
                item.setFeaturedImage(null);
            } else {
                item.setFeaturedImage(post.optString("featured_image"));
            }

            final int num = item.getPostID();
            final CountDownLatch latch = new CountDownLatch(num);
            final Executor executor = Executors.newFixedThreadPool(10);

            for (int i = 1; i <= num; i++) {
                final int index = i;
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


                                //Cuts out all the Child nodes and gets only the  tags.
                                JSONObject  Tobj = new JSONObject(jObject.optString("tags"));
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

                                JSONObject  Cobj = new JSONObject(jObject.optString("categories"));
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
                                    double progress = ((double) index / num) * 100;
                                    Intent intent = new Intent(DOWNLOAD_PROGRESS);
                                    intent.putExtra(PROGRESS, progress);
                                    intent.putExtra(TITLE, item1.getTitle());
                                    LocalBroadcastManager.getInstance(PostDownloader.this).sendBroadcast(intent);
                                }
                            }
                        } catch (IOException | JSONException e) {
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
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                throw new IOException(e);
            }
            Log.d("PostDownloader", "Broadcast Sent!");
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

}
