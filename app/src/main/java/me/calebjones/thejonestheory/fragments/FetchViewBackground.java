package me.calebjones.thejonestheory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.calebjones.thejonestheory.MainActivity;
import me.calebjones.thejonestheory.R;
import me.calebjones.thejonestheory.activity.PostSelected;
import me.calebjones.thejonestheory.drawer.NavigationDrawerAdapter;
import me.calebjones.thejonestheory.feed.FeedItem;
import me.calebjones.thejonestheory.feed.FeedItemCallbacks;
import me.calebjones.thejonestheory.loader.PostLoader;
import me.calebjones.thejonestheory.feed.MyRecyclerAdapter;
import me.calebjones.thejonestheory.util.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FetchViewBackground.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FetchViewBackground#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FetchViewBackground extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static Context context;
    private Button btnSubmit;
    public String numPost;
    public int num;
    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=";
    public String tURL;
    public static final String TAG = "The Jones Theory";
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;
    public List<FeedItem> feedItemList;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressDialog nDialog;
    private OnFragmentInteractionListener mListener;
    int showPbar = 0;
    CircleProgressBar progressbar1;


    public static FetchViewBackground newInstance(String param1, String param2) {
        FetchViewBackground fragment = new FetchViewBackground();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FetchViewBackground() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context hostActivity = getActivity();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(hostActivity);
        preferences.getInt("pref_key_num_post", num);

        numPost = Integer.toString(num);
        Log.d(TAG, numPost);
        tURL = new StringBuilder().append(mURL).append(numPost).toString();

        setHasOptionsMenu(true);

        new RefreshHttpTask().execute(tURL);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity c = getActivity();

        LayoutInflater lf = getActivity().getLayoutInflater();

        if (feedItemList != null) {
            if (feedItemList.size() > 0) {
                Log.d(TAG, "Its not null!");
            } else {
                Log.d(TAG, "Its null!");
                new RefreshHttpTask().execute(tURL);
            }
        }

        View view = lf.inflate(R.layout.fragment_fetch_data, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        /* Needed to tell the RecyclerView which direction to scroll */
        LinearLayoutManager layoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever
                        Log.v(TAG, feedItemList.get(position).getID());
                        Toast.makeText(getActivity(),  position + " has been clicked.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), PostSelected.class);
                        intent.putExtra("PostTitle", feedItemList.get(position).getTitle());
                        intent.putExtra("PostImage", feedItemList.get(position).getThumbnail());
                        intent.putExtra("PostText", feedItemList.get(position).getContent());
                        intent.putExtra("PostURL", feedItemList.get(position).getpostURL());
                        startActivity(intent);
                    }
                })
        );
        this.feedItemList= PostLoader.getWords();

        adapter = new MyRecyclerAdapter(getActivity(), feedItemList);
        mRecyclerView.setAdapter(adapter);
        Log.v("The Jones Theory", "mRecyclerView Adapter set!");

        /*Set up Pull to refresh*/
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menuRefresh){
            onRefresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "FeedItemList: " + feedItemList.size());
        Log.d(TAG, "FeedItemList: " + adapter);
        Log.d(TAG, "FeedItemList: " + mRecyclerView);

        super.onResume();
    }

    @Override
    public void onRefresh() {
//        new PostLoader().execute(mURL);
        mSwipeRefreshLayout.setRefreshing(true);
        showPbar = 1;
        String tURL = new StringBuilder().append(mURL).append(numPost).toString();
        new RefreshHttpTask().execute(tURL);
        Log.v(TAG, "Refreshing - onRefresh method.");

    }

    private void setupAdapter() {
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.v("The Jones Theory", "setupAdapter Adapter set!");
    }

    public void closeRefreshListener(){
        mSwipeRefreshLayout.setRefreshing(false);
    }

    // fake a network operation's delayed response
    // this is just for demonstration, not real code!
//    private void refreshContent(){
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                adapter = new MyRecyclerAdapter(getActivity(), feedItemList);
//                mRecyclerView.setAdapter(adapter);
//                mSwipeRefreshLayout.setRefreshing(false);
//            });
//        }
//    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class RefreshHttpTask extends AsyncTask<String, Void, Integer> {
        private ProgressDialog dialog;
        private int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showPbar == 0){
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Loading " + numPost + " posts...");
                dialog.show();
            }
            if (feedItemList != null) {
                feedItemList.clear();
            }

        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            Integer result = 0;
            HttpURLConnection urlConnection = null;

            try {
                /* forming th java.net.URL object */
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();


                /* for Get request */
                urlConnection.setRequestMethod("GET");

                int statusCode = urlConnection.getResponseCode();


                /* 200 represents HTTP OK */
                if (statusCode ==  200) {

                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }

                    parseResult(response.toString());
                    result = 1; // Successful
                }else{
                    result = 0; //"Failed to fetch data!";
                }

            } catch (Exception e) {
                Log.v(TAG, e.getLocalizedMessage());
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (showPbar == 0 & dialog != null){
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            /* Download complete. Lets update UI */
            if (result == 1) {
//                adapter = new MyRecyclerAdapter(getActivity(), feedItemList);
//                mRecyclerView.setAdapter(adapter);
                setupAdapter();
                closeRefreshListener();
                Log.v(TAG, "Success! We fetched data!");
            } else {
                Log.e(TAG, "Failed to fetch data!");
                new AlertDialog.Builder(getActivity())
                        .setTitle("Failed to Load!")
                        .setMessage("Would you like to retry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                onRefresh();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                System.exit(0);
                            }
                        })
                        .show();
            }

        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("posts");
            feedItemList.clear();
            /*Initialize array if null*/
            if (null == feedItemList) {
                feedItemList = new ArrayList<FeedItem>();
            }

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);

                FeedItem item = new FeedItem();
                item.setTitle(post.optString("title"));
                item.setContent(post.optString("content"));
                item.setExcerpt(post.optString("excerpt"));
                item.setID(post.optString("ID"));
                item.setpostURL(post.optString("URL"));
                Integer ImageLength = post.optString("featured_image").length();
                if (ImageLength == 0) {
                    Log.v(TAG, "Feature Image null");
                    item.setThumbnail(null);
                } else {
                    item.setThumbnail(post.optString("featured_image"));
                }
                feedItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
