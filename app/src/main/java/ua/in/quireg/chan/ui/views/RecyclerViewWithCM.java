package ua.in.quireg.chan.ui.views;

import android.animation.AnimatorSet;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;

import timber.log.Timber;

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
        final int longPressPosition = getChildLayoutPosition(originalView);

        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new ContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    float lockedY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Timber.d("onInterceptTouchEvent: " + event.getActionMasked());
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lockedY = event.getRawY();
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Timber.d("event.getRawY(): " + event.getRawY());
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float rawY = event.getRawY();
            if (computeVerticalScrollOffset() == 0) {
                Timber.d("lockedY: " + lockedY);
                if (rawY > lockedY) {
                    setTranslationY(rawY - lockedY);
                    return true;
                } else {
                    lockedY = rawY;
                    setTranslationY(0);
                }
            } else {
                lockedY = rawY;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL ||
                event.getAction() == MotionEvent.ACTION_UP) {
            setTranslationY(0);
        }
        return super.onTouchEvent(event);
    }

    public static class ContextMenuInfo implements ContextMenu.ContextMenuInfo {

        ContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}