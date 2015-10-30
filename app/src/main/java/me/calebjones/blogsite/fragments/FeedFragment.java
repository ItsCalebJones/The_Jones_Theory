package me.calebjones.blogsite.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.activity.PostSelectedActivity;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.network.PostDownloader;
import me.calebjones.blogsite.models.FeedItem;
import me.calebjones.blogsite.models.Posts;
import me.calebjones.blogsite.util.EndlessRecyclerOnScrollListener;

public class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "The Jones Theory";
    public String mCategory = null;
    public String category;
    public List<Posts> feedItemList;
    public View mainView;
    public View view;

    private SlideInBottomAnimationAdapter animatorAdapter;
    private View noPost;
    private DatabaseManager databaseManager;
    private RecyclerView mRecyclerView;
    private FeedAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private static final String BUNDLE_RECYCLER_LAYOUT = "BlogFragment.mRecylcerView.fragment_fetch_data";

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PostDownloader.DOWNLOAD_FAIL:
                    stopRefreshing();
                    Toast.makeText(getActivity(), "Error!", Toast.LENGTH_LONG).show();
                    break;
                case PostDownloader.DOWNLOAD_SUCCESS:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("The Jones Theory", "Adapter Size" + String.valueOf(adapter.getItemCount()));
                            adapter.removeAll();
                            Log.d("The Jones Theory", "FeedFragment: Broadcast " + mCategory);
                            adapter.addItems(databaseManager.getFeed(mCategory));
                            animatorAdapter.notifyDataSetChanged();
                            Log.d("The Jones Theory", "Adapter Size" + String.valueOf(adapter.getItemCount()));
                            stopRefreshing();
                        }
                    }, 2000);

                    break;
            }
        }
    };

    public SwipeRefreshLayout mSwipeRefreshLayout;

    public FeedFragment() {
        // Required empty public constructor
        feedItemList = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCategory = SharedPrefs.getInstance().getCategory();
        Log.d("The Jones Theory", "FeedFragment: -onCreate " + mCategory);

//        refreshPost();

        setHasOptionsMenu(true);

        this.setRetainInstance(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity c = getActivity();
        super.onCreateView(inflater, container, savedInstanceState);
        LayoutInflater lf = getActivity().getLayoutInflater();

        view = lf.inflate(R.layout.fragment_fetch_data, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        if (getResources().getBoolean(R.bool.landscape)) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int topRowVerticalPostion = (mRecyclerView == null || mRecyclerView.getChildCount() == 0) ? 0 : mRecyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(dx == 0 && topRowVerticalPostion >= 0);
            }
        });
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (adapter != null && databaseManager != null) {
                    Log.d("The Jones Theory", "FeedFragment: -onLoadMore" + mCategory);
                    if ((Objects.equals(mCategory, "home")) || (Objects.equals(mCategory, ""))) {
                        adapter.addItems(databaseManager.getFeed(adapter.getLastItemNum() - 1));
                    }
                }
            }
        });

        /*Set up Pull to refresh*/
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.myPrimaryColor,
                R.color.myTextPrimaryColor,
                R.color.myAccentColor);

        noPost = view.findViewById(R.id.no_Post);

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fetch_data, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (databaseManager == null) {
            databaseManager = new DatabaseManager(getActivity());
        }

        if (mRecyclerView.getAdapter() == null) {
            adapter = new FeedAdapter();
            Log.d("The Jones Theory", "FeedFragment: -onResume " + mCategory);
            adapter.addItems(databaseManager.getFeed(mCategory));
            animatorAdapter = new SlideInBottomAnimationAdapter(adapter);
            animatorAdapter.setDuration(350);
            mRecyclerView.setAdapter(animatorAdapter);
            noPost.setVisibility(View.GONE);
        }
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            noPost.setVisibility(View.VISIBLE);
        }
        revealView();
    }

    public void changeFeed(String category){
        mCategory = category;
        Log.d("The Jones Theory", "FeedFragment: -changeFeed " + mCategory);
        setRefreshing();

        Random r = new Random();
        int delay = 200 + r.nextInt(600 - 200);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.removeAll();
                adapter.addItems(databaseManager.getFeed(mCategory));
                animatorAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(0);
                stopRefreshing();
            }
        }, delay);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealView() {
        final View mainView = getView().findViewById(R.id.fragment_feed_content);
        if (mainView.getVisibility() == View.INVISIBLE){
            int cx = (mainView.getLeft() + mainView.getRight()) / 2;
            int cy = (mainView.getTop() + mainView.getBottom()) / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(mainView.getWidth(), mainView.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(mainView, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            mainView.setVisibility(View.VISIBLE);

            anim.start();
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void exitReveal() {
        final View tMainView = getView().findViewById(R.id.fragment_feed_content);

        // get the center for the clipping circle
        int cx = tMainView.getMeasuredWidth() / 2;
        int cy = tMainView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = tMainView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(tMainView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                tMainView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();

    }


    @Override
    public void onRefresh() {
        setRefreshing();
        if (mRecyclerView.getAdapter() == null) {
            adapter = new FeedAdapter();
            Log.d("The Jones Theory", "FeedFragment: -onRefresh " + mCategory);
            adapter.addItems(databaseManager.getFeed(mCategory));
            animatorAdapter = new SlideInBottomAnimationAdapter(adapter);
            animatorAdapter.setDuration(1000);
            mRecyclerView.setAdapter(animatorAdapter);
        }

        if (databaseManager.getCount() > 0){
            refreshPost();
        } else {
            Log.d("The Jones Theory", "Feed - onRefresh - Doing the download thing.");

            Intent intent = new Intent(getActivity(), PostDownloader.class);
            intent.setAction(PostDownloader.DOWNLOAD_ALL);
            getActivity().startService(intent);
        }
        Log.v(TAG, "Refreshing - onRefresh method.");
    }

    private void refreshPost() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PostDownloader.DOWNLOAD_SUCCESS);
        intentFilter.addAction(PostDownloader.DOWNLOAD_FAIL);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(getActivity(), PostDownloader.class);
        intent.setAction(PostDownloader.DOWNLOAD_MISSING);
        getActivity().startService(intent);
    }

    public void setRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    public void stopRefreshing() {
        if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

        private List<Posts> feedItemList;
        public int position;

        public FeedAdapter() {

        }

        public void addItems(List<Posts> feedItemList) {
            if (this.feedItemList == null) {
                this.feedItemList = feedItemList;
            } else {
                this.feedItemList.addAll(feedItemList);
            }
        }

        public void removeAll(){
            this.feedItemList.clear();
        }


        public int getLastItemNum() {
            int counter = feedItemList.size() - 1;
            int number = 999;
            int id;
            for (int i = 0; i < counter; i++){
                id = feedItemList.get(i).getID();
                if (id < number){
                    number = id;
                }
            }
            return number;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                             int i) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.list_item, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int i) {
            Posts feedItem = feedItemList.get(i);

            position = i;

            Ion.with(holder.thumbnail)
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .error(R.drawable.placeholder)
                    .load(feedItem.getFeaturedImage());

            holder.title.setText(Html.fromHtml(feedItem.getTitle()));
            holder.excerpt.setText(Html.fromHtml(feedItem.getExcerpt()));
            holder.tags.setText("Tags: " + Html.fromHtml(feedItem.getTags()));
            holder.category.setText("Category: " + Html.fromHtml(feedItem.getCategories()));
        }

        @Override
        public int getItemCount() {
            return feedItemList.size();
        }


        //Class that binds data to the views and registers the clicks.
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

            //Add content to the card
            public ViewHolder(View view) {
                super(view);

                thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
                title = (TextView) view.findViewById(R.id.title);
                excerpt = (TextView) view.findViewById(R.id.excerpt);
                tags = (TextView) view.findViewById(R.id.tags);
                category = (TextView) view.findViewById(R.id.category);

                thumbnail.setOnClickListener(this);
                title.setOnClickListener(this);
            }

            //React to click events.
            @Override
            public void onClick(View v) {
                final int position = getAdapterPosition();
                exitReveal();
                switch (v.getId()) {
                    case R.id.title:
                        Log.d("The Jones Theory", "Title!");
                        break;
                    case R.id.thumbnail:
                        Intent intent = new Intent(getActivity(), PostSelectedActivity.class);
                        intent.putExtra("PostTitle", feedItemList.get(position).getTitle());
                        intent.putExtra("PostImage", feedItemList.get(position).getFeaturedImage());
                        intent.putExtra("PostText", feedItemList.get(position).getContent());
                        intent.putExtra("PostURL", feedItemList.get(position).getURL());
                        intent.putExtra("PostID", feedItemList.get(position).getPostID());
                        intent.putExtra("ID", feedItemList.get(position).getID());
                        startActivity(intent);
                        Log.d("The Jones Theory", "Thumbnail!");
                        break;
                }
            }
        }
    }
}
