package me.calebjones.blogsite.util.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.calebjones.blogsite.R;

public class MarginDecorationFetch extends RecyclerView.ItemDecoration {
    private int margin;

    public MarginDecorationFetch(Context context) {
        margin = context.getResources().getDimensionPixelSize(R.dimen.item_margin_fetch);
    }

    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(margin, margin, margin, margin);
    }
}