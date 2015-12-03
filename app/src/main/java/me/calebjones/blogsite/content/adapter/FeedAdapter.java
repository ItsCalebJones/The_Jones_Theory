package me.calebjones.blogsite.content.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.database.DatabaseManager;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.ui.activity.DetailActivity;
import me.calebjones.blogsite.ui.fragments.FeedFragment;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    public int position;
    public View mView;
    private List<Posts> feedItemList;
    private Context mContext;

    public FeedAdapter(Context context, View view) {
        this.mContext = context;
        this.mView = view;
    }

    public void addItems(List<Posts> feedItemList) {
        if (this.feedItemList == null) {
            this.feedItemList = feedItemList;
        } else {
            this.feedItemList.addAll(feedItemList);
        }
    }

    public void removeAll() {
        this.feedItemList.clear();
    }


    public int getLastItemNum() {
        int counter = feedItemList.size() - 1;
        int number = 999;
        int id;
        for (int i = 0; i < counter; i++) {
            id = feedItemList.get(i).getID();
            if (id < number) {
                number = id;
            }
        }
        return number;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                         int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        Posts feedItem = feedItemList.get(i);

        position = i;

        //Setup the category icon
        String cat = feedItem.getCategories().toLowerCase();
        if (cat.contains("blog")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_about);
        } else if (cat.contains("parent")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_parenting);
        } else if (cat.contains("tech")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_tech);
        } else if (cat.contains("android")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_android);
        } else if (cat.contains("science")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_science);
        }

        //Setup the Favorite Icon
        if (feedItem.isFavourite()) {
            holder.favorite.setColorFilter(Color.RED);
        } else {
            holder.favorite.setColorFilter(Color.BLACK);
        }
        holder.browser.setColorFilter(Color.BLACK);

        Glide.with(mContext)
                .load(feedItem.getFeaturedImage())
                .bitmapTransform(new BlurTransformation(mContext, 25, 1), new CropCircleTransformation(mContext))
                .into(holder.headerIcon);

        Glide.with(mContext)
                .load(feedItem.getFeaturedImage())
                .asBitmap()
                .into(new BitmapImageViewTarget(holder.thumbnail) {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);
                        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                // Here's your generated palette
                                holder.categoryIcon.setColorFilter(palette
                                        .getLightMutedColor(Color.BLACK));
                                holder.exploreButton.setTextColor(palette
                                        .getVibrantColor(Color.BLACK));
                                holder.shareButton.setTextColor(palette
                                        .getVibrantColor(Color.BLACK));

                            }
                        });
                    }
                });


//        //GreyScale the HeaderIcon
//        ColorMatrix matrix = new ColorMatrix();
//        matrix.setSaturation(1);  //0 means grayscale
//        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
//        holder.headerIcon.setColorFilter(cf);
//        holder.headerIcon.setAlpha(128);   // 128 = 0.5

        holder.title.setText(Html.fromHtml(feedItem.getTitle()));
        holder.excerpt.setText(Html.fromHtml(shorten(feedItem.getExcerpt())));
        holder.tags.setText(Html.fromHtml(feedItem.getTags()));
