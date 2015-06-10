package me.calebjones.thejonestheory.activity;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.manuelpeinado.fadingactionbar.view.ObservableScrollable;
import com.manuelpeinado.fadingactionbar.view.OnScrollChangedCallback;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import me.calebjones.thejonestheory.R;


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

        //Get information about the post that was selected from FetchViewBackground
        Intent intent = getIntent();
        PostTitle = intent.getExtras().getString("PostTitle");
        PostImage = intent.getExtras().getString("PostImage");
        PostText = intent.getExtras().getString("PostText");
        PostURL = intent.getExtras().getString("PostURL");

        Log.d("The Jones Theory", "Post ID = " + PostTitle + " PostImage= " + PostImage + " PostText = " + PostText);

        //Remove Styling from Text
        PostText = PostText.replaceAll("<style>.*?</style>", "");

        //Remove Styling from Text
        PostText = PostText.replaceAll("<img[^>]*?>.*?</img[^>]*?>", "");

        Log.d("The Jones Theory", "PostText Updated= " + PostText);



        ImageView imgFavorite = (ImageView)findViewById(R.id.header);
        imgFavorite.setClickable(true);

        //Replace the header image with the Feature Image
        Ion.with(imgFavorite)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .load(PostImage);

        imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView imgFavorite = (ImageView)v;

                BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

                Intent intent = new Intent(PostSelected.this, TransitionFullscreen.class);
                intent.putExtra("bitmapInfo", bi.key);
                intent.putExtra("PostImage", PostImage);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(PostSelected.this, imgFavorite, "photo_hero").toBundle());
            }
        });



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
        getSupportActionBar().setElevation(60);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, PostURL);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}