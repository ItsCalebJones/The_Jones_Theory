package me.calebjones.thejonestheory;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import me.calebjones.thejonestheory.activity.LoginActivity;
import me.calebjones.thejonestheory.drawer.NavigationDrawerCallbacks;
import me.calebjones.thejonestheory.drawer.NavigationDrawerFragment;
import me.calebjones.thejonestheory.fragments.FetchView;
import me.calebjones.thejonestheory.fragments.WebView;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
        // populate the navigation drawer
        mNavigationDrawerFragment.setUserData("Caleb Jones", "http://calebjones.me", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0: //Home//todo
                Toast.makeText(getApplicationContext(), "Home has been clicked.", Toast.LENGTH_SHORT).show();
                fragment = getFragmentManager().findFragmentByTag(WebView.TAG);
                if (fragment == null) {
                fragment = new WebView();
            }
                break;
            case 1: //About
                Toast.makeText(getApplicationContext(), "About has been clicked.", Toast.LENGTH_SHORT).show();
                fragment = getFragmentManager().findFragmentByTag(FetchView.TAG);
                if (fragment == null) {
                    fragment = new FetchView();
                }
                break;
            case 2: //Gallery//todo
                Toast.makeText(getApplicationContext(), "Gallery has been clicked.", Toast.LENGTH_SHORT).show();
                fragment = getFragmentManager().findFragmentByTag(FetchView.TAG);
                if (fragment == null) {
                    fragment = new FetchView();
                }
                break;
            case 3: //Science //todo
                Toast.makeText(getApplicationContext(), "Science has been clicked.", Toast.LENGTH_SHORT).show();
                fragment = getFragmentManager().findFragmentByTag(FetchView.TAG);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;
            case 4: //Parenting//todo
                Toast.makeText(getApplicationContext(), "Parenting has been clicked.", Toast.LENGTH_SHORT).show();
//                Intent fadingntent = new Intent(getApplicationContext(), *.class);
//                startActivity(intent);
                break;
        }
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

    }


    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
