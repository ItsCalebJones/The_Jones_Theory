package me.calebjones.blogsite.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.database.DatabaseManager;
import me.calebjones.blogsite.content.database.SharedPrefs;
import me.calebjones.blogsite.content.models.FeedItem;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.network.PostLoader;
import me.calebjones.blogsite.util.customtab.CustomTabActivityHelper;
import me.calebjones.blogsite.util.customtab.WebViewFallback;
import me.calebjones.blogsite.util.images.ImageUtils;
import me.calebjones.blogsite.util.images.URLImageParser;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class DetailActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private LinearLayout CommentLayout, mText, spacer_layout;
    private Context mContext;
    private FloatingActionButton fullscreenFab;
    private FloatingActionButton commentFab;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private boolean LoginStatus;
    private Button button;
    private Posts post;
    private CustomTabActivityHelper customTabActivityHelper;
    private MediaBrowser.ConnectionCallback mConnectionCallback;
    private Bitmap mCloseButtonBitmap;
    private CompositeSubscription mSubscriptions;
    private Intent animateIntent;
    private ImageView imageView;
    private View myView;

    public String URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/";
    public static final String COMMENT_URL = "http://calebjones.me/api/user/post_comment/?";
    public String PostTitle, PostImage, PostText, PostURL, PostCat;
    public Integer PostID;
    public Bitmap bitmap;
    public List<FeedItem> feedItemList;
    public Palette mPalette;
    public TextView mTextView, CommentBoxTitle, CommentTextLoggedOut, mTitle;
    public View CommentBox;
    public EditText CommentEditText;


    int defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseManager databaseManager = new DatabaseManager(this);
        customTabActivityHelper = new CustomTabActivityHelper();
        mSubscriptions = new CompositeSubscription();

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Slide());
        }

        decodeBitmaps();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        Bundle bundle = getIntent().getExtras();

        myView = findViewById(R.id.main_content);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            myView.setVisibility(View.INVISIBLE);
        }

        defaultColor = getResources().getColor(R.color.icons);
        Log.v("The Jones Theory", "OnCreate: ");
        if (bundle != null){
            //Get information about the post that was selected from BlogFragment
            Intent intent = getIntent();
            PostID = intent.getExtras().getInt("PostID");
            this.feedItemList = PostLoader.getWords();
            post = databaseManager.getPostByID(PostID);
            PostCat = post.getCategories();
            PostImage = post.getFeaturedImage();
            PostText = post.getContent();
            PostTitle = post.getTitle();
            PostURL = post.getURL();
        }
        if (savedInstanceState != null) {
            Log.v("The Jones Theory", "Saved Instance: " + savedInstanceState.getString("PostTitle"));
        }

        animateIntent = new Intent(DetailActivity.this, AnimateFullscreenActivity.class);
        animateIntent.putExtra("PostImage", PostImage);
        animateIntent.putExtra("PostURL", PostURL);
        animateIntent.putExtra("PostTitle", PostTitle);
        animateIntent.putExtra("PostText", PostText);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);


        collapsingToolbar.setTitle(PostCat.replaceAll(",", " |"));

        fullscreenFab = (FloatingActionButton) findViewById(R.id.postFab);
        fullscreenFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (PostImage != null) {
                    myView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fabSlideOut();
                        }
                    }, 150);
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
                Intent commentIntent = new Intent(DetailActivity.this, CommentActivity.class);
                commentIntent.putExtra("PostID", PostID.toString());
                commentIntent.putExtra("PostURL", PostURL);
                if (mPalette != null) {
                    commentIntent.putExtra("bgcolor", mPalette.getDarkMutedColor(getResources().getColor(R.color.icons)));
                }
                startActivity(commentIntent);
            }
        });

        fullscreenFab.setTranslationX(fullscreenFab.getWidth() + 250);
        commentFab.setTranslationX(commentFab.getWidth() + 250);

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
                    }
                }, 100);
            }
        }
        setUpFeatureImage();
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

