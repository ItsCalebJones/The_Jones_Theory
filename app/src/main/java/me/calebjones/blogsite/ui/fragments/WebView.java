package me.calebjones.blogsite.ui.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import me.calebjones.blogsite.R;

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
    public static final String TAG = "The Jones Theory";
    public static android.webkit.WebView myWebView;

    private OnFragmentInteractionListener mListener;

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

        final android.webkit.WebView myWebView = (android.webkit.WebView) view.findViewById(R.id.webview2);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        myWebView.loadUrl("http://calebjones.me/hireme");
        myWebView.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack())
                {
                    myWebView.goBack();
                    return true;
                }
                return false;
            }
        });

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

        //Set NavBar to translucent | Currently bugged
//        int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
//        if (id != 0 && getResources().getBoolean(id)) {
//            Window w = getActivity().getWindow();
//            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }

        return view;
    }

    public boolean canGoBack() {
        return myWebView.canGoBack();
    }

    public void goBack() {
        myWebView.goBack();
    }


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
        int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
        if (id == 0) {
            // not on KitKat
        } else {
            boolean enabled = getResources().getBoolean(id);
            // enabled = are translucent bars supported on this device
            // Set the status bar to dark-semi-transparentish
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }

    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
