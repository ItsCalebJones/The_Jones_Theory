package me.calebjones.blogsite.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.koushikdutta.ion.Ion;
import java.util.List;

import me.calebjones.blogsite.R;


public class MyRecyclerAdapter extends RecyclerView.Adapter<FeedListRowHolder> {


    private List<FeedItem> feedItemList;
    private Context mContext;
    public int position;


    public MyRecyclerAdapter(Context context, List<FeedItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

//    @Override
//    public FeedListRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
//        FeedListRowHolder mh = new FeedListRowHolder(v);
//        return mh;
//    }

    //You have several options.
    // Putting the position in an int field inside the viewholder
    // update it while binding
    // then returning it on click is one.

    @Override
    public FeedListRowHolder onCreateViewHolder(ViewGroup viewGroup,
                                                int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, null);

//        Log.d("TAG", "Position: " + position);

        FeedListRowHolder vh = new FeedListRowHolder(v, new FeedListRowHolder.IMyViewHolderClicks() {
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
    public void onBindViewHolder(FeedListRowHolder feedListRowHolder, int i) {
        final FeedItem feedItem = feedItemList.get(i);

//        Log.d("TAG", "feedItem: " + feedItem);
//        Log.d("TAG", "FeedListRow: " + feedListRowHolder);
//        Log.d("TAG", "Position: " + position);
        position = i;

        Ion.with(feedListRowHolder.thumbnail)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .error(R.drawable.placeholder)
                .load(feedItem.getThumbnail());

        feedListRowHolder.title.setText(Html.fromHtml(feedItem.getTitle()));
        feedListRowHolder.excerpt.setText(Html.fromHtml(feedItem.getExcerpt()));
        }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

}
