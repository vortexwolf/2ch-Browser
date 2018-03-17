package ua.in.quireg.chan.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.aspsine.swipetoloadlayout.SwipeRefreshTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;

import ua.in.quireg.chan.R;

/**
 * Created by Arcturus Mengsk on 3/11/2018, 11:40 AM.
 * 2ch-Browser
 */

@SuppressWarnings("FieldCanBeLocal")
public class RefreshHeaderView extends LinearLayout implements SwipeRefreshTrigger, SwipeTrigger {

    private TextView mTextView;
    private ProgressBar mProgressBar;

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

    private void init(){
        mProgressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);

        mTextView = new TextView(getContext());
        mTextView.setText(R.string.pull_to_refresh);
        mTextView.setPadding(
                getResources().getDimensionPixelSize(R.dimen.pull_to_refresh_text_padding),
                0,
                getResources().getDimensionPixelSize(R.dimen.pull_to_refresh_text_padding),
                0
        );

        addView(mTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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

    }

//    boolean readyToRefresh = false;

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
//        readyToRefresh = false;
    }

    @Override
    public void onComplete() {
//        readyToRefresh = false;
//        mProgressBar.setVisibility(GONE);
    }


    @Override
    public void onReset() {
//        readyToRefresh = false;
//        mProgressBar.setVisibility(GONE);
//        mTextSwitcher.setText("SWIPE TO REFRESH");
//        Timber.d("SWIPE TO REFRESH");
    }
}
