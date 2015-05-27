package me.calebjones.thejonestheory.feed;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import me.calebjones.thejonestheory.R;

public class FeedListRowHolder extends RecyclerView.ViewHolder {
    public ImageView thumbnail;
    public TextView title;
    public TextView content;
    public TextView ID;
    public TextView excerpt;

    public FeedListRowHolder(View view) {
        super(view);
        this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        this.title = (TextView) view.findViewById(R.id.title);
        this.excerpt = (TextView) view.findViewById(R.id.excerpt);
//        this.ID = (TextView) view.findViewById(R.id.ID);

    }

}