package me.calebjones.blogsite.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.comments.CommentAdapter;
import me.calebjones.blogsite.comments.CommentItem;
import me.calebjones.blogsite.models.FeedItem;
import me.calebjones.blogsite.loader.CommentsLoader;
import me.calebjones.blogsite.loader.PostLoader;


public class PostSelected extends AppCompatActivity {

    private Toolbar mToolbar;
    private Drawable mActionBarBackgroundDrawable;
    private View mHeader;
    private int mLastDampedScroll;
    private int mInitialStatusBarColor;
    private int mFinalStatusBarColor;

    public String tURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/";
    public String commentURL;
    public String related = "/related";
    public String PostTitle;
    public String PostImage;
    public String PostText;
    public String PostURL;
    public Integer PostID;
    public Bitmap bitmap;
    private LinearLayout mTitle;
    private LinearLayout mText;
    private Context mContext;
    public List<FeedItem> feedItemList;
    public List<CommentItem> commentItemList;
    public Palette mPalette;
    public TextView mTextView;
    public TextView mTitleView;

    private FloatingActionButton fullscreenFab;
    private FloatingActionButton commentFab;
    private CollapsingToolbarLayout collapsingToolbar;
    private RecyclerView commentRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CommentAdapter adapter;

    int defaultColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Slide());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_selected_para);
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
            Log.i("The Jones Theory", "Intent!");

            this.commentItemList = CommentsLoader.getWords();
            this.feedItemList = PostLoader.getWords();
        }
        if (savedInstanceState != null) {
            Log.v("The Jones Theory", "Saved Instance: " + savedInstanceState.getString("PostTitle"));
        }
        //Replace the header image with the Feature Image
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
                        mPalette = Palette.generate(bitmap);
                        final View mainView = findViewById(R.id.main_content);
                        if (mainView.getVisibility() == View.INVISIBLE) {
                            mainView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mApplyPalette(mPalette);
                                    revealView(mainView);
                                    if (commentFab.getVisibility() == View.INVISIBLE) {
                                        commentFab.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                revealView(commentFab);
                                            }
                                        }, 250);
                                    }
                                    if (fullscreenFab.getVisibility() == View.INVISIBLE) {
                                        fullscreenFab.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                revealView(fullscreenFab);
                                            }
                                        }, 500);
                                    }
                                }
                            }, 100);
                        }
                        Log.d("The Jones Theory", "Result: " + result.toString() + " Bitmap: " + bitmap.toString());
                    }
                });

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        fullscreenFab = (FloatingActionButton) findViewById(R.id.postFab);
        fullscreenFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= 21){
                    try {
                        LollipopTransition(v);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Transition(v);
                }
            }
        });

        commentFab = (FloatingActionButton) findViewById(R.id.commentFab);
        commentFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent commentIntent = new Intent(PostSelected.this, PostComments.class);
                startActivity(commentIntent);

            }
        });

//        this.commentItemList.clear();
//        commentURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/" + PostID + "/replies/";
//        Log.d("The Jones Theory", commentURL);
//        new CommentsLoader(){
//            @Override
//            protected void onPostExecute(Integer result) {
//            /* Download complete. Lets update UI */
//                if (result == 1) {
//                    Log.d(TAG, "Succeeded fetching data! - POST LOADER");
//                } else Log.e(TAG, "Failed to fetch data!");
//            }
//        }.execute(commentURL);

        //Removes HTML artifcats
        PostText = removeStyling(PostText);
        PostTitle = stripHtml(PostTitle);

//        mTitle = (LinearLayout) findViewById(R.id.content_title);
        mText = (LinearLayout) findViewById(R.id.selected_content);

        //Setup the Title and Textview
        mTextView = (TextView) findViewById(R.id.PostTextPara);
        mTextView.setText(Html.fromHtml(PostText));


        //Init the toolbar
        mToolbar = (Toolbar) findViewById(R.id.PostToolbar);
        setSupportActionBar(mToolbar);

        //Setup the Actionabar backbutton and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(25);

        collapsingToolbar.setTitle(PostTitle);

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

    //Pallete bugs here where swatches are empty
    public void mApplyPalette(Palette mPalette){
        getWindow().setBackgroundDrawable(new ColorDrawable(mPalette.getDarkMutedColor(defaultColor)));

        collapsingToolbar.setContentScrimColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));
        collapsingToolbar.setStatusBarScrimColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));

        mText.setBackgroundColor(mPalette.getDarkMutedColor(getResources().getColor(R.color.icons)));

        fullscreenFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getVibrantColor(getResources().getColor(R.color.myAccentColor))));
        commentFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getDarkVibrantColor(getResources().getColor(R.color.myPrimaryDarkColor))));

        if (mPalette.getDarkMutedSwatch() != null){
            mTextView.setTextColor(defaultColor);
        }
    }
    public void Transition(View v){
        ImageView imgFavorite = (ImageView) findViewById(R.id.header);

        BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Intent intent = new Intent(PostSelected.this, Fullscreen.class);
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


        if (nestedContent.getVisibility() == View.INVISIBLE) {
            nestedContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    revealView(nestedContent);
                }
            }, 500);
        }
        if (commentFab.getVisibility() == View.INVISIBLE) {
            commentFab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    revealView(commentFab);
                }
            }, 750);
        }
        if (fullscreenFab.getVisibility() == View.INVISIBLE) {
            fullscreenFab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    revealView(fullscreenFab);
                }
            }, 1000);
        }
        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealView(View mainView) {
        int cx = (mainView.getLeft() + mainView.getRight()) / 2;
        int cy = (mainView.getTop() + mainView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(mainView.getWidth(), mainView.getHeight() / 2);

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mainView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        mainView.setVisibility(View.VISIBLE);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void hideView(View view) {
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
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void LollipopTransition(View v) throws IOException {
        final View nestedContent = findViewById(R.id.nested_content);
        final View commentFab = findViewById(R.id.commentFab);
        final View fullscreenFab = findViewById(R.id.postFab);

        hideView(nestedContent);
        hideView(commentFab);
        hideView(fullscreenFab);

        commentFab.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideView(commentFab);
            }
        }, 250);

        fullscreenFab.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideView(fullscreenFab);
            }
        }, 500);

        ImageView imgFavorite = (ImageView) findViewById(R.id.header);

        BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Log.d("The Jones Theory", "byteLength: " + byteArray.length);


        if (byteArray.length > 1000000){
            for (int i = 85; (byteArray.length > 1000000 && i >= 20); i = i - 5) {
                stream.reset();
                Log.d("The Jones Theory", "BEFORE byteLength - Compression: " + i + " - " + byteArray.length + " stream " + stream.size());
                bitmap.compress(Bitmap.CompressFormat.JPEG, i, stream);
                byteArray = stream.toByteArray();
                Log.d("The Jones Theory", "AFTER byteLength - Compression: " + i + " - " + byteArray.length + " stream " + stream.size());
            }
        }


        Intent intent = new Intent(PostSelected.this, TransitionFullscreen.class);
        intent.putExtra("bitmap", byteArray);
        intent.putExtra("PostImage", PostImage);
        intent.putExtra("PostURL", PostURL);
        intent.putExtra("PostTitle", PostTitle);
        intent.putExtra("PostText", PostText);

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(PostSelected.this, imgFavorite, "photo_hero").toBundle());
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    private String removeStyling(String PostText) {

        //Remove Styling from Text
        PostText = PostText.replaceAll("<style>.*?</style>", "");
        //Remove Styling from Text
        PostText = PostText.replaceAll("<img.+/(img)*>", "");

        return PostText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_post_selected, menu);
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