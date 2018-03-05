package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ua.in.quireg.chan.R;

/**
 * Created by Arcturus Mengsk on 2/15/2018, 10:24 PM.
 * 2ch-Browser
 */

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public SimpleDividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.list_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.State state) {

        int left = recyclerView.getPaddingLeft();
        int right = recyclerView.getWidth() - recyclerView.getPaddingRight();

        int childCount = recyclerView.getChildCount();

        for (int i = 0; i < childCount; i++) {

            View view = recyclerView.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();


            int top = view.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}