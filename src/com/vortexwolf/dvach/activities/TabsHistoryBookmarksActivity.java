package com.vortexwolf.dvach.activities;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.CompatibilityUtils;
import com.vortexwolf.dvach.services.Tracker;

public class TabsHistoryBookmarksActivity extends TabActivity {
    private static final String TAG = "TabsHistoryBookmarksActivity";
    private Menu mSoftwareMenu;
    private boolean mIsSoftwareMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication application = (MainApplication) this.getApplication();
        this.setTheme(application.getSettings().getTheme());

        TabHost tabHost = this.getTabHost();
        Resources res = this.getResources();

        Bundle extras = this.getIntent().getExtras();

        Intent intent = new Intent(this, OpenTabsActivity.class);
        intent.putExtras(extras);
        tabHost.addTab(tabHost.newTabSpec("tabs").setIndicator(res.getString(R.string.tabs_opentabs), res.getDrawable(R.drawable.browser_visited_tab)).setContent(intent));

        intent = new Intent(this, FavoritesActivity.class);
        intent.putExtras(extras);
        tabHost.addTab(tabHost.newTabSpec("bookmarks").setIndicator(res.getString(R.string.tabs_bookmarks), res.getDrawable(R.drawable.browser_bookmark_tab)).setContent(intent));

        intent = new Intent(this, HistoryActivity.class);
        intent.putExtras(extras);
        tabHost.addTab(tabHost.newTabSpec("history").setIndicator(res.getString(R.string.tabs_history), res.getDrawable(R.drawable.browser_history_tab)).setContent(intent));

        // devices without hardware menu button work differently with tabs, so I add extra code so that the menu is updated properly
        this.mIsSoftwareMenu = !CompatibilityUtils.hasHardwareMenu(this.getApplicationContext());

        if (this.mIsSoftwareMenu) {
            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String arg0) {
                    TabsHistoryBookmarksActivity.this.updateMenu(TabsHistoryBookmarksActivity.this.mSoftwareMenu);
                }
            });
        }
        
        Tracker tracker = Factory.getContainer().resolve(Tracker.class);
        tracker.clearBoardVar();
        tracker.trackActivityView(TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.history, menu);

        this.updateMenu(menu);
        if (this.mIsSoftwareMenu) {
            this.mSoftwareMenu = menu;
        }

        return true;
    }

    private void updateMenu(Menu menu) {
        MenuItem clearHistory = menu.findItem(R.id.menu_clear_history_id);
        Activity currentActivity = this.getCurrentActivity();

        // the clear history menu item is visible only for the history tab
        if (currentActivity instanceof HistoryActivity) {
            clearHistory.setVisible(true);
        } else {
            clearHistory.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mIsSoftwareMenu) {
            return this.getCurrentActivity().onOptionsItemSelected(item);
        }

        return true;
    }
}
