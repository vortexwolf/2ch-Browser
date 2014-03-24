package com.vortexwolf.chan.activities;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.MainApplication;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.services.MyTracker;

public class TabsHistoryBookmarksActivity extends FragmentActivity  {
    private static final String TAG = "TabsHistoryBookmarksActivity";
    
    public static final String FAVORITES_TAB_ID = "FavoritesTab";
    
    private TabsPagerAdapter mAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        
        MainApplication application = (MainApplication) this.getApplication();
        this.setTheme(application.getSettings().getTheme());
        this.setContentView(R.layout.tabs_favorites_history_view);

        this.mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this.getResources());
        ViewPager viewPager = (ViewPager) findViewById(R.id.tabs_pager);
        viewPager.setAdapter(this.mAdapter);
        
        Bundle extras = this.getIntent().getExtras();
        if (extras.containsKey(Constants.EXTRA_SELECT_TAB) && extras.getString(Constants.EXTRA_SELECT_TAB).equals(FAVORITES_TAB_ID)) {
            viewPager.setCurrentItem(1);
        }

        MyTracker tracker = Factory.getContainer().resolve(MyTracker.class);
        tracker.trackActivityView(TAG);
    }
    
    public class TabsPagerAdapter extends FragmentStatePagerAdapter {
        private final Resources mResources;
        
        public TabsPagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            this.mResources = resources;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    fragment = new OpenTabsFragment();
                    fragment.setArguments(getIntent().getExtras());
                    break;
                case 1:
                    fragment = new FavoritesFragment(); 
                    break;
                case 2:
                    fragment = new HistoryFragment();
                    break;
            }
            
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return this.mResources.getString(R.string.tabs_opentabs);
                case 1:
                    return this.mResources.getString(R.string.tabs_bookmarks);
                case 2:
                    return this.mResources.getString(R.string.tabs_history);
            }
            
            return null;
        }

    }
}
