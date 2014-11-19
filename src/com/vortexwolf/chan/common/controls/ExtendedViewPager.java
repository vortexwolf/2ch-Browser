package com.vortexwolf.chan.common.controls;

import java.lang.reflect.Field;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.MyLog;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ExtendedViewPager extends ViewPager {
    public ExtendedViewPager(Context context) {
        super(context);
    }

    public ExtendedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * Hacky fix for Issue #4 and
     * http://code.google.com/p/android/issues/detail?id=18990
     * <p/>
     * ScaleGestureDetector seems to mess up the touch events, which means that
     * ViewGroups which make use of onInterceptTouchEvent throw a lot of
     * IllegalArgumentException: pointerIndex out of range.
     * <p/>
     * There's not much I can do in my code for now, but we can mask the result by
     * just catching the problem and ignoring it.
     *
     * @author Chris Banes
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            MyLog.e("ExtendedViewPager", e);
            return false;
        }
    }
    
    public void movePrevious() {
        int currentItem = this.getCurrentItem();
        if (currentItem > 0) {
            this.setCurrentItem(currentItem - 1, false);
        }
    }

    public void moveNext() {
        int currentItem = this.getCurrentItem();
        if (currentItem < this.getAdapter().getCount() - 1) {
            this.setCurrentItem(currentItem + 1, false);
        }
    }
    
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (Constants.SDK_VERSION < 14 && v instanceof WebViewFixed) {
            return ((WebViewFixed) v).canScrollHorizontallyOldAPI(-dx);
        } else if (Constants.SDK_VERSION < 14 && v instanceof TouchGifView) {
            return ((TouchGifView) v).canScrollHorizontallyOldAPI(-dx);
        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }
}