package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import ua.in.quireg.chan.R;

/**
 * Created by Arcturus Mengsk on 8/8/2018, 8:43 AM.
 * 2ch-Browser
 */

public class LeftPaddedDivider extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int mThumbnailSize;
    private int mThumbnailPadding;

    public LeftPaddedDivider(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.shadowline);
        mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
        mThumbnailPadding = 8 * Math.round(((float)context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {

            View view = parent.getChildAt(i);

            if (parent.getChildAt(i + 1) instanceof android.widget.TextView || view instanceof android.widget.TextView) {
                //do not draw if it is a header or next item is a header
                continue;
            }

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            int left = mThumbnailSize + mThumbnailPadding;
            int top = view.getBottom() + params.bottomMargin;

            int right = parent.getWidth() - parent.getPaddingRight();

            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
