package me.calebjones.blogsite.content.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.models.CommentItem;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {


    private List<CommentItem> commentItemList;
    private Context mContext;
    public int position;


    public CommentAdapter(Context context, List<CommentItem> commentItemList) {
        this.commentItemList = commentItemList;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_comment_item, null);

        Log.d("TAG", "Position: " + position);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder commentRowHolder, int i) {
        final CommentItem commentItem = commentItemList.get(i);

        position = i;

        Log.d("TAG", "feedItem: " + commentItem);
        Log.d("TAG", "FeedListRow: " + commentRowHolder);
        Log.d("TAG", "Position: " + position);

        commentRowHolder.name.setText(Html.fromHtml(commentItem.getName()));
        commentRowHolder.date.setText(Html.fromHtml(commentItem.getDate()));
        commentRowHolder.content.setText(Html.fromHtml(commentItem.getContent()));
        }

    @Override
    public int getItemCount() {
        return (null != commentItemList ? commentItemList.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        public TextView name;
        public TextView content;
        public TextView date;


        public ViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.commenter_name);
            content = (TextView) view.findViewById(R.id.content);
            date = (TextView) view.findViewById(R.id.date);


            name.setOnClickListener(this);
            content.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
//            Toast.makeText(mContext, "Comment: " + commentItemList.get(position).getName(), Toast.LENGTH_LONG).show();
        }
    }

}
