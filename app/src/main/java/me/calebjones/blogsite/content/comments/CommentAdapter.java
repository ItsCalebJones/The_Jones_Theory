package me.calebjones.blogsite.content.comments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import me.calebjones.blogsite.R;



public class CommentAdapter extends RecyclerView.Adapter<CommentListRowHolder> {


    private List<CommentItem> commentItemList;
    private Context mContext;
    public int position;


    public CommentAdapter(Context context, List<CommentItem> commentItemList) {
        this.commentItemList = commentItemList;
        this.mContext = context;
    }

    @Override
    public CommentListRowHolder onCreateViewHolder(ViewGroup viewGroup,
                                                int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_comment_item, null);

        Log.d("TAG", "Position: " + position);

        CommentListRowHolder vh = new CommentListRowHolder(v, new CommentListRowHolder.IMyViewHolderClicks() {
            public int positionItem() {
//                Log.d("TAG", "Position: " + position);
                return position;
            }
            public void onPotato(View title) {
//                Log.d("The Jones Theory","Title - Position: " +  positionItem());

            }
            public void onTomato(ImageView thumbnail) {
//                Log.d("The Jones Theory", "Thumbnail - Position: " + positionItem());
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(CommentListRowHolder commentListRowHolder, int i) {
        final CommentItem commentItem = commentItemList.get(i);

        position = i;

        Log.d("TAG", "feedItem: " + commentItem);
        Log.d("TAG", "FeedListRow: " + commentListRowHolder);
        Log.d("TAG", "Position: " + position);

        commentListRowHolder.name.setText(Html.fromHtml(commentItem.getName()));
        commentListRowHolder.date.setText(Html.fromHtml(commentItem.getDate()));
        commentListRowHolder.content.setText(Html.fromHtml(commentItem.getContent()));
        }

    @Override
    public int getItemCount() {
        return (null != commentItemList ? commentItemList.size() : 0);
    }

}
