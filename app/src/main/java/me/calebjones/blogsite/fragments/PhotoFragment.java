package me.calebjones.blogsite.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.DatabaseManager;
import me.calebjones.blogsite.gallery.ImageAdapter;
import me.calebjones.blogsite.util.EndlessRecyclerOnScrollListener;

public class PhotoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String TAG = "The Jones Theory - PhG";
    public SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean isListView;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;
    private RecyclerView mRecyclerView;
    private ImageAdapter adapter;
    private DatabaseManager databaseManager;
    private Menu menu;
    private View noPost;
    private SlideInBottomAnimationAdapter animatorAdapter;


    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        this.setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    // TODO implement onRefresh Swiperefreshlayout
    @Override
    public void onRefresh() {
        Log.v(TAG, "Refreshing - onRefresh method.");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getActivity();
        final Activity c = getActivity();

        LayoutInflater lf = getActivity().getLayoutInflater();

        View view = lf.inflate(R.layout.fragment_auto_fit_recycler_view, container, false);
        noPost = view.findViewById(R.id.no_Post);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        if (getResources().getBoolean(R.bool.landscape)) {
            mStaggeredLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mStaggeredLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }

        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);

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
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mStaggeredLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (adapter != null && databaseManager != null) {
                    adapter.addItems(databaseManager.getFeed(adapter.getLastItemNum() - 1));
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_grid_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        isListView = true;

        // uses the view to get the context instead of getActivity().
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO update menu
        inflater.inflate(R.menu.menu_list_gallery, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_toggle) {
            toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (isListView) {
            mStaggeredLayoutManager.setSpanCount(2);
            item.setIcon(R.drawable.ic_view_list_white_18dp);
            item.setTitle("Show as list");
            isListView = false;
        } else {
            mStaggeredLayoutManager.setSpanCount(1);
            item.setIcon(R.drawable.ic_view_dashboard_white_18dp);
            item.setTitle("Show as grid");
            isListView = true;
        }

    }

    @Override
    public void onResume() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager(getActivity());
        }

        if (mRecyclerView.getAdapter() == null) {
            adapter = new ImageAdapter(getActivity());
            adapter.addItems(databaseManager.getFeed(""));
            animatorAdapter = new SlideInBottomAnimationAdapter(adapter);
            animatorAdapter.setDuration(500);
            mRecyclerView.setAdapter(animatorAdapter);
        }
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            noPost.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    public void stopRefresh(){
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void startRefresh(){
        mSwipeRefreshLayout.setRefreshing(true);
    }

    public boolean isRefreshing(){
        boolean refreshing = mSwipeRefreshLayout.isRefreshing();
        return refreshing;
    }

    public boolean swipeNull(){
        if (mSwipeRefreshLayout == null){
            return true;
        }
        return false;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}