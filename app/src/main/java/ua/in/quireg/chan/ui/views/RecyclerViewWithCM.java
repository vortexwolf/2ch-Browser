package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

/**
 * Created by Arcturus Mengsk on 2/16/2018, 1:09 AM.
 * 2ch-Browser
 */

public class RecyclerViewWithCM extends RecyclerView {

    private ContextMenuInfo mContextMenuInfo;

    public RecyclerViewWithCM(Context context) {
        super(context);
    }

    public RecyclerViewWithCM(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewWithCM(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {

        final int longPressPosition = getChildAdapterPosition(originalView);

        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);

            mContextMenuInfo = new ContextMenuInfo(longPressPosition, longPressId);

            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public class ContextMenuInfo implements ContextMenu.ContextMenuInfo {

        ContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}