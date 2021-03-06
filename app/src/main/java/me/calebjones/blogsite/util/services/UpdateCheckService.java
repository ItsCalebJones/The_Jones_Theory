package me.calebjones.blogsite.util.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.content.database.DatabaseManager;
import me.calebjones.blogsite.content.database.SharedPrefs;
import me.calebjones.blogsite.content.models.Posts;

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

    private DatabaseManager databaseManager;

    //TODO sleep during the hours of 10pm and 8am

    @Override
    protected void onHandleIntent(Intent intent) {
        databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();
        if (!SharedPrefs.getInstance().getFirstRun() && databaseManager.getCount() > 0) {
            Log.d("The Jones Theory", "UpdateCheckService init...");
            try {
                //First make a call to see how many total posts there are and save to 'found'
                final Response response = BlogsiteApplication.getInstance().client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException();

                //Take the response and parse the JSON
                JSONObject JObject = new JSONObject(response.body().string());
                JSONArray nPosts = JObject.optJSONArray("posts");

                for (int i = 0; i < nPosts.length(); i++) {
                    JSONObject post = nPosts.optJSONObject(i);
                    if (!databaseManager.idExists(post.optString("ID"))) {
                        Intent notifIntent = new Intent(NEW_POST);
                        saveToDatabase(post);
                        String Image_URL = post.optString("featured_image");
                        if (Image_URL.length() != 0) {
                            Bitmap bitmap = getBitmapFromURL(Image_URL);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] bytes = stream.toByteArray();
                            Log.d("The Jones Theory", "byteLength: " + bytes.length);

                            //Compress the Bitmap if its over an arbitrary size that probably could crash at a lower count.
                            if (bytes.length > 524288) {
                                for (int count = 95; (bytes.length > 524288 && count >= 20); count = count - 5) {
                                    stream.reset();
                                    Log.d("The Jones Theory", "BEFORE byteLength - Compression: " + count + " - " + bytes.length + " stream " + stream.size());
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, i, stream);
                                    bytes = stream.toByteArray();
                                    Log.d("The Jones Theory", "AFTER byteLength - Compression: " + count + " - " + bytes.length + " stream " + stream.size());
                                }
                            }
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
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            return BitmapFactory.decodeStream(input, null, options);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveToDatabase(JSONObject jObject) throws ParseException, JSONException {
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
        databaseManager.close();
        post = null;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, RESTART_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);

        Random r = new Random();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String notificationTimer = sharedPreferences
                .getString("notification_sync_time", "4");

        //Init the calendar and calculate the wakeup time.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, Integer.parseInt(notificationTimer));
        calendar.add(Calendar.MINUTE, r.nextInt(30 - 1) + 1);
        calendar.add(Calendar.SECOND, r.nextInt(60 - 1) + 1);

        Log.d("The Jones Theory", "UpdateCheckService init...calendar.getTimeInMillis()" + calendar.getTimeInMillis());
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        super.onDestroy();
    }
}
