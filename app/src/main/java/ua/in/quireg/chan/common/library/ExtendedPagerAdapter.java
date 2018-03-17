package ua.in.quireg.chan.common.library;

import java.util.ArrayList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public abstract class ExtendedPagerAdapter<T> extends PagerAdapter {
    private final Context mContext;
    private final ArrayList<T> mModels;
    private final View[] mViews;

    private boolean mLoadFirstTime = true;
    private boolean mSubscribedToPager = false;
    private int mPreviousPosition = -1;

    public ExtendedPagerAdapter(Context context, ArrayList<T> models) {
        mContext = context;
        mModels = models;
        mViews = new View[models.size() + 1]; // requires 1 extra item
    }

    public Context getContext() {
        return mContext;
    }

    public T getItem(int index) {
        return mModels.get(index);
    }

    public View getCreatedView(int position) {
        return mViews[position];
    }

    public void subscribeToPageChangeEvent(ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                notifyPositionChange(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSubscribedToPager = true;
    }

    @Override
    public int getCount() {
        return mModels.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = createView(position);
        mViews[position] = view;
        container.addView(view);

        if (mLoadFirstTime) {
            mLoadFirstTime = false;

            if (mSubscribedToPager) {
                notifyPositionChange(position);
            }
        }

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        mViews[position] = null;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    protected abstract View createView(int position);

    protected void onViewSelected(int position, View view) {
    }

    protected void onViewUnselected(int position, View view) {
    }

    private void notifyPositionChange(int position) {
        if (mViews[position] == null) {
            // shouldn't happen
            MyLog.w("ExtendedPageAdapter", "the view is null for the position " + position);
            return;
        }

        if (mPreviousPosition != -1 && mViews[mPreviousPosition] != null && mPreviousPosition != position) {
            onViewUnselected(mPreviousPosition, mViews[mPreviousPosition]);
        }
        mPreviousPosition = position;

        onViewSelected(position, mViews[position]);
    }
}
