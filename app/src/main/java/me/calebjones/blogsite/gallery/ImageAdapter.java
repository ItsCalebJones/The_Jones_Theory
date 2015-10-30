package me.calebjones.blogsite.gallery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.List;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.activity.PostSelectedActivity;
import me.calebjones.blogsite.models.Posts;
import me.calebjones.blogsite.util.BlipUtils;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {


    private List<Posts> imageItemList;
    private Context mContext;
    public int position;


    public ImageAdapter(Context context) {
        this.mContext = context;
        OkHttpClient picassoClient = BlogsiteApplication.getInstance().client.clone();
        picassoClient.interceptors().add(BlipUtils.REWRITE_CACHE_CONTROL_INTERCEPTOR);
        new Picasso.Builder(context).downloader(new OkHttpDownloader(picassoClient)).build();
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
                .inflate(R.layout.image_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder imageRowHolder, int i) {
        final Posts imageItem = imageItemList.get(i);

        position = i;

        Picasso.with(mContext)
                .load(imageItem.getFeaturedImage())
                .error(R.drawable.placeholder)
                .into(imageRowHolder.thumbnail);

//        Ion.with(mContext)
//                .load(imageItem.getFeaturedImage())
//                .withBitmap()
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.placeholder)
//                .asBitmap()
//                .setCallback(new FutureCallback<Bitmap>() {
//                    @Override
//                    public void onCompleted(Exception e, Bitmap result) {
//                        imageRowHolder.thumbnail.setImageBitmap(result);
//                        // do something with your bitmap
//                        if (result != null) {
//                            Palette.from(result).generate(new Palette.PaletteAsyncListener() {
//                                public void onGenerated(Palette palette) {
//                                    Palette.Swatch Swatch = palette.getVibrantSwatch();
//                                    if (Swatch != null) {
//                                        imageRowHolder.title.setBackgroundColor(Swatch.getRgb());
//
//                                    }
//                                }
//                            });
//                        }
//                    }
//                });
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
            Intent intent = new Intent(mContext, PostSelectedActivity.class);
            intent.putExtra("PostTitle", imageItemList.get(position).getTitle());
            intent.putExtra("PostImage", imageItemList.get(position).getFeaturedImage());
            intent.putExtra("PostText", imageItemList.get(position).getContent());
            intent.putExtra("PostURL", imageItemList.get(position).getURL());
            intent.putExtra("PostID", imageItemList.get(position).getPostID());
            intent.putExtra("ID", imageItemList.get(position).getID());
            mContext.startActivity(intent);
//            mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(PostSelectedActivity.this, imgFavorite, "photo_hero").toBundle());
        }
    }
}
