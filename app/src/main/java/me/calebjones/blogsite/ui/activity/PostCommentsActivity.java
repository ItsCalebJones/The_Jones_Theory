package me.calebjones.blogsite.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;
import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.comments.CommentAdapter;
import me.calebjones.blogsite.content.comments.CommentItem;
import me.calebjones.blogsite.network.CommentsLoader;

public class PostCommentsActivity extends AppCompatActivity {

    private RecyclerView commentRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CommentAdapter adapter;
    private View CommentLayout;
    private ProgressBar progressView;
    private Button button;
    private SlideInBottomAnimationAdapter animatorAdapter;

    public String URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts/";
    public List<CommentItem> commentItemList;
    public String PostTitle, PostImage, PostText, PostURL, PostID;
    public EditText CommentEditText;
    public PostCommentsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentRecyclerView = (RecyclerView) findViewById(R.id.comment_view);

        //Init the list
        this.commentItemList = CommentsLoader.getWords();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            //Get information about the post that was selected from BlogFragment
            Intent intent = getIntent();
            PostTitle = intent.getExtras().getString("PostTitle");
            PostImage = intent.getExtras().getString("PostImage");
            PostText = intent.getExtras().getString("PostText");
            PostURL = intent.getExtras().getString("PostURL");
            PostID = intent.getExtras().getString("PostID");
            Log.i("The Jones Theory", "Intent!");

        }
        if (savedInstanceState != null) {
            Log.v("The Jones Theory", "Saved Instance: " + savedInstanceState.getString("PostTitle"));
        }

        this.commentItemList.clear();

        //Get the comments!
        new CommentsLoader(){
            @Override
            protected void onPreExecute() {
                commentItemList.clear();
            }

            @Override
            protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
                if (result == 1) {
                    update();
                    Log.d(TAG, "Done!");
                } else Log.e(TAG, "Failed to fetch data!");
            }
        }.execute(URL + PostID + "/replies");

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new CommentAdapter(this, commentItemList);
        animatorAdapter = new SlideInBottomAnimationAdapter(adapter);
        animatorAdapter.setDuration(500);
        commentRecyclerView.setAdapter(animatorAdapter);
//        commentRecyclerView.setAdapter(adapter);

        CommentLayout = findViewById(R.id.CommentLayout);
        progressView = (ProgressBar) findViewById(R.id.comment_progress);

        CommentEditText = (EditText) findViewById(R.id.CommentEditText);
        button = (Button) findViewById(R.id.comment_submit_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                try {
                    postComment();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.regi_toolbar);
        setSupportActionBar(toolbar);

        //Setup the Actionabar backbutton and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            Window window = this.getWindow();
//            window.setStatusBarColor(this.getResources().getColor(R.color.myPrimaryColor));
//        }
    }

    private void update() {
        this.commentItemList = CommentsLoader.getWords();
        commentItemList = CommentsLoader.getWords();

        Log.d("The Jones Theory", "Done! 2x: " + this.commentItemList.size() + " - " + commentItemList.size());
        animatorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        if (CommentLayout.getVisibility() == View.INVISIBLE) {
            CommentLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    revealView(CommentLayout);
                }
            }, 500);
        }
        super.onResume();
    }

    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            CommentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            CommentLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    CommentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            CommentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comments, menu);
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
        } else if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void revealView(View mainView) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
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
        } else {
            mainView.setVisibility(View.VISIBLE);
        }
    }
    //Need to move this off main thread
    private void postComment() throws IOException {
        SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
        String authCookie = prefs.getString("AUTH_COOKIE", "");
        String text = CommentEditText.getText().toString();
        new postComment().execute(authCookie, text);
    }
    public class postComment extends AsyncTask<String, Void, String> {

        private String COMMENT_URL = "http://calebjones.me/api/user/post_comment/";

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected String doInBackground(String... params){
            //Send response
            RequestBody formBody = new FormEncodingBuilder()
                    .add("cookie", params[0])
                    .add("post_id", PostID)
                    .addEncoded("content", params[1])
                    .add("comment_status", "1")
                    .build();

            Request todayReq = new Request.Builder().url(COMMENT_URL).post(formBody).build();
            Response response = null;
            try {
                response = BlogsiteApplication.getInstance().client.newCall(todayReq).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String result = null;

            if (response.isSuccessful()){
                result = "Success!";
            }
            else {
                try {
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d("The Jones Theory", result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            /* Download complete. Lets update UI */
            showProgress(false);
            Log.d("The Jones Theory", "Result: " + result);
            if (!result.equals("Success!")){

                CommentEditText.requestFocus();
                CommentEditText.setError("Error, try again. Please make sure you are logged in.");
            } else{


                //Get the comments!
                new CommentsLoader(){
                    @Override
                    protected void onPreExecute() {
                        commentItemList.clear();
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
                        if (result == 1) {
                            update();
                            Log.d(TAG, "Done!");
                        } else Log.e(TAG, "Failed to fetch data!");
                    }
                }.execute(URL + PostID + "/replies");
            }

        }
    }
}
