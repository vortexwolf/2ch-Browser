package com.vortexwolf.chan.activities;

import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.FavoritesAdapter;
import com.vortexwolf.chan.adapters.HistoryAdapter;
import com.vortexwolf.chan.adapters.OpenTabsAdapter;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.FavoritesEntity;
import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.db.HistoryEntity;
import com.vortexwolf.chan.interfaces.IOpenTabsManager;
import com.vortexwolf.chan.models.presentation.OpenTabModel;
import com.vortexwolf.chan.services.NavigationService;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class TabsHistoryBookmarksCompActivity extends Activity {    
    private FrameLayout layout;
    private Uri mCurrentUri;
    private static final String TAG = "TabsHistoryBookmarksActivityC";
    private final ApplicationSettings mApplicationSettings = Factory.resolve(ApplicationSettings.class);
    
    private HistoryAdapter historyAdapter;
    private HistoryDataSource historyDatasource;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        layout = new FrameLayout(this);
        
        Bundle extras = this.getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.EXTRA_CURRENT_URL)) {
            this.mCurrentUri = Uri.parse(extras.getString(Constants.EXTRA_CURRENT_URL));
        }

        this.setTheme(mApplicationSettings.getTheme());
        
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setContentView(layout, params);
        layout.setBackgroundColor(AppearanceUtils.getThemeColor(this.getTheme(), R.styleable.Theme_activityRootBackground));
        
        openTabs();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    void openTabs() {
        setTitle(getString(R.string.tabs_opentabs));
        
        final IOpenTabsManager mTabsManager = Factory.getContainer().resolve(IOpenTabsManager.class);
        final OpenTabsAdapter mAdapter = new OpenTabsAdapter(this, mTabsManager.getOpenTabs(), mTabsManager);
        
        ListView view = new ListView(this);
        view.setAdapter(mAdapter);
        view.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OpenTabModel item = mAdapter.getItem(position);
                if (item.getUri().equals(TabsHistoryBookmarksCompActivity.this.mCurrentUri)) {
                    TabsHistoryBookmarksCompActivity.this.finish();
                } else {
                    mTabsManager.navigate(item, TabsHistoryBookmarksCompActivity.this);
                }
            }
        });
        layout.addView(view);

    }
    
    void openFavorites() {
        setTitle(getString(R.string.tabs_bookmarks));
        
        final FavoritesDataSource mDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        final NavigationService mNavigationService = Factory.getContainer().resolve(NavigationService.class);
        final FavoritesAdapter mAdapter = new FavoritesAdapter(this, mDatasource);
        
        List<FavoritesEntity> favorites = mDatasource.getAllFavorites();
        mAdapter.clear();
        for (FavoritesEntity item : favorites) {
            mAdapter.add(item);
        }
        
        ListView view = new ListView(this);
        view.setAdapter(mAdapter);
        view.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FavoritesEntity item = mAdapter.getItem(position);
                mNavigationService.navigate(Uri.parse(item.getUrl()), TabsHistoryBookmarksCompActivity.this);
            }
        });

        layout.addView(view);
    }
    
    void openHistory() {
        setTitle(getString(R.string.tabs_history));
        
        final FavoritesDataSource mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        final NavigationService mNavigationService = Factory.getContainer().resolve(NavigationService.class);
        historyDatasource = Factory.getContainer().resolve(HistoryDataSource.class);
        historyAdapter = new HistoryAdapter(this, mFavoritesDatasource);
        
        List<HistoryEntity> historyItems = historyDatasource.getAllHistory();
        historyAdapter.clear();
        for (HistoryEntity item : historyItems) {
            historyAdapter.add(item);
        }

        ListView view = new ListView(this);
        registerForContextMenu(view);
        view.setAdapter(historyAdapter);
        view.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryEntity item = historyAdapter.getItem(position);
                mNavigationService.navigate(Uri.parse(item.getUrl()), TabsHistoryBookmarksCompActivity.this);
            }
        });
        view.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                menu.add(Menu.NONE, 4, 0, TabsHistoryBookmarksCompActivity.this.getString(R.string.menu_clear_history));
            }
        });
        
        layout.addView(view);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 4: {
                historyDatasource.deleteAllHistory();
                historyAdapter.clear();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(Menu.NONE, 1, 0, getString(R.string.tabs_opentabs));
      menu.add(Menu.NONE, 2, 0, getString(R.string.tabs_bookmarks));
      menu.add(Menu.NONE, 3, 0, getString(R.string.tabs_history));
      
      return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        layout.removeAllViews();
        switch (item.getItemId()) {
            case 1:
                openTabs();
                break;
            case 2:
                openFavorites();
                break;
            case 3:
                openHistory();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
