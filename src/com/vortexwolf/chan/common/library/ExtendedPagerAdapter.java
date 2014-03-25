package com.vortexwolf.chan.common.library;

import java.util.ArrayList;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.models.presentation.ThreadImageModel;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public abstract class ExtendedPagerAdapter<T> extends PagerAdapter {
    private final Context mContext;
    private final ArrayList<T> mModels;
    private final View[] mViews;
    
    private boolean mLoadFirstTime = true;
    private boolean mSubscribedToPager = false;
    private int mPreviousPosition = -1;
        
    public ExtendedPagerAdapter(Context context, ArrayList<T> models) {
        this.mContext = context;
        this.mModels = models;
        this.mViews = new View[models.size() + 1]; // requires 1 extra item
    }
    
    public Context getContext() {
        return this.mContext;
    }
    
    public T getItem(int index) {
        return this.mModels.get(index);
    }
    
    public View getCreatedView(int position) {
        return this.mViews[position];
    }
    
    public void subscribeToPageChangeEvent(ViewPager viewPager){
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                ExtendedPagerAdapter.this.notifyPositionChange(position);
            }
        });
        
        this.mSubscribedToPager = true;
    }
        
    @Override
    public int getCount() {
        return this.mModels.size();
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = this.createView(position);
        this.mViews[position] = view;
        container.addView(view);
        
        if (this.mLoadFirstTime) {
            this.mLoadFirstTime = false;
            
            if (this.mSubscribedToPager) {
                this.notifyPositionChange(position);
            }
        }
        
        return view;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
        this.mViews[position] = null;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    protected abstract View createView(int position);
    protected void onViewSelected(int position, View view) {}
    protected void onViewUnselected(int position, View view) {}
    
    private void notifyPositionChange(int position) {
        if (this.mViews[position] == null) {
            // shouldn't happen
            MyLog.w("ExtendedPageAdapter", "the view is null for the position " + position);
            return;
        }
        
        if (this.mPreviousPosition != -1 && this.mViews[this.mPreviousPosition] != null && this.mPreviousPosition != position) {
            this.onViewUnselected(this.mPreviousPosition, this.mViews[this.mPreviousPosition]);
        }
        this.mPreviousPosition = position;

        this.onViewSelected(position, this.mViews[position]);
    }
}
