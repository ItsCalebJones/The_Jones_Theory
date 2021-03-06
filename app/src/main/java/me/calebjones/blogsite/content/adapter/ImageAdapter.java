package me.calebjones.blogsite.content.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.List;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.ui.activity.DetailActivity;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.util.BlipUtils;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {


    private List<Posts> imageItemList;
    private Context mContext;
    private DisplayMetrics metrics;
    public int position;


    public ImageAdapter(Context context) {
        this.mContext = context;
        OkHttpClient picassoClient = BlogsiteApplication.getInstance().client.clone();
        picassoClient.interceptors().add(BlipUtils.REWRITE_CACHE_CONTROL_INTERCEPTOR);
        new Picasso.Builder(context).downloader(new OkHttpDownloader(picassoClient)).build();
        new DisplayMetrics();
        metrics = Resources.getSystem().getDisplayMetrics();
    }

    public void addItems(List<Posts> imageItemList) {
        if (this.imageItemList == null) {
            this.imageItemList = imageItemList;
        } else {
            this.imageItemList.addAll(imageItemList);
        }
        Log.d("The Jones Theory", "ImageList Size: " + String.valueOf(imageItemList.size()));
    }

    public void removeAll(){
        this.imageItemList.clear();
    }

    public int getLastItemNum() {
        Log.d("The Jones Theory", "ImageList Size: " + String.valueOf(imageItemList.size()));
        return imageItemList.get(imageItemList.size() - 1).getID();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_image_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder imageRowHolder, int i) {
        final Posts imageItem = imageItemList.get(i);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels / 3;

        position = i;

        Glide.with(mContext)
                .load(imageItem.getFeaturedImage())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(imageRowHolder.thumbnail);

        imageRowHolder.title.setText(Html.fromHtml(imageItem.getTitle()));
        }

    @Override
    public int getItemCount() {
        return imageItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView thumbnail;

        public TextView title;
        public TextView content;
        public TextView excerpt;
        public TextView category;
        public TextView tags;
        public TextView ID;

        public ViewHolder(View view) {
            super(view);

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            title = (TextView) view.findViewById(R.id.image_title);

            thumbnail.setOnClickListener(this);
            title.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra("PostTitle", imageItemList.get(position).getTitle());
            intent.putExtra("PostImage", imageItemList.get(position).getFeaturedImage());
            intent.putExtra("PostText", imageItemList.get(position).getContent());
            intent.putExtra("PostURL", imageItemList.get(position).getURL());
            intent.putExtra("PostID", imageItemList.get(position).getPostID());
            intent.putExtra("ID", imageItemList.get(position).getID());
            mContext.startActivity(intent);
//            mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(DetailActivity.this, imgFavorite, "photo_hero").toBundle());
        }
    }
}
