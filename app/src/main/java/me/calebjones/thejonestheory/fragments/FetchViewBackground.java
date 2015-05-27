package me.calebjones.thejonestheory.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import me.calebjones.thejonestheory.feed.FeedItem;
import me.calebjones.thejonestheory.feed.JsonBackground;
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
public class FetchViewBackground extends Fragment {

    private static Context context;
    private Button btnSubmit;
    public final static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=20";
    public static final String TAG = "stats";
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;
    public List<FeedItem> feedItemList;

    private OnFragmentInteractionListener mListener;

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity c = getActivity();

        LayoutInflater lf = getActivity().getLayoutInflater();

        View view = lf.inflate(R.layout.fragment_fetch_data, container, false);

//
//        tvPostCount = (TextView) view.findViewById(R.id.txtPostCount);
//        tvPostTitle = (TextView) view.findViewById(R.id.txtPostTitle);
//        tvPostUrl = (TextView) view.findViewById(R.id.txtPostUrl);

//        /* Allow activity to show indeterminate progressbar */
//        this.getActivity().requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        /* Initialize recyclerview */
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever
                        Toast.makeText(getActivity(),  position + " has been clicked.", Toast.LENGTH_SHORT).show();
                    }
                })
        );
        this.feedItemList=JsonBackground.getWords();

        adapter = new MyRecyclerAdapter(getActivity(), feedItemList);
        mRecyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;
    }


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

}
