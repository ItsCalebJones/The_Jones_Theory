package me.calebjones.blogsite.network;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.models.Posts;

/**
 * Created by cjones on 11/10/15.
 * TODO: Download hit the URL to grab the latest post, parse it, check the DB to see if its new.
 * If it is a new post then notify the user and save to DB.
 */
public class UpdateCheckService extends IntentService {

    public static final int RESTART_CODE = 135325;
    public static final String EXTRA_NUM = "number";
    public static final String NEW_POST = "me.calebjones.blogsite.NEW_POST";
    public static final String LATEST_URL = "https://public-api.wordpress.com/rest/v1.1/sites/" +
            "calebjones.me/posts/?pretty=true&number=1&fields=ID,date,author,content,URL,excerpt," +
            "tags,categories,featured_image,title,type";

    public Posts post;

    public UpdateCheckService() {
        super("UpdateCheckService");
    }

    public UpdateCheckService(String name) {
        super(name);
    }

    //TODO sleep during the hours of 10pm and 8am

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseManager databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();
        Log.d("The Jones Theory", "UpdateCheckService init...");
        try{

            //First make a call to see how many total posts there are and save to 'found'
            final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();

            //Take the response and parse the JSON
            JSONObject JObject = new JSONObject(response.body().string());
            JSONArray nPosts = JObject.optJSONArray("posts");

                for (int i = 0; i < nPosts.length(); i++){
                    JSONObject post = nPosts.optJSONObject(i);
                    if(!databaseManager.idExists(post.optString("ID"))){
                        Intent notifIntent = new Intent(NEW_POST);
                        saveToDatabase(post);
                        String Image_URL = post.optString("featured_image");
                        if (Image_URL.length() != 0) {
                            Bitmap bitmap = getBitmapFromURL(Image_URL);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] bytes = stream.toByteArray();
                            notifIntent.putExtra("BitmapImage", bytes);
                        }
                        notifIntent.putExtra(EXTRA_NUM, Integer.parseInt(post.optString("ID")));
                        sendBroadcast(notifIntent);
                    }
                }
        } catch (IOException | JSONException | ParseException e) {
            SharedPrefs.getInstance().setDownloading(false);
            e.printStackTrace();
        }
        SharedPrefs.getInstance().setDownloading(false);
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveToDatabase(JSONObject jObject) throws ParseException, JSONException {
        DatabaseManager databaseManager = new DatabaseManager(this);
        post = new Posts();

        //If the item is not a post break out of loop and ignore it
        if (!(jObject.optString("type").equals("post"))) {
            return;
        }
        post.setTitle(jObject.optString("title"));
        post.setContent(jObject.optString("content"));
        post.setExcerpt(jObject.optString("excerpt"));
        post.setPostID(jObject.optInt("ID"));
        post.setURL(jObject.optString("URL"));

        //Parse the Date!
        String date = jObject.optString("date");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date newDate = format.parse(date);

        format = new SimpleDateFormat("yyyy-MM-dd");
        date = format.format(newDate);

        post.setDate(date);


        //Cuts out all the Child nodes and gets only the  tags.
        JSONObject tagObject = new JSONObject(jObject.optString("tags"));
        JSONArray tagArray = tagObject.names();
        String tagsList = null;

        if (tagArray != null && (tagArray.length() > 0)) {

            for (int c = 0; c < tagArray.length(); c++) {
                if (tagsList != null) {
                    String thisTag = tagArray.getString(c);
                    tagsList = tagsList + ", " + thisTag;
                } else {
                    String thisTag = tagArray.getString(c);
                    tagsList = thisTag;
                }
            }
            post.setTags(tagsList);
        } else {
            post.setTags("");
        }

        JSONObject categoryObject = new JSONObject(jObject.optString("categories"));
        JSONArray categoryArray = categoryObject.names();
        String catsList = null;

        if (categoryArray != null && (categoryArray.length() > 0)) {

            for (int c = 0; c < categoryArray.length(); c++) {
                if (catsList != null) {
                    String thisCat = categoryArray.getString(c);
                    catsList = catsList + ", " + thisCat;
                } else {
                    String thisCat = categoryArray.getString(c);
                    catsList = thisCat;
                }
            }
            post.setCategories(catsList);
        } else {
            post.setCategories("");
        }

        Integer ImageLength = jObject.optString("featured_image").length();
        if (ImageLength == 0) {
            post.setFeaturedImage(null);
        } else {
            post.setFeaturedImage(jObject.optString("featured_image"));
        }
        if (post != null) {
            databaseManager.addPost(post);
        }
    }

    @Override
    public void onDestroy() {
        post = null;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, RESTART_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);

        Random r = new Random();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 8);
        calendar.add(Calendar.MINUTE, r.nextInt(240 - 1) + 1);
        Log.d("The Jones Theory", "UpdateCheckService init...calendar.getTimeInMillis()" + calendar.getTimeInMillis());
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        super.onDestroy();
    }
}
