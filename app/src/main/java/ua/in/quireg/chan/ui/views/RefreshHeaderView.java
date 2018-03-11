package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;

import com.aspsine.swipetoloadlayout.SwipeRefreshTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;

import timber.log.Timber;

/**
 * Created by Arcturus Mengsk on 3/11/2018, 11:40 AM.
 * 2ch-Browser
 */

public class RefreshHeaderView extends LinearLayout implements SwipeRefreshTrigger, SwipeTrigger {

    public RefreshHeaderView(Context context) {
        super(context);
        init();
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private TextView mTextView;
    private TextSwitcher mTextSwitcher;
    private ProgressBar mProgressBar;

    private ViewSwitcher.ViewFactory mViewFactory = () -> new TextView(getContext());

    private void init(){

        mProgressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
//        mProgressBar.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
        mTextView = new TextView(getContext());

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        mTextView.setPadding(padding, 0, padding, 0);
        mTextView.setSingleLine(true);

        mTextView.setText("REFRESHING");

        mTextSwitcher = new TextSwitcher(getContext());
        mTextSwitcher.setFactory(mViewFactory);
//        mTextSwitcher.setInAnimation(getContext(), android.R.anim.fade_in);
//        mTextSwitcher.setOutAnimation(getContext(), android.R.anim.fade_out);
//        mTextSwitcher.setPadding(padding, 0, padding, 0);

        addView(mTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//        addView(mTextSwitcher, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(mProgressBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    }


    @Override
    public void onRefresh() {
//        mProgressBar.setVisibility(VISIBLE);
//        mTextView.setText("REFRESHING");
//        Timber.d("REFRESHING");

    }

    @Override
    public void onPrepare() {
//        mProgressBar.setVisibility(GONE);
//        mTextView.setText("SWIPE TO REFRESH");
//        Timber.d("SWIPE TO REFRESH");
    }

    boolean readyToRefresh = false;

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
//        if (!isComplete) {
//            if (yScrolled >= getHeight() && !readyToRefresh) {
//                mTextView.setText("RELEASE TO REFRESH");
//                Timber.d("RELEASE TO REFRESH");
//                readyToRefresh = true;
//
//            } else if (yScrolled <= getHeight() && readyToRefresh) {
//                mTextView.setText("SWIPE TO REFRESH");
//                Timber.d("SWIPE TO REFRESH");
//                readyToRefresh = false;
//            }
//        } else {
////            mTextView.setText("ENJOY");
//        }
    }

    @Override
    public void onRelease() {
        readyToRefresh = false;
    }

    @Override
    public void onComplete() {
        readyToRefresh = false;
    }

    @Override
    public void onReset() {
        readyToRefresh = false;
//        mProgressBar.setVisibility(GONE);
//        mTextSwitcher.setText("SWIPE TO REFRESH");
//        Timber.d("SWIPE TO REFRESH");
    }
}
