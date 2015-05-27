package me.calebjones.thejonestheory.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import java.util.List;

import me.calebjones.thejonestheory.R;

public class MyRecyclerAdapter extends RecyclerView.Adapter<FeedListRowHolder> {


    private List<FeedItem> feedItemList;
    private Context mContext;

    public MyRecyclerAdapter(Context context, List<FeedItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public FeedListRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
        FeedListRowHolder mh = new FeedListRowHolder(v);

        return mh;
    }

    @Override
    public void onBindViewHolder(FeedListRowHolder feedListRowHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);
        Log.e("TAG", "feedItem: " + feedItem);
        Log.e("TAG", "FeedListRow: " + feedListRowHolder);


        Ion.with(feedListRowHolder.thumbnail)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .error(R.drawable.placeholder)
                .load(feedItem.getThumbnail());

//        Picasso.with(mContext)
//                .load(feedItem.getThumbnail())
//                .error(R.drawable.placeholder)
//                .placeholder(R.drawable.placeholder)
//                .into(feedListRowHolder.thumbnail);



        feedListRowHolder.title.setText(Html.fromHtml(feedItem.getTitle()));
        feedListRowHolder.excerpt.setText(Html.fromHtml(feedItem.getExcerpt()));
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

}
