package me.calebjones.blogsite.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.koushikdutta.ion.Ion;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.ui.activity.DetailActivity;
import me.calebjones.blogsite.content.database.DatabaseManager;
import me.calebjones.blogsite.content.models.FeedItem;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.util.BlipUtils;

public class RandomFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public static final String TAG = "The Jones Theory";
    public String mCategory = null;
    public List<Posts> feedItemList;

    private DatabaseManager databaseManager;
    private RecyclerView mRecyclerView;

    public ImageView thumbnail, headerIcon, categoryIcon, favorite, browser;
    public TextView title, content, excerpt, category, tags, feed, ID;
    public Integer mPosition;
    public android.support.v7.widget.AppCompatButton shareButton, exploreButton;
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
        categoryIcon = (ImageView) view.findViewById(R.id.categoryIcon);
        favorite = (ImageView) view.findViewById(R.id.favorite);
        browser = (ImageView) view.findViewById(R.id.web_launcher);
        headerIcon = (ImageView) view.findViewById(R.id.imageIcon);
        title = (TextView) view.findViewById(R.id.title);
        excerpt = (TextView) view.findViewById(R.id.excerpt);
        tags = (TextView) view.findViewById(R.id.tags);
        shareButton = (android.support.v7.widget.AppCompatButton)
                view.findViewById(R.id.shareButton);
        exploreButton = (android.support.v7.widget.AppCompatButton)
                view.findViewById(R.id.exploreButton);

        noPost = view.findViewById(R.id.no_Post);
        item = view.findViewById(R.id.Random_posts);

        favorite.setOnClickListener(this);
        browser.setOnClickListener(this);
        thumbnail.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        exploreButton.setOnClickListener(this);

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

//        Ion.with(thumbnail)
//                .placeholder(R.drawable.placeholder)
//                .centerCrop()
//                .error(R.drawable.placeholder)
//                .load(post.getFeaturedImage());

        //Setup the category icon
        String cat = post.getCategories().toLowerCase();
        if (cat.contains("blog")) {
            categoryIcon.setImageResource(R.drawable.ic_about);
        } else if (cat.contains("parent")) {
            categoryIcon.setImageResource(R.drawable.ic_parenting);
        } else if (cat.contains("tech")) {
            categoryIcon.setImageResource(R.drawable.ic_tech);
        } else if (cat.contains("android")) {
            categoryIcon.setImageResource(R.drawable.ic_android);
        } else if (cat.contains("science")) {
            categoryIcon.setImageResource(R.drawable.ic_science);
        }

        //Setup the Favorite Icon
        if (post.isFavourite()) {
            favorite.setColorFilter(Color.RED);
        } else {
            favorite.setColorFilter(Color.BLACK);
        }
        browser.setColorFilter(Color.BLACK);

        Glide.with(getActivity())
                .load(post.getFeaturedImage())
                .bitmapTransform(new BlurTransformation(getActivity(), 25, 2),
                        new CropCircleTransformation(getActivity()))
                .into(headerIcon);

        Glide.with(getActivity())
                .load(post.getFeaturedImage())
                .asBitmap()
                .into(new BitmapImageViewTarget(thumbnail) {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);
                        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                // Here's your generated palette
                                categoryIcon.setColorFilter(palette
                                        .getLightMutedColor(Color.BLACK));
                                exploreButton.setTextColor(palette
                                        .getVibrantColor(Color.BLACK));
                                shareButton.setTextColor(palette
                                        .getVibrantColor(Color.BLACK));

                            }
                        });
                    }
                });

    }

    //React to click events.
    @Override
    public void onClick(View v) {
        Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                .putExtra("PostTitle", post.getTitle())
                .putExtra("PostImage", post.getFeaturedImage())
                .putExtra("PostText", post.getContent())
                .putExtra("PostURL", post.getURL())
                .putExtra("PostID", post.getPostID())
                .putExtra("ID", post.getID());

        switch (v.getId()) {
            case R.id.shareButton:
                //Set up the PendingIntent for the Share action button
                Intent sendThisIntent = new Intent();
                sendThisIntent.setAction(Intent.ACTION_SEND);
                sendThisIntent.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
                sendThisIntent.putExtra(Intent.EXTRA_TEXT, post.getURL());
                sendThisIntent.setType("text/plain");
                getActivity().startActivity(Intent.createChooser(sendThisIntent, "Share"));
                break;
            case R.id.thumbnail:
                //Setup Intent mContext
                getActivity().startActivity(detailIntent);
                break;
            case R.id.exploreButton:
                //Setup Intent mContext
                getActivity().startActivity(detailIntent);
                break;
            case R.id.web_launcher:
                String url = post.getURL();
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse(url));
                getActivity().startActivity(sendIntent);
                break;
            case R.id.favorite:
                boolean fav = post.isFavourite();
                post.setFavourite(!fav);
                databaseManager.setFavourite(post.getID(), !fav);
                if (fav) {
                    favorite.setColorFilter(Color.BLACK);
                } else {
                    //make fav
                    favorite.setColorFilter(getActivity().getColor(R.color.myAccentColor));
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_random, menu);
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

    public void onDestroy()
    {
        super.onDestroy();
        if (databaseManager != null)
        {
            databaseManager.close();
        }
    }



}
