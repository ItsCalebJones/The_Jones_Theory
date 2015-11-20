package me.calebjones.blogsite.util.auth;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
 * doAuthGlobal class
 */
public class AuthValidate extends AsyncTask<String, Void, Boolean>{
    HttpURLConnection urlConnection;

    public final String cookieStr;
    public final String charset = "UTF-8";
    public String error;
    public String status;
    public String valid;
    public String TAG = "The Jones Theory-dA";

    public AuthValidate(String cookie) {
        cookieStr = cookie;
        Log.v(TAG, "Constructor: " + cookieStr);
    }

    @Override
    protected Boolean doInBackground(String... args) {

        String result = "empty";
        StringBuilder sResult = new StringBuilder();

        try {
            //Set up the Query and add in the aparamters
            String query = String.format("cookie=%s",
                    URLEncoder.encode(cookieStr, charset));

            Log.v(TAG, "doInBg: Query -" + query);

            URL url = new URL("http://calebjones.me/api/user/validate_auth_cookie/");
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
                Log.v(TAG, "doInBg: While Line - " + line);
            }
            result = sResult.toString();
//                result = parseCookie(result);

            JSONObject response = new JSONObject(result);
            String validStr = response.optString("valid");
            status = response.optString("status");
            error = response.optString("error");
            valid = response.optString("valid");

            if (status.equals("error")){
                return false;
            } else if (status.equals("ok") && valid.equals("true")){
                return true;
            } else {
                return false;
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
            Log.d(TAG, "AuthValidate -" + status);
        } else {
            //DO THAT
            Log.d(TAG, "AuthValidate -" + valid + " " + error);
        }
        }
    }

