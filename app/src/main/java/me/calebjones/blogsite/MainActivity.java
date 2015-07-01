package me.calebjones.blogsite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import me.calebjones.blogsite.activity.LoginActivity;
import me.calebjones.blogsite.activity.SettingsActivity;
import me.calebjones.blogsite.fragments.FetchViewBackground;
import me.calebjones.blogsite.fragments.PhotoGridView;
import me.calebjones.blogsite.fragments.SettingsFragment;
import me.calebjones.blogsite.fragments.WebView;
import me.calebjones.blogsite.loader.PhotoLoader;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private String pURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=20";
    private doAuthValidate doAuth = null;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    //Public Variables
    public static final String TAG = "The Jones Theory - M";
    public Context context;


    int i = 0;
    int mCurCheckPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);


        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        if (savedInstanceState == null) {
            new PhotoLoader().execute(pURL);

            //Load initial Fragment
            FetchViewBackground myBlogFragment = new FetchViewBackground();
            android.support.v4.app.FragmentTransaction mBlogTransaction = getSupportFragmentManager().beginTransaction();

            Bundle hBundle = new Bundle();
            hBundle.putString("category", "blog");
            myBlogFragment.setArguments(hBundle);

            mBlogTransaction.replace(R.id.frame, myBlogFragment);
            mBlogTransaction.commit();
        }

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            Log.v(TAG + "-S", "onCreate: " + mCurCheckPosition);
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);

            Log.v(TAG + "-S", "onCreate: " + mCurCheckPosition);

            //Check to see which item was being clicked and perform appropriate action
            switch (mCurCheckPosition) {

                case R.id.home:
                    homeFragment();
                case R.id.about:
                    webFragment();
                case R.id.gallery:
                    galleryFragment();
                case R.id.science:
                    scienceFragment();
                case R.id.parenting:
                    parentingFragment();
                case R.id.android:
                    androidFragment();
                case R.id.technology:
                    technologyFragment();
                default:
                    homeFragment();
            }

        }



        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                Log.v(TAG + "-S", "Pre_Switch = " + mCurCheckPosition);

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {


                    case R.id.home:
                        mCurCheckPosition = R.id.home;
                        homeFragment();
                        Log.v(TAG + "-S", "Home = " + R.id.home + " " + mCurCheckPosition);
                        return true;
                    case R.id.about:
                        mCurCheckPosition = R.id.about;
                        webFragment();
                        return true;
                    case R.id.gallery:
                        Log.v(TAG + "-S", "Switch = " + mCurCheckPosition);
                        mCurCheckPosition = R.id.gallery;
//                        Toast.makeText(getApplicationContext(), "Gallery coming soon!", Toast.LENGTH_SHORT).show();
                        galleryFragment();
                        return true;
                    case R.id.science:
                        Log.v(TAG + "-S", "Science = " + R.id.science + " " + mCurCheckPosition);
                        mCurCheckPosition = R.id.science;
                        scienceFragment();
                        return true;
                    case R.id.parenting:
                        Log.v(TAG + "-S", "Parenting = " + R.id.parenting + " " + mCurCheckPosition);
                        mCurCheckPosition = R.id.parenting;
                        Log.v(TAG + "-S", "Parenting = " + R.id.parenting + " " + mCurCheckPosition);
                        parentingFragment();
                        return true;
                    case R.id.android:
                        Log.v(TAG + "-S", "Switch = " + mCurCheckPosition);
                        mCurCheckPosition = R.id.android;
                        androidFragment();
                        return true;
                    case R.id.technology:
                        Log.v(TAG + "-S", "Home = " + R.id.technology + " " + mCurCheckPosition);
                        mCurCheckPosition = R.id.technology;
                        technologyFragment();
                        return true;
                    case R.id.settings:
                        Log.v(TAG + "-S", "Switch = " + mCurCheckPosition);
                        mCurCheckPosition = R.id.settings;
                        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(i);
                    default:
                        return true;


                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(TAG + "-S", "onSaved: " + mCurCheckPosition);
        outState.putInt("curChoice", mCurCheckPosition);
        Log.v(TAG + "-S", "onSaved-outState: " + mCurCheckPosition);
    }

    // TODO Fix SavedInstance by getting the active fragment and making sure its set correctly.
    @Override
    public void onResume() {
        SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
        SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean loginCheck = sharedPerf.getBoolean("prompt_logged_out", false);
        Log.v(TAG, "Prompt To login: " + Boolean.toString(loginCheck));
        if (loginCheck){
            String cookie = prefs.getString("AUTH_COOKIE", "");
            if (cookie.length() > 10){
                Log.v(TAG, "Cookie: " + cookie);
//                Toast.makeText(getApplicationContext(), cookie, Toast.LENGTH_SHORT).show();
                checkAuthKey(cookie);
            } else {
                showLogin();
            }
        }

        boolean previouslyStarted = prefs.getBoolean("PREVIOUSLY_STARTED_KEY", false);
        Log.d(TAG, "Previously Started = " + Boolean.toString(previouslyStarted));
        if(!previouslyStarted){
            showLogin();
        }

        String cookie = prefs.getString("AUTH_COOKIE", null);
        if (cookie != null){
            Log.v(TAG, cookie);
        }

        if (cookie != null) {
//            Toast.makeText(getApplicationContext(), cookie, Toast.LENGTH_SHORT).show();
            checkAuthKey(cookie);
        }
//        // Logs 'install' and 'app activate' App Events.
//        AppEventsLogger.activateApp(this);
        super.onResume();
    }

    private void checkAuthKey(String cookie) {
        doAuth = new doAuthValidate(cookie);
        doAuth.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();

//        // Logs 'app deactivate' App Event.
//        AppEventsLogger.deactivateApp(this);
    }

    private void showLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void galleryFragment() {
        //Set up the Fragment transaction
        PhotoGridView photoFragment = new PhotoGridView();
        android.support.v4.app.FragmentTransaction mPhotoTransaction = getSupportFragmentManager().beginTransaction();

        //Execute the transaction
        mPhotoTransaction.replace(R.id.frame, photoFragment);
        mPhotoTransaction.commit();
    }

    private void technologyFragment() {
        FetchViewBackground myTechFragment = new FetchViewBackground();
        android.support.v4.app.FragmentTransaction mTechTransaction = getSupportFragmentManager().beginTransaction();

        Bundle tBundle = new Bundle();
        tBundle.putString("category", "technology");
        myTechFragment.setArguments(tBundle);

        mTechTransaction.replace(R.id.frame, myTechFragment);
        mTechTransaction.commit();
    }

    private void androidFragment() {
        FetchViewBackground myAndroidFragment = new FetchViewBackground();
        android.support.v4.app.FragmentTransaction mAndroidTransaction = getSupportFragmentManager().beginTransaction();

        Bundle aBundle = new Bundle();
        aBundle.putString("category", "android");
        myAndroidFragment.setArguments(aBundle);

        mAndroidTransaction.replace(R.id.frame, myAndroidFragment);
        mAndroidTransaction.commit();
    }

    private void parentingFragment() {
        FetchViewBackground myParentingFragment = new FetchViewBackground();
        android.support.v4.app.FragmentTransaction mParentingTransaction = getSupportFragmentManager().beginTransaction();

        Bundle pBundle = new Bundle();
        pBundle.putString("category", "parenting");
        myParentingFragment.setArguments(pBundle);

        mParentingTransaction.replace(R.id.frame, myParentingFragment);
        mParentingTransaction.commit();
    }

    private void scienceFragment() {
        FetchViewBackground myScienceFragment = new FetchViewBackground();
        android.support.v4.app.FragmentTransaction mScienceTransaction = getSupportFragmentManager().beginTransaction();

        Bundle sBundle = new Bundle();
        sBundle.putString("category", "science");
        myScienceFragment.setArguments(sBundle);

        mScienceTransaction.replace(R.id.frame, myScienceFragment);
        mScienceTransaction.commit();
    }

    private void webFragment() {
        WebView myWebFragment = new WebView();
        android.support.v4.app.FragmentTransaction mWebTransaction = getSupportFragmentManager().beginTransaction();
        mWebTransaction.replace(R.id.frame, myWebFragment);
        mWebTransaction.commit();
    }

    private void homeFragment() {
        FetchViewBackground myBlogFragment = new FetchViewBackground();
        android.support.v4.app.FragmentTransaction mBlogTransaction = getSupportFragmentManager().beginTransaction();

        Bundle hBundle = new Bundle();
        hBundle.putString("category", "blog");
        myBlogFragment.setArguments(hBundle);

        mBlogTransaction.replace(R.id.frame, myBlogFragment);
        mBlogTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class doAuthValidate extends AsyncTask<String, Void, Boolean> {
        HttpURLConnection urlConnection;

        public final String cookieStr;
        public final String charset = "UTF-8";
        public String error;

        doAuthValidate(String cookie) {
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
                    Log.v("The Jones Theory", "doInBg: While Line -" + line);
                }
                result = sResult.toString();
//                result = parseCookie(result);

                JSONObject response = new JSONObject(result);
                String validStr = response.optString("valid");

                if (validStr.equals("false")){
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("AUTH_COOKIE", "");
                    edit.apply();
                    return false;
                } else if (validStr.equals("true")){
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

            } else {
                SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean loginCheck = sharedPerf.getBoolean("prompt_logged_out", false);
                Log.v(TAG, "onPostExecute: LoginCheck =" + Boolean.toString(loginCheck));
                if (loginCheck){
                    showLogin();
                }
            }
        }
    }
}
