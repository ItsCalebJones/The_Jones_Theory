package me.calebjones.blogsite.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.transition.Slide;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.comments.CommentItem;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.network.PostLoader;
import me.calebjones.blogsite.models.FeedItem;
import me.calebjones.blogsite.util.URLImageParser;


public class PostSelectedActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private LinearLayout CommentLayout, mText, spacer_layout;
    private Context mContext;
    private FloatingActionButton fullscreenFab;
    private FloatingActionButton commentFab;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private boolean LoginStatus;
    private Button button;

    public String URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/";
    public static final String COMMENT_URL = "http://calebjones.me/api/user/post_comment/?";
    public String PostTitle, PostImage, PostText, PostURL, PostCat;
    public Integer PostID;
    public Bitmap bitmap;
    public List<FeedItem> feedItemList;
    public Palette mPalette;
    public TextView mTextView, CommentBoxTitle, CommentTextLoggedOut, mTitle;
    private SpannableStringBuilder htmlSpannable;
    public View CommentBox;
    public EditText CommentEditText;

    int defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Slide());
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected);
        Bundle bundle = getIntent().getExtras();

        final ImageView imgFavorite = (ImageView)findViewById(R.id.header);
        final View myView = findViewById(R.id.main_content);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            myView.setVisibility(View.INVISIBLE);
        }

        defaultColor = getResources().getColor(R.color.icons);

        if (bundle != null){
            //Get information about the post that was selected from BlogFragment
            Intent intent = getIntent();
            PostTitle = intent.getExtras().getString("PostTitle");
            PostImage = intent.getExtras().getString("PostImage");
            PostText = intent.getExtras().getString("PostText");
            PostURL = intent.getExtras().getString("PostURL");
            PostID = intent.getExtras().getInt("PostID");
            PostCat = intent.getExtras().getString("PostCat");

            Log.i("The Jones Theory", "Intent!");

            this.feedItemList = PostLoader.getWords();
        }
        if (savedInstanceState != null) {
            Log.v("The Jones Theory", "Saved Instance: " + savedInstanceState.getString("PostTitle"));
        }

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);


        collapsingToolbar.setTitle(PostCat.replaceAll(",", " |"));

        fullscreenFab = (FloatingActionButton) findViewById(R.id.postFab);
        fullscreenFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (PostImage != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        try {
                            LollipopTransition(v);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Transition(v);
                    }
                }
            }
        });

        commentFab = (FloatingActionButton) findViewById(R.id.commentFab);
        commentFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent commentIntent = new Intent(PostSelectedActivity.this, PostCommentsActivity.class);
                commentIntent.putExtra("PostID", PostID.toString());
                commentIntent.putExtra("PostURL", PostURL);
                if (mPalette != null) {
                    commentIntent.putExtra("bgcolor", mPalette.getDarkMutedColor(getResources().getColor(R.color.icons)));
                }
                startActivity(commentIntent);
            }
        });

        //Replace the content_header image with the Feature Image
        if (PostImage == null){
            if (fullscreenFab.getVisibility() == View.VISIBLE) {
                fullscreenFab.setVisibility(View.GONE);
            }
            commentFab.setVisibility(View.INVISIBLE);
            if (myView.getVisibility() == View.INVISIBLE) {
                myView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        revealView(myView);
                        if (commentFab.getVisibility() == View.INVISIBLE) {
                            commentFab.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    revealView(commentFab);
                                }
                            }, 500);
                        }
                    }
                }, 100);
            }
        }
        Ion.with(getApplicationContext())
                .load(PostImage)
                .withBitmap()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        imgFavorite.setImageBitmap(result);
                        bitmap = result;
                        if (bitmap != null) {
                            mPalette = Palette.generate(bitmap);
                            final View mainView = findViewById(R.id.main_content);
                            commentFab.setVisibility(View.INVISIBLE);
                            fullscreenFab.setVisibility(View.INVISIBLE);
                            if (mainView.getVisibility() == View.INVISIBLE) {
                                mainView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mApplyPalette(mPalette);
                                        revealView(mainView);
                                        if (commentFab.getVisibility() == View.INVISIBLE) {
                                            revealView(commentFab);
                                        }
                                        if (fullscreenFab.getVisibility() == View.INVISIBLE) {
                                            revealView(fullscreenFab);
                                        }
                                    }
                                }, 100);
                            }
                            Log.d("The Jones Theory", "Result: " + result.toString() + " Bitmap: " + bitmap.toString());
                        }
                    }
                });

        //Removes HTML artifcats

