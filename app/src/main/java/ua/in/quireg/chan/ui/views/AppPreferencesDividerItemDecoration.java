package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ua.in.quireg.chan.R;

/**
 * Created by Arcturus Mengsk on 2/15/2018, 10:41 AM.
 * 2ch-Browser
 */

public class AppPreferencesDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public AppPreferencesDividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.list_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {

            View view = parent.getChildAt(i);

            if (parent.getChildAt(i + 1) instanceof TextView || view instanceof TextView) {
                //do not draw if it is a header or next item is a header
                continue;
            }

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            int left = parent.getPaddingLeft() * 2;
            int right = parent.getWidth() - parent.getPaddingRight();

            int top = view.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}