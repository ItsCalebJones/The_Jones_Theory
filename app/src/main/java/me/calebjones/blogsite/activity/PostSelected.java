package me.calebjones.blogsite.activity;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import me.calebjones.blogsite.R;
import uk.co.senab.photoview.PhotoView;


public class PostSelected extends AppCompatActivity{

    private Toolbar mToolbar;
    private Drawable mActionBarBackgroundDrawable;
    private View mHeader;
    private int mLastDampedScroll;
    private int mInitialStatusBarColor;
    private int mFinalStatusBarColor;
    private SystemBarTintManager mStatusBarManager;

    public String PostTitle;
    public String PostImage;
    public String PostText;
    public String PostURL;



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_selected);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            //Get information about the post that was selected from FetchViewBackground
            Intent intent = getIntent();
            PostTitle = intent.getExtras().getString("PostTitle");
            PostImage = intent.getExtras().getString("PostImage");
            PostText = intent.getExtras().getString("PostText");
            PostURL = intent.getExtras().getString("PostURL");
            Log.i("The Jones Theory", "Intent!");
        }
        if (savedInstanceState != null) {
            Log.v("The Jones Theory", savedInstanceState.getString("PostTitle"));
        }

        PhotoView imgFavorite = (PhotoView)findViewById(R.id.header);
        imgFavorite.setZoomable(false);
        imgFavorite.setClickable(true);

        //Replace the header image with the Feature Image
        Ion.with(imgFavorite)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .load(PostImage);

        imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhotoView imgFavorite = (PhotoView)v;

                BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

                Intent intent = new Intent(PostSelected.this, TransitionFullscreen.class);
                intent.putExtra("bitmapInfo", bi.key);
                intent.putExtra("PostImage", PostImage);
                intent.putExtra("PostURL", PostURL);
                intent.putExtra("PostTitle", PostTitle);
                intent.putExtra("PostText", PostText);

                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(PostSelected.this, imgFavorite, "photo_hero").toBundle());
            }
        });


        //Removes HTML artifcats
        PostText = removeStyling(PostText);


        //Setup the Title and Textview
        TextView mTextView = (TextView) findViewById(R.id.PostText);
        mTextView.setText(Html.fromHtml(PostText));
        TextView mTitleView = (TextView) findViewById(R.id.PostTitle);
        mTitleView.setText(Html.fromHtml(PostTitle));

        //Init the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mActionBarBackgroundDrawable = mToolbar.getBackground();
        setSupportActionBar(mToolbar);

        //Setup the Actionabar backbutton and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(25);

    }

    private String removeStyling(String PostText) {

        //Remove Styling from Text
        PostText = PostText.replaceAll("<style>.*?</style>", "");
        //Remove Styling from Text
        PostText = PostText.replaceAll("<img.+/(img)*>", "");

        return PostText;
    }

    private void showImgDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);


        dialog.show();
        //Replace the header image with the Feature Image
//        Ion.with((ImageView) findViewById(R.id.image))
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.placeholder)
//                .load(PostImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_post_selected, menu);
        Log.d("The Jones Theory", "Menu has been created");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("The Jones Theory", "Item has been clicked = " + PostURL);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuShare:
                shareIntent();
                return true;
            case R.id.openBrowser:
                browserIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void browserIntent() {
        String url = PostURL;
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse(url));
        startActivity(sendIntent);
    }

    public void shareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, PostURL);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        // Save the user's current game state
        savedInstanceState.putString("PostTitle", PostTitle);
        savedInstanceState.putString("PostImage", PostImage);
        savedInstanceState.putString("PostText", PostText);
        savedInstanceState.putString("PostURL", PostURL);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        PostTitle = savedInstanceState.getString("PostTitle");
        PostText = savedInstanceState.getString("PostText");
        PostImage = savedInstanceState.getString("PostImage");
        PostURL = savedInstanceState.getString("PostURL");
    }

}