//        mTextView.setText(htmlSpan);
        setTextViewHTML(mTextView, PostText);
        customTabActivityHelper.mayLaunchUrl(Uri.parse(PostURL), null, null);

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
                    ImageView imgFavorite = (ImageView)findViewById(R.id.header);

                    // load the full version, crossfading from the thumbnail image
                    Ion.with(imgFavorite)
                            .crossfade(true)
                            .deepZoom()
                            .load(PostImage);
                }
            });
        }
    }

    private void setUpFeatureImage() {
        final ImageView imgFavorite = (ImageView)findViewById(R.id.header);
        Ion.with(getApplicationContext())
                .load(PostImage)
                .withBitmap()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        final View mainView = findViewById(R.id.main_content);
                        final Bitmap resultBitmap = result;

                        commentFab.setVisibility(View.INVISIBLE);
                        fullscreenFab.setVisibility(View.INVISIBLE);
                        bitmap = result;

                        if (bitmap != null) {
                            mainView.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    imgFavorite.setImageBitmap(resultBitmap);
                                    setUpCompressedBitmap();
                                }
                            }, 0);
                            if (mainView.getVisibility() == View.INVISIBLE) {
                                mainView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPalette = Palette.generate(bitmap);
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

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    private void decodeBitmaps() {
        decodeBitmap(R.drawable.ic_action_arrow_back);
    }

    private void decodeBitmap(final int resource) {
        mSubscriptions.add(ImageUtils.decodeBitmap(this, resource)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("The Jones Theory", "There was a problem decoding the bitmap " + e);
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        if (resource == R.drawable.ic_action_arrow_back) {
                            mCloseButtonBitmap = bitmap;
                        }
                    }
                }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                Context context = getApplicationContext();
                CharSequence text = "Hello toast!" + span.getURL();
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
                if (Patterns.WEB_URL.matcher(span.getURL()).matches()){
                    Log.d("The Jones Theory", span.getURL());
                    openCustomTab(span.getURL());
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    protected void setTextViewHTML(TextView text, String html)
    {
        URLImageParser urlImageParser = new URLImageParser(text, this);
        CharSequence sequence = Html.fromHtml(html, urlImageParser, null);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);

        }

        text.setText(strBuilder);
    }

    //PalLete bugs here where swatches are empty
    public void mApplyPalette(Palette mPalette){
        collapsingToolbar.setContentScrimColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));
        collapsingToolbar.setStatusBarScrimColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));
        fullscreenFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getLightMutedColor(getResources().getColor(R.color.myAccentColor))));
        commentFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getDarkVibrantColor(getResources().getColor(R.color.myPrimaryDarkColor))));

        animateIntent.putExtra("PostBG", mPalette.getDarkMutedColor(getResources().getColor(R.color.myPrimaryDarkColor)));

//        if (mPalette.getDarkMutedSwatch() != null){
//            mTextView.setTextColor(defaultColor);
//        }
    }

    public void Transition(View v){
        ImageView imgFavorite = (ImageView) findViewById(R.id.header);

        BitmapInfo bi = Ion.with(imgFavorite)
                .getBitmapInfo();

        Intent intent = new Intent(DetailActivity.this, FullscreenActivity.class);
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
        fabSlideOut();

        final Handler backHandler = new Handler();
        backHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 400);
    }

    @Override
    public void onPause() {
        fabSlideOut();
        super.onPause();
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
            }, 0);
        }

        //Animate the FAB's loading
        myView.postDelayed(new Runnable() {
            @Override
            public void run() {
                fabSlideIn();
            }
        }, 1000);
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
            int cx = (myView.getLeft() + myView.getRight()) / 2;
            int cy = (myView.getTop() + myView.getBottom()) / 2;


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

    private void setUpCompressedBitmap(){
        Runnable r = new Runnable()
        {
            @Override
            public void run(){
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
                animateIntent.putExtra("bitmap", byteArray);

            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void LollipopTransition(View v) throws IOException {
        Log.d("The Jones Theory", "LollipopTransition...");
        final ImageView imgFavorite = (ImageView) findViewById(R.id.header);

        final Handler intentHandler = new Handler();
        intentHandler.postDelayed(new Runnable() {
            public void run() {
                startActivity(animateIntent, ActivityOptions.makeSceneTransitionAnimation(DetailActivity.this, imgFavorite, "photo_hero").toBundle());
            }
        }, 1000);
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
                fabSlideOut();
                final Handler hideHandler = new Handler();
                hideHandler.postDelayed(new Runnable() {
                    public void run() {
                        exitReveal();
                    }
                }, 400);

                final Handler intentHandler = new Handler();
                intentHandler.postDelayed(new Runnable() {
                    public void run() {
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    }
                }, 700);
                return true;
            case R.id.menuShare:
                shareIntent();
                return true;
            case R.id.openBrowser:
                openCustomTab(PostURL);
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

    private void openCustomTab(String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        intentBuilder.setToolbarColor(mPalette.getVibrantColor(getResources()
                .getColor(R.color.myPrimaryColor, null)));
        intentBuilder.setShowTitle(true);

        PendingIntent actionPendingIntent = createPendingShareIntent(url);
        intentBuilder.setActionButton(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_action_share), "Share", actionPendingIntent);

        intentBuilder.setCloseButtonIcon(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_back_arrow));

        intentBuilder.setStartAnimations(this,
                R.anim.slide_in_right, R.anim.slide_out_left);
        intentBuilder.setExitAnimations(this,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        CustomTabActivityHelper.openCustomTab(
                this, intentBuilder.build(), Uri.parse(url), new WebViewFallback());
    }

    private PendingIntent createPendingShareIntent(String url) {
        Intent actionIntent = new Intent(Intent.ACTION_SEND);
        actionIntent.setType("text/plain");
        actionIntent.putExtra(Intent.EXTRA_TEXT, url);
        return PendingIntent.getActivity(getApplicationContext(), 0, actionIntent, 0);
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

    private void fabSlideOut(){
        fullscreenFab.animate().translationX(fullscreenFab.getWidth() + 250).setInterpolator(new AccelerateInterpolator(1)).start();
        commentFab.animate().translationX(fullscreenFab.getWidth() + 250).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void fabSlideIn(){
        fullscreenFab.animate().translationX(0).setInterpolator(new DecelerateInterpolator(4)).start();
        commentFab.animate().translationX(0).setInterpolator(new DecelerateInterpolator(3)).start();
    }
}