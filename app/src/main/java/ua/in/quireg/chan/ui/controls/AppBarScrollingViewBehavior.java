package ua.in.quireg.chan.ui.controls;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.MotionEvent;
import android.view.View;

public class AppBarScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {
    private int counter = 0;
    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        counter++;
        if(counter % 2 == 0){
            return true;
        }
        return false;
    }
}
