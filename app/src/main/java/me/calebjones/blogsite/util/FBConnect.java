package me.calebjones.blogsite.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by cjones on 7/9/15.
 */
public class FBConnect extends AsyncTask<String, Void, Boolean> {
    HttpURLConnection urlConnection;

    public String cookieStr;
    public final String charset = "UTF-8";
    public String error;
    public String TAG = "The Jones Theory - dA";

    public FBConnect(String cookie) {
        cookieStr = cookie;
        Log.v(TAG, "Constructor: " + cookieStr);
    }

    @Override
    protected Boolean doInBackground(String... args) {

        String result = "empty";
        Boolean valid;
        StringBuilder sResult = new StringBuilder();

        try {
            //Set up the Query and add in the aparamters
            String query = String.format("access_token=%s",
                    URLEncoder.encode(cookieStr, charset));

            Log.v(TAG, "doInBg: Query -" + query);

            URL url = new URL("http://calebjones.me/api/user/fb_connect/");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            Log.v(TAG, urlConnection.toString());
            urlConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length", "" +
                    Integer.toString(query.getBytes().length));
            urlConnection.setRequestProperty("Content-Language", "en-US");
            Log.v(TAG, "URL " + urlConnection.toString());

            DataOutputStream wr = new DataOutputStream(
                    urlConnection.getOutputStream());
            wr.writeBytes(query);
            wr.flush();
            wr.close();

            InputStream inP = new BufferedInputStream(urlConnection.getInputStream());
            Log.v(TAG, "InputStream: " + inP.toString());

            BufferedReader reader = new BufferedReader(new InputStreamReader(inP));
            Log.v(TAG, "Reader: " + reader.toString());
            String line;
            while ((line = reader.readLine()) != null) {
                sResult.append(line);
                Log.v("The Jones Theory", "doInBg: While Line -" + line);
            }
            result = sResult.toString();
//                result = parseCookie(result);

            JSONObject response = new JSONObject(result);
            String validStr = response.optString("status");
            cookieStr = response.optString("cookie");

            if (validStr.equals("error")){
                return false;
            } else if (validStr.equals("ok")){
                return true;
            }

        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            Log.v(TAG, "doInBg: urlConnection.disconnect();");
            urlConnection.disconnect();
        }

        Log.v(TAG, "doInBg: something happened");
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //Do something with the JSON string
        Log.v(TAG, "onPostExecute: " + result);

        if (result){
            //DO THIS
        } else {
            //DO THAT
        }
    }
}

