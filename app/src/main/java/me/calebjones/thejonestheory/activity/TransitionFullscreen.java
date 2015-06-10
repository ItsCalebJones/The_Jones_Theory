package me.calebjones.thejonestheory.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import me.calebjones.thejonestheory.R;
import uk.co.senab.photoview.PhotoView;


/**
 * Created by koush on 11/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TransitionFullscreen extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mActionBarBackgroundDrawable = mToolbar.getBackground();
        setSupportActionBar(mToolbar);

        //Setup the Actionabar backbutton and elevation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(60);

        final PhotoView photoView = (PhotoView)findViewById(R.id.image);
        photoView.setMaximumScale(16);


//        final ImageView iv = (ImageView)findViewById(R.id.image);
        String bitmapKey = getIntent().getStringExtra("bitmapInfo");
        BitmapInfo bi = Ion.getDefault(this)
                .getBitmapCache()
                .get(bitmapKey);
        photoView.setImageBitmap(bi.bitmap);



        final String PostImage = getIntent().getExtras().getString("PostImage");

        getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                getWindow().getEnterTransition().removeListener(this);

                // load the full version, crossfading from the thumbnail image
                Ion.with(photoView)
                        .crossfade(true)
                        .deepZoom()
                        .load(PostImage);

            }
        });
    }
}
