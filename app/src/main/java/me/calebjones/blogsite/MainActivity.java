package me.calebjones.blogsite;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import me.calebjones.blogsite.fragments.FetchViewBackground;
import me.calebjones.blogsite.fragments.WebView;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);


        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Load initial Fragment
        FetchViewBackground myBlogFragment = new FetchViewBackground();
        android.support.v4.app.FragmentTransaction mBlogTransaction = getSupportFragmentManager().beginTransaction();

        Bundle hBundle = new Bundle();
        hBundle.putString("category", "blog");
        myBlogFragment.setArguments(hBundle);

        mBlogTransaction.replace(R.id.frame, myBlogFragment);
        mBlogTransaction.commit();

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){


                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.home:
                        FetchViewBackground myBlogFragment = new FetchViewBackground();
                        android.support.v4.app.FragmentTransaction mBlogTransaction = getSupportFragmentManager().beginTransaction();

                        Bundle hBundle = new Bundle();
                        hBundle.putString("category", "blog");
                        myBlogFragment.setArguments(hBundle);

                        mBlogTransaction.replace(R.id.frame, myBlogFragment);
                        mBlogTransaction.commit();
                        return true;

                    // For rest of the options we just show a toast on click

                    case R.id.about:
                        WebView myWebFragment = new WebView();
                        android.support.v4.app.FragmentTransaction mWebTransaction = getSupportFragmentManager().beginTransaction();
                        mWebTransaction.replace(R.id.frame, myWebFragment);
                        mWebTransaction.commit();
                        return true;
                    case R.id.gallery:

                        return true;
                    case R.id.science:
                        FetchViewBackground myScienceFragment = new FetchViewBackground();
                        android.support.v4.app.FragmentTransaction mScienceTransaction = getSupportFragmentManager().beginTransaction();

                        Bundle sBundle = new Bundle();
                        sBundle.putString("category", "science");
                        myScienceFragment.setArguments(sBundle);

                        mScienceTransaction.replace(R.id.frame, myScienceFragment);
                        mScienceTransaction.commit();
                        return true;
                    case R.id.parenting:
                        FetchViewBackground myParentingFragment = new FetchViewBackground();
                        android.support.v4.app.FragmentTransaction mParentingTransaction = getSupportFragmentManager().beginTransaction();

                        Bundle pBundle = new Bundle();
                        pBundle.putString("category", "parenting");
                        myParentingFragment.setArguments(pBundle);

                        mParentingTransaction.replace(R.id.frame, myParentingFragment);
                        mParentingTransaction.commit();
                        return true;
                    case R.id.android:
                        FetchViewBackground myAndroidFragment = new FetchViewBackground();
                        android.support.v4.app.FragmentTransaction mAndroidTransaction = getSupportFragmentManager().beginTransaction();

                        Bundle aBundle = new Bundle();
                        aBundle.putString("category", "android");
                        myAndroidFragment.setArguments(aBundle);

                        mAndroidTransaction.replace(R.id.frame, myAndroidFragment);
                        mAndroidTransaction.commit();
                        return true;
                    case R.id.technology:
                        FetchViewBackground myTechFragment = new FetchViewBackground();
                        android.support.v4.app.FragmentTransaction mTechTransaction = getSupportFragmentManager().beginTransaction();

                        Bundle tBundle = new Bundle();
                        tBundle.putString("category", "technology");
                        myTechFragment.setArguments(tBundle);

                        mTechTransaction.replace(R.id.frame, myTechFragment);
                        mTechTransaction.commit();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
