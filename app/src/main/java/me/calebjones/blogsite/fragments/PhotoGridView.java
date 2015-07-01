package me.calebjones.blogsite.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.util.List;

import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.activity.PostSelected;
import me.calebjones.blogsite.activity.TransitionFullscreen;
import me.calebjones.blogsite.gallery.ImageAdapter;
import me.calebjones.blogsite.gallery.ImageItem;
import me.calebjones.blogsite.loader.PhotoLoader;
import me.calebjones.blogsite.util.MarginDecoration;
import me.calebjones.blogsite.util.RecyclerItemClickListener;
import uk.co.senab.photoview.PhotoView;

public class PhotoGridView extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=25";
    public List<ImageItem> imageList;
    public static final String TAG = "The Jones Theory - PhG";

    public SwipeRefreshLayout mSwipeRefreshLayout;

    private GridView mGridview;
    private RecyclerView mRecyclerView;
    private ImageAdapter adapter;
    private static final String BUNDLE_RECYCLER_LAYOUT = "PhotoGridView.mRecylcerView.fragment_auto_fit_recycler_view.xml";
    private int i;

    // HOLD THE URL TO MAKE THE API CALL TO
    private String URL;

    // STORE THE PAGING URL
    private String pagingURL;

    // FLAG FOR CURRENT PAGE
    int current_page = 1;

    // BOOLEAN TO CHECK IF NEW FEEDS ARE LOADING
    Boolean loadingMore = true;

    Boolean stopLoadingData = false;


    public PhotoGridView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    // TODO implement onRefresh Swiperefreshlayout
    @Override
    public void onRefresh() {
        startRefresh();
        new PhotoLoader(){
            @Override
            protected void onPostExecute(Integer result) {
                    /* Download complete. Lets update UI */
                if (result == 1) {
                    if (isRefreshing()){
                        stopRefresh();
                    }
                    Log.d(TAG, "Succeeded fetching data! - POST LOADER");
                } else Log.e(TAG, "Failed to fetch data!");
            }
        }.execute(mURL);
        Log.v(TAG, "Refreshing - onRefresh method.");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getActivity();
        final Activity c = getActivity();

        LayoutInflater lf = getActivity().getLayoutInflater();

        View view = lf.inflate(R.layout.fragment_auto_fit_recycler_view, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new MarginDecoration(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        // do whatever
//                        Log.v(TAG, imageList.get(position).getID());

                        Intent intent = new Intent(getActivity(), PostSelected.class);
                        intent.putExtra("PostTitle", imageList.get(position).getTitle());
                        intent.putExtra("PostImage", imageList.get(position).getThumbnail());
                        intent.putExtra("PostText", imageList.get(position).getContent());
                        intent.putExtra("PostURL", imageList.get(position).getpostURL());
                        intent.putExtra("PostID", imageList.get(position).getID());

                        startActivity(intent);
                    }
                })
        );

        this.imageList = PhotoLoader.getWords();

        Log.v(TAG, "ImageList Size: " + imageList.size());

        if (imageList.size() < 1) {
            Log.v(TAG, "imageList size is less then 1");
            new PhotoLoader(){
                @Override
                protected void onPostExecute(Integer result) {
                    /* Download complete. Lets update UI */
                    if (result == 1) {
                        if (isRefreshing()){
                            stopRefresh();
                        }
                        Log.d(TAG, "Succeeded fetching data! - POST LOADER");
                    } else Log.e(TAG, "Failed to fetch data!");
                }
            }.execute(mURL);
        }

        adapter = new ImageAdapter(getActivity(), imageList);
        mRecyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_grid_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // uses the view to get the context instead of getActivity().
        return view;
    }

    @Override
    public void onResume() {
        this.imageList = PhotoLoader.getWords();
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
        Log.v(TAG, "onViewStateRestored");
        if(savedInstanceState != null)
        {
            Log.v(TAG, "savedInstanceState = null");
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(TAG, "In frag's on save instance state ");
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

}