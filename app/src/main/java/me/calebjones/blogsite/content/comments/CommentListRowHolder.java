package me.calebjones.blogsite.content.comments;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.calebjones.blogsite.R;

public class CommentListRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView thumbnail;
    public TextView name;
    public TextView content;
    public TextView date;
    public CommentItem comments;
    public IMyViewHolderClicks mListener;
    public Integer mPosition;

//    private ClickListener clickListener;

    public CommentListRowHolder(View view, IMyViewHolderClicks listener) {
        super(view);
        mListener = listener;
        this.name = (TextView) view.findViewById(R.id.commenter_name);
        this.content = (TextView) view.findViewById(R.id.content);
        this.date = (TextView) view.findViewById(R.id.date);

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



