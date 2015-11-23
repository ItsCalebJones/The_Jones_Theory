package me.calebjones.blogsite.content.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.List;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.ui.activity.PostSelectedActivity;
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

    public void removeAll(){
        this.feedItemList.clear();
    }


    public int getLastItemNum() {
        int counter = feedItemList.size() - 1;
        int number = 999;
        int id;
        for (int i = 0; i < counter; i++){
            id = feedItemList.get(i).getID();
            if (id < number){
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
    public void onBindViewHolder(ViewHolder holder, int i) {
        Posts feedItem = feedItemList.get(i);

        position = i;

        Ion.with(holder.thumbnail)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .error(R.drawable.placeholder)
                .load(feedItem.getFeaturedImage());

        holder.title.setText(Html.fromHtml(feedItem.getTitle()));
        holder.excerpt.setText(Html.fromHtml(feedItem.getExcerpt()));
        holder.tags.setText("Tags: " + Html.fromHtml(feedItem.getTags()));
        holder.category.setText("Category: " + Html.fromHtml(feedItem.getCategories()));
    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }


    //Class that binds data to the views and registers the clicks.
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView thumbnail;
        public TextView title;
        public TextView content;
        public TextView excerpt;
        public TextView category;
        public TextView tags;
        public TextView ID;
        private View tMainView;

        //Add content to the card
        public ViewHolder(View view) {
            super(view);

            tMainView = mView.findViewById(R.id.fragment_feed_content);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            title = (TextView) view.findViewById(R.id.title);
            excerpt = (TextView) view.findViewById(R.id.excerpt);
            tags = (TextView) view.findViewById(R.id.tags);
            category = (TextView) view.findViewById(R.id.category);

            thumbnail.setOnClickListener(this);
            title.setOnClickListener(this);
        }

        //React to click events.
        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            FeedFragment.exitReveal(tMainView);
            switch (v.getId()) {
                case R.id.title:
                    break;
                case R.id.thumbnail:
                    Intent intent = new Intent(mContext, PostSelectedActivity.class);
                    intent.putExtra("PostCat", feedItemList.get(position).getCategories());
                    intent.putExtra("PostTitle", feedItemList.get(position).getTitle());
                    intent.putExtra("PostImage", feedItemList.get(position).getFeaturedImage());
                    intent.putExtra("PostText", feedItemList.get(position).getContent());
                    intent.putExtra("PostURL", feedItemList.get(position).getURL());
                    intent.putExtra("PostID", feedItemList.get(position).getPostID());
                    intent.putExtra("ID", feedItemList.get(position).getID());
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}