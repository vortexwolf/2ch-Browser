package ua.in.quireg.chan.views.controls;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class SelectiveViewPager extends ViewPager {
    private static final String LOG_TAG = SelectiveViewPager.class.getSimpleName();

    private GestureDetectorCompat gestureDetector;

    private OnSingleClickListener mViewPagerSingleClickListener;

    public interface OnSingleClickListener {
        void onSingleClick();
    }

    public SelectiveViewPager(Context context) {
        super(context);
        init();
    }

    public SelectiveViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }


    private void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                mViewPagerSingleClickListener.onSingleClick();
                return false;
            }
        });
    }

    public void setSingleClickListener(OnSingleClickListener viewPagerSingleClickListener) {
        mViewPagerSingleClickListener = viewPagerSingleClickListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

}