//Skipping this for now trying to get images to load
        PostText = removeStyling(PostText);
        PostTitle = stripHtml(PostTitle);

        mText = (LinearLayout) findViewById(R.id.selected_content);
        CommentLayout = (LinearLayout) findViewById(R.id.CommentLayout);

        //Setup the Title, EditText and Textviews
        mTitle = (TextView) findViewById(R.id.PostTextTitle);
        mTitle.setText(Html.fromHtml(PostTitle));
        mTextView = (TextView) findViewById(R.id.PostTextPara);

        URLImageParser p = new URLImageParser(mTextView, this);
        Spanned htmlSpan = Html.fromHtml(PostText, p, null);
        mTextView.setText(htmlSpan);

//        mTextView.setText(Html.fromHtml(PostText,new URLImageParser(mTextView, this), null));

        CommentTextLoggedOut = (TextView) findViewById(R.id.CommentTextLoggedOut);
        CommentBoxTitle = (TextView) findViewById(R.id.CommentBoxTitle);
        CommentBox = findViewById(R.id.CommentBox);
        CommentEditText = (EditText) findViewById(R.id.CommentEditText);


        //Init the toolbar
        mToolbar = (Toolbar) findViewById(R.id.PostToolbar);
        setSupportActionBar(mToolbar);

        //Setup the Action Bar back button and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(25);


        if (android.os.Build.VERSION.SDK_INT >= 21) {

            getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    getWindow().getEnterTransition().removeListener(this);

                    // load the full version, crossfading from the thumbnail image
                    Ion.with(imgFavorite)
                            .crossfade(true)
                            .deepZoom()
                            .load(PostImage);
                }
            });
        }
    }

    //PalLete bugs here where swatches are empty
    public void mApplyPalette(Palette mPalette){
//        getWindow().setBackgroundDrawable(new ColorDrawable(mPalette.getDarkMutedColor(defaultColor)));

        collapsingToolbar.setContentScrimColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));
        collapsingToolbar.setStatusBarScrimColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));
//        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus);
//        collapsingToolbar.setExpandedTitleColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryLight)));


//        CommentBox.setBackgroundColor(mPalette.getDarkMutedColor(getResources().getColor(R.color.icons)));
//        mText.setBackgroundColor(mPalette.getDarkMutedColor(getResources().getColor(R.color.icons)));

        fullscreenFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getLightMutedColor(getResources().getColor(R.color.myAccentColor))));
        commentFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getDarkVibrantColor(getResources().getColor(R.color.myPrimaryDarkColor))));

