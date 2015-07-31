package me.calebjones.blogsite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import me.calebjones.blogsite.activity.DownloadActivity;
import me.calebjones.blogsite.activity.LoginActivity;
import me.calebjones.blogsite.activity.SettingsActivity;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.fragments.FeedFragment;
import me.calebjones.blogsite.fragments.PhotoFragment;
import me.calebjones.blogsite.fragments.RandomFragment;
import me.calebjones.blogsite.network.PostDownloader;
import me.calebjones.blogsite.util.AuthValidate;
import me.calebjones.blogsite.util.FBConnect;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private String pURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=20";
    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=30";
    private String[] titles;
    private AuthValidate doAuth = null;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;
    private static final int DOWNLOAD_REQUEST = 1045;
    private TabLayout tabLayout;
    private DatabaseManager databaseManager;
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setUpViews();
        }
    };

    //Public Variables
    public static final String TAG = "The Jones Theory - M";
    public Context context;
    public ProgressBar progressBar;
    public String mCategory;

    int i = 0;
    int mCurCheckPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Check to see if the app is loading for the first time.
        if(SharedPrefs.getInstance().getFirstRun()){
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
            }
            Log.d("The Jones Theory", "DB Size = " + String.valueOf(databaseManager.getCount()));
            if (databaseManager.getCount() == 0){
                doDownload();
            }
        }



        SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
        SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);


        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);



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
                FragmentManager fm = getSupportFragmentManager();
                FeedFragment fragment = (FeedFragment)fm.findFragmentByTag(makeFragmentName(R.id.viewpager, 0));
                titles = getResources().getStringArray(R.array.tabs);

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {

                    case R.id.home:
                        viewPager.setCurrentItem(0);
                        titles[0] = "Feed";
                        fragment.changeFeed("");
                        return true;
                    case R.id.gallery:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.random:
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.blog:
                        viewPager.setCurrentItem(0);
                        titles[0] = "Blog";
                        fragment.changeFeed("blog");
                        return true;
                    case R.id.science:
                        viewPager.setCurrentItem(0);
                        titles[0] = "Science";
                        fragment.changeFeed("science");
                        return true;
                    case R.id.parenting:
                        viewPager.setCurrentItem(0);
                        titles[0] = "Parenting";
                        fragment.changeFeed("parenting");
                        return true;
                    case R.id.android:
                        viewPager.setCurrentItem(0);
                        titles[0] = "Android";
                        fragment.changeFeed("android");
                        return true;
                    case R.id.technology:
                        viewPager.setCurrentItem(0);
                        titles[0] = "Technology";
                        fragment.changeFeed("technology");
                        return true;
                    case R.id.settings:
                        settingsIntent();
                        return true;
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

        setUpViews();
    }

    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

    private void setUpViews() {
        if (viewPager.getAdapter() == null) {
            FragmentsAdapter adapter = new FragmentsAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
        }
        tabLayout.setupWithViewPager(viewPager);
    }

    private void settingsIntent() {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
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
        //Check to see if the app is loading for the first time.
        if(!SharedPrefs.getInstance().getFirstRun()){
            if (databaseManager == null) {
                databaseManager = new DatabaseManager(this);
            }
            Log.d("The Jones Theory", "DB Size onResume = " + String.valueOf(databaseManager.getCount()));
            if (databaseManager.getCount() == 0){
                doDownload();
            }
        }


        validateLoginStatus();

    //  Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        super.onResume();
    }

    private void doDownload() {
        Intent intent = new Intent(this, PostDownloader.class);
        intent.setAction(PostDownloader.DOWNLOAD_ALL);
        startService(intent);
    }

    public void validateLoginStatus() {
        //Get Shared Preference auth key
        SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
        final SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String authCookie = prefs.getString("AUTH_COOKIE", "");

        //Check to see if the app is loading for the first time.
        if(SharedPrefs.getInstance().getFirstRun()){
            showLogin();
        } else if (SharedPrefs.getInstance().isDownloading()){
            showDownload();
        }

        //Gets current FB Access Token
        final AccessToken token = AccessToken.getCurrentAccessToken();

        //This checks to see
        if (token != null) {

            //If FB token is not null then check current WP auth_key for validity.
            doAuth = new AuthValidate(authCookie) {
                @Override
                protected void onPostExecute(Boolean result) {
                    Log.v(TAG, "Main - Token - onPostExecute: " + result);
                    if (result) {
                        //If key is valid then the user is logged in.
                        SharedPrefs.getInstance().setLoginStatus(true);
                        Log.d(TAG, "Main - Token - AuthValidateSuccess: " + status);
                    } else {
                        //If key is not valid try to generate a new auth key and save it.
                        Log.d(TAG, "Main - Token - AuthValidateFailure: " + error);
                        FBConnect fbAuth = new FBConnect(token.getToken()) {
                            @Override
                            protected void onPostExecute(Boolean result) {
                                //Do something with the JSON string
                                Log.v(TAG, "onPostExecute: " + result);

                                if (result) {
                                    //DO THIS
                                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putString("AUTH_COOKIE", cookieStr);
                                    edit.apply();
                                    SharedPrefs.getInstance().setLoginStatus(true);
                                } else {
                                    //DO THAT
                                    SharedPrefs.getInstance().setLoginStatus(false);
                                    LoginManager.getInstance().logOut();
                                    Toast.makeText(getApplicationContext(), "Sorry, something happened please log back in.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                        fbAuth.execute();
                    }
                }
            };
            doAuth.execute();

        } else if (authCookie != null) {

            //This SharedPreference key checks to see if the user wants to be logged in based on the settings.
            final boolean promptLoginCheck = sharedPerf.getBoolean("prompt_logged_out", false);
            Log.v(TAG, "Prompt To login: " + Boolean.toString(promptLoginCheck));

            //Easy way to check if authCookie has been added. Probably a better way to do this.
            if (authCookie.length() > 10) {
                Log.v(TAG, "Cookie: " + authCookie);

                doAuth = new AuthValidate(authCookie) {
                    @Override
                    protected void onPostExecute(Boolean result) {
                        //Do something with the JSON string
                        Log.v(TAG, "Main - authCookie - onPostExecute: " + result);
                        if (result) {
                            //DO THIS
                            SharedPrefs.getInstance().setLoginStatus(true);
                            Log.d(TAG, "Main - authCookie - AuthValidateSuccess - " + status);
                        } else {
                            //DO THAT
                            SharedPrefs.getInstance().setLoginStatus(false);
                            Log.d(TAG, "Main - authCookie - AuthValidateFailure -" + error);
                            if (promptLoginCheck){
                                showLogin();
                            }
                        }
                    }
                };
                doAuth.execute();
            }
        } else if (authCookie == null && token == null) {
            SharedPrefs.getInstance().setLoginStatus(false);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
//        AppEventsLogger.deactivateApp(this);
    }

    private void showLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void showDownload() {
        Intent loginIntent = new Intent(this, DownloadActivity.class);
        startActivity(loginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_main, menu);
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
            settingsIntent();
            return true;
        } else if (id == R.id.action_login){
            showLogin();
            return true;
        } else if (id == R.id.action_search){
            return true;
        } else if (id == R.id.DBTest){
            Intent DBIntent = new Intent(this, DownloadActivity.class);
            startActivity(DBIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public class FragmentsAdapter extends FragmentPagerAdapter {


        private String[] titles = getResources().getStringArray(R.array.tabs);

        public FragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FeedFragment();
                case 1:
                    return new PhotoFragment();
                case 2:
                    return new RandomFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

    }

}
