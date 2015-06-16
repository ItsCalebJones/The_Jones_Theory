package me.calebjones.blogsite.feed;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.calebjones.blogsite.R;

public class FeedListRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView thumbnail;
    public TextView title;
    public TextView content;
    public TextView excerpt;
    public FeedItem feed;
    public TextView ID;
    public IMyViewHolderClicks mListener;
    public Integer mPosition;

//    private ClickListener clickListener;

    public FeedListRowHolder(View view, IMyViewHolderClicks listener) {
        super(view);
        mListener = listener;
        this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        this.title = (TextView) view.findViewById(R.id.title);
        this.excerpt = (TextView) view.findViewById(R.id.excerpt);

        thumbnail.setOnClickListener(this);
        title.setOnClickListener(this);

    }

    public void bindPos(Integer position) {
        mPosition = position;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageView){
            mListener.onTomato((ImageView)v);
        } else {
            mListener.onPotato(v);
        }
    }

    public static interface IMyViewHolderClicks {
        public void onPotato(View caller);
        public void onTomato(ImageView callerImage);
    }

}



