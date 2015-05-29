package me.calebjones.thejonestheory.loader;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import me.calebjones.thejonestheory.feed.FeedItem;

/**
 * Created by cjones on 5/27/15.
 */
public class PostLoader extends AsyncTask<String, Void, Integer> {


    public static List<FeedItem> feedItemList = new ArrayList<FeedItem>();
    public static final String TAG = "The Jones Theory";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            Integer result = 0;
            HttpURLConnection urlConnection = null;

            try {
                /* forming th java.net.URL object */
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                /* for Get request */
                urlConnection.setRequestMethod("GET");

                int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
                if (statusCode ==  200) {

                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }

                    parseResult(response.toString());
                    result = 1; // Successful
                }else{
                    result = 0; //"Failed to fetch data!";
                }

            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            if (result == 1) {
//                FetchViewBackground.setList(feedItemList);
                Log.e(TAG, "Succeeded fetching data!");
            } else Log.e(TAG, "Failed to fetch data!");
        }

    public static List<FeedItem> getWords()
    {
        if(feedItemList == null)
        {
            feedItemList = new ArrayList<FeedItem>();
        }
        return feedItemList;
    }


    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("posts");

            /*Initialize array if null*/
            if (null == feedItemList) {
                feedItemList = new ArrayList<FeedItem>();
            }

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);

                FeedItem item = new FeedItem();
                item.setTitle(post.optString("title"));
                item.setContent(post.optString("content"));
                item.setExcerpt(post.optString("excerpt"));
                item.setID(post.optString("ID"));
                Integer ImageLength = post.optString("featured_image").length();
                if (ImageLength == 0) {
                    Log.d(TAG, "It should be null!");
                    item.setThumbnail(null);
                } else {
                    item.setThumbnail(post.optString("featured_image"));
                }
                feedItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
