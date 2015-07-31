package me.calebjones.blogsite.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.List;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.activity.PostSelectedActivity;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.models.FeedItem;
import me.calebjones.blogsite.models.Posts;
import me.calebjones.blogsite.util.BlipUtils;

public class RandomFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public static final String TAG = "The Jones Theory";
    public String mCategory = null;
    public List<Posts> feedItemList;

    private DatabaseManager databaseManager;
    private RecyclerView mRecyclerView;

    public ImageView thumbnail;
    public TextView title;
    public TextView content;
    public TextView excerpt;
    public TextView category;
    public TextView tags;
    public FeedItem feed;
    public TextView ID;
    public Integer mPosition;
    private Context context;
    private Posts post;
    private View noPost;
    private View item;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        this.setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity c = getActivity();

        LayoutInflater lf = getActivity().getLayoutInflater();
        View view = lf.inflate(R.layout.fragment_random, container, false);

        thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        title = (TextView) view.findViewById(R.id.title);
        excerpt = (TextView) view.findViewById(R.id.excerpt);
        tags = (TextView) view.findViewById(R.id.tags);

        noPost = view.findViewById(R.id.no_Post);
        item = view.findViewById(R.id.Random_posts);

        excerpt.setOnClickListener(this);
        thumbnail.setOnClickListener(this);
        title.setOnClickListener(this);

        databaseManager = new DatabaseManager(getActivity());


        int num = 0;
        if (savedInstanceState != null)
            num = savedInstanceState.getInt("NUM");

        if (databaseManager.getCount() != 0) {
            loadPost(num);
        } else {
            item.setVisibility(View.INVISIBLE);
            noPost.setVisibility(View.VISIBLE);
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void loadPost(int num) {
        int random;
        int max = databaseManager.getMax();

        if (databaseManager.getCount() != 0){
            if (max != 0 && num == 0){
                random = BlipUtils.randInt(1, max);
                post = databaseManager.getPost(random);
            } else {
                random = num;
                post = databaseManager.getPost(random);
            }
        }

        title.setText(Html.fromHtml(post.getTitle()));
        excerpt.setText(Html.fromHtml(post.getExcerpt()));
        tags.setText(Html.fromHtml(post.getTags()));

        Ion.with(thumbnail)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .error(R.drawable.placeholder)
                .load(post.getFeaturedImage());

    }

    //React to click events.
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), PostSelectedActivity.class);
        intent.putExtra("PostTitle", post.getTitle());
        intent.putExtra("PostImage", post.getFeaturedImage());
        intent.putExtra("PostText", post.getContent());
        intent.putExtra("PostURL", post.getURL());
        intent.putExtra("PostID", post.getPostID());
        intent.putExtra("ID", post.getID());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO update menu
        inflater.inflate(R.menu.menu_fetch_data, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menuRefresh) {
            onRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        loadPost(0);
    }


}
