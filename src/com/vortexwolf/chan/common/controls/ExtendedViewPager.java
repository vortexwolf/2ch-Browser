package com.vortexwolf.chan.common.controls;

import com.vortexwolf.chan.common.Constants;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class ExtendedViewPager extends ViewPager {
    public ExtendedViewPager(Context context) {
        super(context);
        this.init();
    }

    public ExtendedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    private void init() {
        /* Class sc = this.getClass().getSuperclass(); try { Field
         * touchSlopField = sc.getDeclaredField("mTouchSlop");
         * touchSlopField.setAccessible(true); int touchSlop =
         * touchSlopField.getInt(this); touchSlopField.setInt(this, touchSlop *
         * 3); } catch (Exception e) { MyLog.e("ExtendedViewPager", e); } */
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
        } else if (v instanceof TouchGifView) {
            return ((TouchGifView) v).canScrollHorizontallyOldAPI(-dx);
        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }
}