package me.calebjones.blogsite.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.activity.PostSelected;
import me.calebjones.blogsite.feed.FeedItem;
import me.calebjones.blogsite.feed.FeedAdapter;
import me.calebjones.blogsite.loader.PostLoader;
import me.calebjones.blogsite.util.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FetchViewBackground.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FetchViewBackground#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FetchViewBackground extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "The Jones Theory";
    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?category=";
    public String numPost = "&number=15";
    public String mCategory = null;
    public String tURL;
    public String category;
    public List<FeedItem> feedItemList;
    public int num;


    private Button btnSubmit;
    private RecyclerView mRecyclerView;
    private FeedAdapter adapter;
    private ProgressDialog nDialog;
    private OnFragmentInteractionListener mListener;
    private static Context context;
    private static final String BUNDLE_RECYCLER_LAYOUT = "FetchViewBackground.mRecylcerView.fragment_fetch_data";

    SwipeRefreshLayout mSwipeRefreshLayout;

    int showPbar = 0;

    public FetchViewBackground() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context hostActivity = getActivity();

        Bundle bundle = this.getArguments();
        mCategory = bundle.getString("category", "");
        tURL = mURL + mCategory + numPost;
        Log.d(TAG, tURL);

        setHasOptionsMenu(true);

//        Log.d("SI." + TAG, "Checking if null!");
//        if (savedInstanceState == null){
//            new RefreshHttpTask().execute(tURL);
//        } else {
//            for (String key: savedInstanceState.keySet())
//            {
//                Log.d ("SI." + TAG, key + " is a key in the bundle");
//            }
//        }

        this.setRetainInstance(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity c = getActivity();

        LayoutInflater lf = getActivity().getLayoutInflater();

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
//                        Toast.makeText(getActivity(), position + " has been clicked.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), PostSelected.class);
                        intent.putExtra("PostTitle", feedItemList.get(position).getTitle());
                        intent.putExtra("PostImage", feedItemList.get(position).getThumbnail());
                        intent.putExtra("PostText", feedItemList.get(position).getContent());
                        intent.putExtra("PostURL", feedItemList.get(position).getpostURL());
                        intent.putExtra("PostID", feedItemList.get(position).getID());
                        startActivity(intent);
                    }
                })
        );
        this.feedItemList = PostLoader.getWords();

        adapter = new FeedAdapter(getActivity(), feedItemList);
        mRecyclerView.setAdapter(adapter);
//        Log.v("The Jones Theory", "mRecyclerView DataAdapter set!");

        /*Set up Pull to refresh*/
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        // @TODO Theres a bug in this code, I dont know how to fix it yet. Still trying to add features.
        if (feedItemList != null) {
            if (feedItemList.size() > 0) {
                Log.d(TAG, "FeedItemList is not null! " + category + " " + mCategory + " " + tURL);
                if (category == "blog" & mCategory == null){
//                    new RefreshHttpTask().execute(tURL);
                    Log.d(TAG, "First Launch!");
                } else if (category != mCategory){
                    Log.d(TAG, "I dont know");
                    new RefreshHttpTask().execute(tURL);
                }
            } else {
                Log.d(TAG, "FeedItemList Size: " + feedItemList.size());
                new RefreshHttpTask().execute(tURL);
            }
        }

        if (mCategory != "" & mCategory != null){
            mCategory = Character.toString(mCategory.charAt(0)).toUpperCase()+mCategory.substring(1);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("The Jones Theory - " + mCategory);
        }


        // Inflate the layout for this fragment
        return view;
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

        if (id == R.id.menuRefresh){
            onRefresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    public void onRefresh() {
//        new PostLoader().execute(mURL);
        mSwipeRefreshLayout.setRefreshing(true);
        showPbar = 1;
//        String tURL = new StringBuilder().append(mURL).append(numPost).toString();
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

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v("SI." + TAG, "onViewStateRestored");
        if(savedInstanceState != null)
        {
            Log.v("SI." + TAG, "savedInstanceState = null");
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("SI." + TAG, "In frag's on save instance state ");
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }



    public class RefreshHttpTask extends AsyncTask<String, Void, Integer> {
        private ProgressDialog dialog;
        private int progress = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showPbar == 0){
                dialog = new ProgressDialog(getActivity());
                if (category != null) {
                    dialog.setMessage("Loading Top 15 " + category + " posts...");
                } else {
                    dialog.setMessage("Loading Top 15 posts...");
                }
                dialog.show();
            }

            if (feedItemList != null ) {
                if (!mSwipeRefreshLayout.isRefreshing()){
                    feedItemList.clear();
                }
            }
            mSwipeRefreshLayout.setRefreshing(true);

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
            mSwipeRefreshLayout.setRefreshing(false);
            /* Download complete. Lets update UI */
            if (result == 1) {
//                adapter = new ImageAdapter(getActivity(), feedItemList);
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

    public static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
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

                //Cuts out all the Child nodes and gets only the  tags.
                JSONObject  Tobj = new JSONObject(post.optString("tags"));
                JSONArray Tarray=Tobj.names();
                String tagsList = null;

                if (Tarray != null) {

                    for (int c = 0; c < Tarray.length(); c++) {

//                        Log.d("dThe Jones Theory", Tarray.getString(c));
                        if (tagsList != null) {
                            String thisTag = Tarray.getString(c);
                            thisTag = capitalizeString(thisTag);
//                            thisTag = Character.toString(thisTag.charAt(0)).toUpperCase()+thisTag.substring(1);
                            tagsList = tagsList + ", " + thisTag;
                        } else {
                            String thisTag = Tarray.getString(c);
                            thisTag = capitalizeString(thisTag);
                            tagsList = thisTag;
                        }
                    }
                    item.setTags("<b>Tags</b>: " + tagsList);
                } else {
                    item.setTags("");
                }



                //Cuts out all the Child nodes and gets only the categories.
                JSONObject  Cobj = new JSONObject(post.optString("categories"));
                JSONArray Carray=Cobj.names();
                String catList = null;

                if (Carray != null) {


                    for (int d = 0; d < Carray.length(); d++) {

//                        Log.e("parenttag", Carray.getString(d));
                        if (catList != null) {
                            catList = catList + ", " + Carray.getString(d);
                        } else {
                            catList = Carray.getString(d);
                        }


                    }
                    item.setCategory("Category: " + catList);
                } else {
                    item.setCategory("Category: Unknown");
                }


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
