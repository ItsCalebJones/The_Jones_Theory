package me.calebjones.blogsite.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.comments.CommentAdapter;
import me.calebjones.blogsite.comments.CommentItem;
import me.calebjones.blogsite.loader.CommentsLoader;

public class PostComments extends AppCompatActivity {

    private RecyclerView commentRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CommentAdapter adapter;
    public List<CommentItem> commentItemList;

    public PostComments(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);

        commentRecyclerView = (RecyclerView) findViewById(R.id.comment_view);

        this.commentItemList = CommentsLoader.getWords();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new CommentAdapter(this, commentItemList);
        commentRecyclerView.setAdapter(adapter);

        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.regi_toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_comments, menu);
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
