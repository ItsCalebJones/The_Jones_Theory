package me.calebjones.thejonestheory.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import me.calebjones.thejonestheory.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WebView.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WebView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebView extends Fragment {

    private final static String URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=20";
    public static final String TAG = "stats";

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebView.
     */
    // TODO: Rename and change types and number of parameters
    public static WebView newInstance(String param1, String param2) {
        WebView fragment = new WebView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public WebView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        android.webkit.WebView myWebView = (android.webkit.WebView) view.findViewById(R.id.webview2);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        myWebView.loadUrl("http://calebjones.me/hireme");

        final ProgressBar Pbar;
        Pbar = (ProgressBar) view.findViewById(R.id.pB1);

        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(android.webkit.WebView view, int progress)
            {
                if(progress < 100 && Pbar.getVisibility() == ProgressBar.GONE){
                    Pbar.setVisibility(ProgressBar.VISIBLE);
                }
                Pbar.setProgress(progress);
                if(progress == 100) {
                    Pbar.setVisibility(ProgressBar.GONE);
                }
            }
        });
        if (savedInstanceState != null) {
            myWebView.restoreState(savedInstanceState);
        } else {
//            if(content == null && !content.isEmpty()){
//            myWebView.loadUrl("http://www.tfmamba.com");
        }
        Log.v("DetailFragment", "onCreateView()");

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
