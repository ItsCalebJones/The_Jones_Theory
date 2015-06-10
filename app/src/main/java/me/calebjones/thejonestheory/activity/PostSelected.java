package me.calebjones.thejonestheory.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.manuelpeinado.fadingactionbar.view.ObservableScrollable;
import com.manuelpeinado.fadingactionbar.view.OnScrollChangedCallback;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import me.calebjones.thejonestheory.R;


public class PostSelected extends ActionBarActivity implements OnScrollChangedCallback {

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



        //Replace the header image with the Feature Image
        Ion.with((ImageView) findViewById(R.id.header))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .load(PostImage);

        //Setup the Title and Textview
        TextView mTextView = (TextView) findViewById(R.id.PostText);
        mTextView.setText(Html.fromHtml(PostText));
        TextView mTitleView = (TextView) findViewById(R.id.PostTitle);
        mTitleView.setText(PostTitle);

        //Init the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionBarBackgroundDrawable = mToolbar.getBackground();
        setSupportActionBar(mToolbar);

        //Setup the Actionabar backbutton and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(100);


        mStatusBarManager = new SystemBarTintManager(this);
        mStatusBarManager.setStatusBarTintEnabled(true);
        mInitialStatusBarColor = Color.BLACK;
        mFinalStatusBarColor = getResources().getColor(R.color.myPrimaryColor);

        mHeader = findViewById(R.id.header);



        ObservableScrollable scrollView = (ObservableScrollable) findViewById(R.id.scrollview);
        scrollView.setOnScrollChangedCallback(this);

        onScroll(-1, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onScroll(int l, int scrollPosition) {
        int headerHeight = mHeader.getHeight() - mToolbar.getHeight();
        float ratio = 0;
        if (scrollPosition > 0 && headerHeight > 0)
            ratio = (float) Math.min(Math.max(scrollPosition, 0), headerHeight) / headerHeight;

        updateActionBarTransparency(ratio);
        updateStatusBarColor(ratio);
        updateParallaxEffect(scrollPosition);
    }

    private void updateActionBarTransparency(float scrollRatio) {
        int newAlpha = (int) (scrollRatio * 255);
        mActionBarBackgroundDrawable.setAlpha(newAlpha);
        mToolbar.setBackground(mActionBarBackgroundDrawable);
    }

    private void updateStatusBarColor(float scrollRatio) {
        int r = interpolate(Color.red(mInitialStatusBarColor), Color.red(mFinalStatusBarColor), 1 - scrollRatio);
        int g = interpolate(Color.green(mInitialStatusBarColor), Color.green(mFinalStatusBarColor), 1 - scrollRatio);
        int b = interpolate(Color.blue(mInitialStatusBarColor), Color.blue(mFinalStatusBarColor), 1 - scrollRatio);
        mStatusBarManager.setTintColor(Color.rgb(r, g, b));
    }

    private void updateParallaxEffect(int scrollPosition) {
        float damping = 0.5f;
        int dampedScroll = (int) (scrollPosition * damping);
        int offset = mLastDampedScroll - dampedScroll;
        mHeader.offsetTopAndBottom(-offset);

        mLastDampedScroll = dampedScroll;
    }

    private int interpolate(int from, int to, float param) {
        return (int) (from * param + to * (1 - param));
    }
}