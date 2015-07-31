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

import me.calebjones.blogsite.comments.CommentItem;


public class CommentsLoader extends AsyncTask<String, Void, Integer> {

    public static List<CommentItem> tCommentItemList = new ArrayList<CommentItem>();
    public static final String TAG = "The Jones Theory - PoL";

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
            Log.d("The Jones Theory", "doInBG: " + url.toString());

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
                Log.d("The Jones Theory", "Response: " + response.toString());
                parseResult(response.toString());
                result = 1; // Successful
            }else{
                Log.d("The Jones Theory", "Error: " + statusCode);
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
            Log.d(TAG, "Succeeded fetching data! - POST LOADER");
        } else Log.e(TAG, "Failed to fetch data!");
    }

    public static List<CommentItem> getWords()
    {
        if(tCommentItemList == null)
        {
            tCommentItemList = new ArrayList<>();
        }
        return tCommentItemList;
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
            JSONArray posts = response.optJSONArray("comments");

            Log.d(TAG, "Parsing Results...");
            /*Initialize array if null*/
            if (null == tCommentItemList) {

                tCommentItemList = new ArrayList<CommentItem>();
                Log.d(TAG, "New ArrayList");
            }

            Log.d(TAG, "tCommentItemList");

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                String authorsString = post.optString("author");
                JSONObject authors = new JSONObject(authorsString);

                Log.d(TAG, "JSONArray -" + posts.optJSONObject(i).toString());
                Log.d(TAG, "JSONArray_Author" + authors);
                Log.d(TAG, "JSONArray_Author_optString" + post.optString("author"));
                CommentItem item = new CommentItem();

                item.setName(authors.optString("name"));
                item.setContent(post.optString("content"));
                item.setDate(post.optString("date"));
                item.setID("ID");

                tCommentItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

