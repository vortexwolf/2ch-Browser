package com.vortexwolf.dvach.activities;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabsHistoryBookmarksActivity extends TabActivity {
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
    }
}
