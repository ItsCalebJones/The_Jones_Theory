package me.calebjones.blogsite.gallery;

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


public class ImageAdapter extends RecyclerView.Adapter<ImageRowHolder> {


    private List<ImageItem> imageItemList;
    private Context mContext;
    public int position;


    public ImageAdapter(Context context, List<ImageItem> imageItemList) {
        this.imageItemList = imageItemList;
        this.mContext = context;
    }

//    @Override
//    public ImageRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
//        ImageRowHolder mh = new ImageRowHolder(v);
//        return mh;
//    }

    //You have several options.
    // Putting the position in an int field inside the viewholder
    // update it while binding
    // then returning it on click is one.

    @Override
    public ImageRowHolder onCreateViewHolder(ViewGroup viewGroup,
                                                int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.image_item, null);

//        Log.d("TAG", "Position: " + position);

        ImageRowHolder vh = new ImageRowHolder(v, new ImageRowHolder.IMyViewHolderClicks() {
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
    public void onBindViewHolder(ImageRowHolder imageRowHolder, int i) {
        final ImageItem imageItem = imageItemList.get(i);

//        Log.d("TAG", "imageItem: " + imageItem);
//        Log.d("TAG", "FeedListRow: " + imageRowHolder);
//        Log.d("TAG", "Position: " + position);
        position = i;

        Ion.with(imageRowHolder.thumbnail)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .error(R.drawable.placeholder)
                .load(imageItem.getThumbnail());

        imageRowHolder.title.setText(Html.fromHtml(imageItem.getTitle()));
//        imageRowHolder.excerpt.setText(Html.fromHtml(imageItem.getExcerpt()));
//        imageRowHolder.tags.setText(Html.fromHtml(imageItem.getTags()));
        }

    @Override
    public int getItemCount() {
        return (null != imageItemList ? imageItemList.size() : 0);
    }

}
