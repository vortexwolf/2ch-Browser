package com.vortexwolf.chan.common.controls;

import java.lang.reflect.Field;

import com.vortexwolf.chan.common.library.MyLog;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

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
        /*Class sc = this.getClass().getSuperclass();
        try {
            Field touchSlopField = sc.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            int touchSlop = touchSlopField.getInt(this);
            touchSlopField.setInt(this, touchSlop * 3);
        } catch (Exception e) {
            MyLog.e("ExtendedViewPager", e);
        }*/   
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
}