//        holder.category.setText("Category: " + Html.fromHtml(feedItem.getCategories()));
    }

    private String shorten(String text) {
        if (text.length() > 140) {
            text = text.substring(0, 140) + "...";
        }
        return text;
    }


    @Override
    public int getItemCount() {
        return feedItemList.size();
    }


    //Class that binds data to the views and registers the clicks.
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView thumbnail, headerIcon, categoryIcon, favorite, browser;
        public TextView title, content, excerpt, category, tags, ID;
        public View tMainView, titleCard;
        public android.support.v7.widget.AppCompatButton shareButton, exploreButton;

        DatabaseManager databaseManager = new DatabaseManager(mContext);

        //Add content to the card
        public ViewHolder(View view) {
            super(view);

            tMainView = mView.findViewById(R.id.fragment_feed_content);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            categoryIcon = (ImageView) view.findViewById(R.id.categoryIcon);
            favorite = (ImageView) view.findViewById(R.id.favorite);
            browser = (ImageView) view.findViewById(R.id.web_launcher);
            headerIcon = (ImageView) view.findViewById(R.id.imageIcon);
            title = (TextView) view.findViewById(R.id.title);
            excerpt = (TextView) view.findViewById(R.id.excerpt);
            tags = (TextView) view.findViewById(R.id.tags);
            titleCard = view.findViewById(R.id.TitleCard);
            shareButton = (android.support.v7.widget.AppCompatButton)
                    view.findViewById(R.id.shareButton);
            exploreButton = (android.support.v7.widget.AppCompatButton)
                    view.findViewById(R.id.exploreButton);

//            category = (TextView) view.findViewById(R.id.category);

            favorite.setOnClickListener(this);
            browser.setOnClickListener(this);
            thumbnail.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            exploreButton.setOnClickListener(this);
        }

        //React to click events.
        @Override
        public void onClick(View v) {

            final int position = getAdapterPosition();
            switch (v.getId()) {
                case R.id.shareButton:
                    //Set up the PendingIntent for the Share action button
                    Intent sendThisIntent = new Intent();
                    sendThisIntent.setAction(Intent.ACTION_SEND);
                    sendThisIntent.putExtra(Intent.EXTRA_SUBJECT, feedItemList.get(position).getTitle());
                    sendThisIntent.putExtra(Intent.EXTRA_TEXT, feedItemList.get(position).getURL());
                    sendThisIntent.setType("text/plain");
                    mContext.startActivity(Intent.createChooser(sendThisIntent, "Share"));
                    break;
                case R.id.thumbnail:
                    FeedFragment.exitReveal(tMainView);
                    //Setup Intent mContext
                    Intent thumbnailIntent = new Intent(mContext, DetailActivity.class)
                            .putExtra("PostCat", feedItemList.get(position).getCategories())
                            .putExtra("PostTitle", feedItemList.get(position).getTitle())
                            .putExtra("PostImage", feedItemList.get(position).getFeaturedImage())
                            .putExtra("PostText", feedItemList.get(position).getContent())
                            .putExtra("PostURL", feedItemList.get(position).getURL())
                            .putExtra("PostID", feedItemList.get(position).getPostID())
                            .putExtra("ID", feedItemList.get(position).getID());
                    mContext.startActivity(thumbnailIntent);
                case R.id.exploreButton:
                    FeedFragment.exitReveal(tMainView);
                    //Setup Intent mContext
                    Intent exploreIntent = new Intent(mContext, DetailActivity.class)
                            .putExtra("PostCat", feedItemList.get(position).getCategories())
                            .putExtra("PostTitle", feedItemList.get(position).getTitle())
                            .putExtra("PostImage", feedItemList.get(position).getFeaturedImage())
                            .putExtra("PostText", feedItemList.get(position).getContent())
                            .putExtra("PostURL", feedItemList.get(position).getURL())
                            .putExtra("PostID", feedItemList.get(position).getPostID())
                            .putExtra("ID", feedItemList.get(position).getID());
                    mContext.startActivity(exploreIntent);
                    break;
                case R.id.web_launcher:
                    String url = feedItemList.get(position).getURL();
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setData(Uri.parse(url));
                    mContext.startActivity(sendIntent);
                    break;
                case R.id.favorite:
                    boolean fav = feedItemList.get(position).isFavourite();
                    feedItemList.get(position).setFavourite(!fav);
                    databaseManager.setFavourite(feedItemList.get(position).getID(), !fav);
                    if (fav) {
                        favorite.setColorFilter(Color.BLACK);
                    } else {
                        //make fav
                        Toast.makeText(mContext, "Favorite set!", Toast.LENGTH_LONG).show();
                        favorite.setColorFilter(mContext.getColor(R.color.myAccentColor));
                    }
                    break;
            }
        }
    }

    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }

}