//        if (mPalette.getDarkMutedSwatch() != null){
//            mTextView.setTextColor(defaultColor);
//        }
    }
    public void Transition(View v){
        ImageView imgFavorite = (ImageView) findViewById(R.id.header);

        BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

        Intent intent = new Intent(PostSelectedActivity.this, FullscreenActivity.class);
        intent.putExtra("bitmapInfo", bi.key);
        intent.putExtra("PostImage", PostImage);
        intent.putExtra("PostURL", PostURL);
        intent.putExtra("PostTitle", PostTitle);
        intent.putExtra("PostText", PostText);

        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitReveal() {
        final View mainView = findViewById(R.id.main_content);

        // get the center for the clipping circle
        int cx = mainView.getMeasuredWidth() / 2;
        int cy = mainView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = mainView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mainView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mainView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();

    }

    @Override
    public void onBackPressed() {
        exitReveal();
        super.onBackPressed();
    }

    @Override
    public void onResume(){
        final View nestedContent = findViewById(R.id.nested_content);
        final View commentFab = findViewById(R.id.commentFab);
        final View fullscreenFab = findViewById(R.id.postFab);

        LoginStatus = SharedPrefs.getInstance().getLoginStatus();

        if (nestedContent.getVisibility() == View.INVISIBLE) {
            nestedContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    revealView(nestedContent);
                }
            }, 500);
        }
        if (LoginStatus) {
            CommentBox.setVisibility(View.VISIBLE);
            CommentTextLoggedOut.setVisibility(View.GONE);

        } else {
            CommentTextLoggedOut.setVisibility(View.VISIBLE);
            CommentBox.setVisibility(View.GONE);
            commentFab.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private void revealView(View view) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            int cx = (view.getLeft() + view.getRight()) / 2;
            int cy = (view.getTop() + view.getBottom()) / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(view.getWidth(), view.getHeight() / 2);

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            view.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void hideView(View view) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            final View myView = view;

            // get the center for the clipping circle
            int cx = (myView.getLeft() + myView.getRight()) / 2;
            int cy = (myView.getTop() + myView.getBottom()) / 2;

            // get the initial radius for the clipping circle
            int initialRadius = myView.getWidth() / 2;

            // create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void LollipopTransition(View v) throws IOException {
        final View nestedContent = findViewById(R.id.nested_content);
        final View commentFab = findViewById(R.id.commentFab);
        final View fullscreenFab = findViewById(R.id.postFab);

        hideView(nestedContent);


        ImageView imgFavorite = (ImageView) findViewById(R.id.header);

        BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Log.d("The Jones Theory", "byteLength: " + byteArray.length);

        //Compress the Bitmap if its over an arbitrary size that probably could crash at a lower count.
        if (byteArray.length > 524288){
            for (int i = 95; (byteArray.length > 524288 && i >= 20); i = i - 5) {
                stream.reset();
                Log.d("The Jones Theory", "BEFORE byteLength - Compression: " + i + " - " + byteArray.length + " stream " + stream.size());
                bitmap.compress(Bitmap.CompressFormat.JPEG, i, stream);
                byteArray = stream.toByteArray();
                Log.d("The Jones Theory", "AFTER byteLength - Compression: " + i + " - " + byteArray.length + " stream " + stream.size());
            }
        }


        Intent intent = new Intent(PostSelectedActivity.this, TransitionFullscreen.class);
        intent.putExtra("bitmap", byteArray);
        intent.putExtra("PostImage", PostImage);
        intent.putExtra("PostURL", PostURL);
        intent.putExtra("PostTitle", PostTitle);
        intent.putExtra("PostText", PostText);

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(PostSelectedActivity.this, imgFavorite, "photo_hero").toBundle());
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    private String removeStyling(String PostText) {

        //Remove Styling from Text
        PostText = PostText.replaceAll("<style>.*?</style>", "");
        //Remove Styling from Text
        int index = PostText.indexOf("<img");
        PostText = PostText.substring(0, index - 4) + "<br> <br>" + PostText.substring(index, PostText.length());
        Log.d("The Jones Theory", PostText.substring(0, index - 4) + "<br> <br>" + PostText.substring(index, PostText.length()));

        return PostText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_selected, menu);
        Log.d("The Jones Theory", "Menu has been created");
        return true;
    }

    private void setupAdapter() {
        Log.v("The Jones Theory", "setupAdapter - notifyDataChanged");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("The Jones Theory", item + " has been clicked = " + PostURL);

        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                exitReveal();
                NavUtils.navigateUpFromSameTask(this);
                return true;
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
        savedInstanceState.putInt("PostID", PostID);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        PostTitle = savedInstanceState.getString("PostTitle");
        PostText = savedInstanceState.getString("PostText");
        PostImage = savedInstanceState.getString("PostImage");
        PostURL = savedInstanceState.getString("PostURL");
        PostID = savedInstanceState.getInt("PostID");
    }
}