package me.calebjones.blogsite.network;

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

import me.calebjones.blogsite.ui.fragments.PhotoFragment;
import me.calebjones.blogsite.content.models.ImageItem;

public class PhotoLoader extends AsyncTask<String, Void, Integer> {

    public static List<ImageItem> mGridItemList = new ArrayList<ImageItem>();
    public static final String TAG = "The Jones Theory - PhL";
    private PhotoFragment photoGrid;

        @Override
        protected void onPreExecute() {

            if (mGridItemList != null ) {
                mGridItemList.clear();
            }
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
//                BlogFragment.setList(feedItemList);
                Log.d(TAG, "Succeeded fetching data!");
            } else Log.e(TAG, "Failed to fetch data!");
        }

    public static List<ImageItem> getWords()
    {
        if(mGridItemList == null)
        {
            mGridItemList = new ArrayList<ImageItem>();
        }
        return mGridItemList;
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
            if (null == mGridItemList) {
                mGridItemList = new ArrayList<ImageItem>();
            }

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);

                ImageItem item = new ImageItem();
                item.setTitle(post.optString("title"));
                item.setContent(post.optString("content"));
                item.setExcerpt(post.optString("excerpt"));
                item.setID(post.optString("ID"));
                item.setpostURL(post.optString("URL"));
                Integer ImageLength = post.optString("featured_image").length();
                if (ImageLength == 0) {
                    Log.d(TAG, "It should be null!");
                    item.setThumbnail(null);
                } else {
                    item.setThumbnail(post.optString("featured_image"));
                }
                mGridItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
