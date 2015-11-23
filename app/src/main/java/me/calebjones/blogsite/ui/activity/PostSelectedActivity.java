package me.calebjones.blogsite.ui.activity;

import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
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
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

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
import me.calebjones.blogsite.util.images.ImageUtils;
import me.calebjones.blogsite.util.images.URLImageParser;
import me.calebjones.blogsite.util.customtab.WebViewFallback;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class PostSelectedActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private LinearLayout CommentLayout, mText, spacer_layout;
    private Context mContext;
    private FloatingActionButton fullscreenFab,commentFab;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private boolean LoginStatus;
    private Button button;
    private Posts post;
    private CustomTabActivityHelper customTabActivityHelper;
    private MediaBrowser.ConnectionCallback mConnectionCallback;
    private Bitmap mCloseButtonBitmap;
    private CompositeSubscription mSubscriptions;
    private Intent intent;
    private ImageView imageView;

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
    public byte[] byteArray;


    int defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(new Fade());
        }
        DatabaseManager databaseManager = new DatabaseManager(this);
        customTabActivityHelper = new CustomTabActivityHelper();
        mSubscriptions = new CompositeSubscription();

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Slide());
        }

        decodeBitmaps();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected);
        Bundle bundle = getIntent().getExtras();

        final ImageView imgFavorite = (ImageView)findViewById(R.id.image);
        final View myView = findViewById(R.id.main_content);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            myView.setVisibility(View.INVISIBLE);
        }

        defaultColor = getResources().getColor(R.color.icons);

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

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);


        collapsingToolbar.setTitle(PostCat.replaceAll(",", " |"));

        fullscreenFab = (FloatingActionButton) findViewById(R.id.postFab);
        fullscreenFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (PostImage != null) {
                    try {
                        LollipopTransition(v);
                    } catch (IOException e) {
                        e.printStackTrace();
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
                            setUpCompressedBitmap();
                            mPalette = Palette.generate(bitmap);
                            final View mainView = findViewById(R.id.main_content);
                            commentFab.setVisibility(View.INVISIBLE);
                            fullscreenFab.setVisibility(View.INVISIBLE);
                            if (mainView.getVisibility() == View.INVISIBLE) {
                                mainView.setBackgroundColor(Color.WHITE);
                                mApplyPalette(mPalette);
                                mainView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        revealView(mainView);
                                        revealView(commentFab);
                                        revealView(fullscreenFab);
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
    }


    private void setUpCompressedBitmap() {
        //Store bitmap off the UI
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                //Convert to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();

                Log.d("The Jones Theory", "byteLength: " + byteArray.length);

                //Compress the Bitmap if its over an arbitrary size that probably could crash at a lower count.
                if (byteArray.length > 500000){
                    for (int i = 95; (byteArray.length > 500000 && i >= 20); i = i - 5) {
                        stream.reset();
                        Log.d("The Jones Theory", "BEFORE byteLength - Compression: " + i + " - " + byteArray.length + " stream " + stream.size());
                        bitmap.compress(Bitmap.CompressFormat.JPEG, i, stream);
                        byteArray = stream.toByteArray();
                        Log.d("The Jones Theory", "AFTER byteLength - Compression: " + i + " - " + byteArray.length + " stream " + stream.size());
                    }
                }
                intent = new Intent(PostSelectedActivity.this, FullscreenActivity.class)
                        .putExtra("bitmap", byteArray)
                        .putExtra("PostImage", PostImage)
                        .putExtra("PostURL", PostURL)
                        .putExtra("PostTitle", PostTitle)
                        .putExtra("PostText", PostText);
            }
        };
        Thread t = new Thread(r);
        t.start();
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

        fullscreenFab.setRippleColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));
        commentFab.setRippleColor(mPalette.getVibrantColor(getResources().getColor(R.color.myPrimaryColor)));

        fullscreenFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getLightMutedColor(getResources().getColor(R.color.myAccentColor))));
        commentFab.setBackgroundTintList(ColorStateList.valueOf(mPalette.getDarkVibrantColor(getResources().getColor(R.color.myPrimaryDarkColor))));

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onResume(){
        final View nestedContent = findViewById(R.id.nested_content);
        final View mainView = findViewById(R.id.main_content);

        LoginStatus = SharedPrefs.getInstance().getLoginStatus();

        if (nestedContent.getVisibility() == View.INVISIBLE) {
            mainView.setBackgroundColor(Color.WHITE);
            nestedContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    revealView(nestedContent);
                }
            }, 150);
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

<<<<<<< HEAD
    public void LollipopTransition(View v) throws IOException {
        Log.d("The Jones Theory", "Sending Intent...");
        startActivity(intent);

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
                NavUtils.navigateUpFromSameTask(this);
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
                .getColor(R.color.myPrimaryColor)));
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
}