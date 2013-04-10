package com.vortexwolf.dvach.activities;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.FoundPostsListAdapter;
import com.vortexwolf.dvach.adapters.PostsListAdapter;
import com.vortexwolf.dvach.asynctasks.SearchPostsTask;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IListView;
import com.vortexwolf.dvach.interfaces.IPostsListView;
import com.vortexwolf.dvach.models.domain.FoundPostsList;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.domain.PostsList;
import com.vortexwolf.dvach.models.domain.ThreadsList;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.services.presentation.ListViewScrollListener;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class SearchableActivity extends BaseListActivity {
    private static final String TAG = "SearchableActivity";
    
    private final IJsonApiReader mJsonReader = Factory.getContainer().resolve(IJsonApiReader.class);
    private final IBitmapManager mBitmapManager = Factory.getContainer().resolve(IBitmapManager.class);
    private final ApplicationSettings mApplciationSettings = Factory.getContainer().resolve(ApplicationSettings.class);
    private final DvachUriBuilder mDvachUriBuilder = Factory.getContainer().resolve(DvachUriBuilder.class);
    private final FoundPostsListener mFoundPostsListener = new FoundPostsListener();
    
    private FoundPostsListAdapter mAdapter = null;
    private SearchPostsTask mCurrentTask = null;
    
    private String mBoardName = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.resetUI();
        
        this.handleIntent(this.getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
        this.handleIntent(intent);
    }
    
    @Override
    public boolean onSearchRequested() {
        Bundle data = new Bundle();
        data.putString(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        
        this.startSearch(null, false, data, false);
        
        return true;
    }
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Bundle b = intent.getBundleExtra(SearchManager.APP_DATA);
            this.mBoardName = b.getString(Constants.EXTRA_BOARD_NAME);
            
            this.setAdapter(this.mBoardName);
            this.doSearch(query, this.mBoardName);
        }
    }
    
    private void setAdapter(String boardName) {
        this.mAdapter = new FoundPostsListAdapter(this, boardName, this.mBitmapManager, this.mApplciationSettings, this.getTheme(), this.mDvachUriBuilder);
        this.setListAdapter(this.mAdapter);

        if (Integer.valueOf(Build.VERSION.SDK) > 7) {
            this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }
    }
    
    private void doSearch(String searchQuery, String boardName){
        MyLog.v(TAG, "doSearch");
        
        if(this.mCurrentTask != null) {
            this.mCurrentTask.cancel(true);
        }
        
        this.setTitle(this.getString(R.string.data_search_posts_title, searchQuery));
        
        this.mCurrentTask = new SearchPostsTask(boardName, searchQuery, this.mJsonReader, this.mFoundPostsListener);
        this.mCurrentTask.execute();
    }

    @Override
    protected int getLayoutId() {
        return com.vortexwolf.dvach.R.layout.search_posts_list_view;
    }
    
    private class FoundPostsListener implements IListView<FoundPostsList> {

        @Override
        public Context getApplicationContext() {
            return SearchableActivity.this.getApplicationContext();
        }

        @Override
        public void setWindowProgress(int value) {
            SearchableActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, value);
        }

        @Override
        public void setData(FoundPostsList postsList) {
            if (postsList != null && postsList.getErrorText() == null) {
                PostInfo[] posts = postsList.getPosts();
                SearchableActivity.this.mAdapter.setAdapterData(posts);
            } else {
                SearchableActivity.this.mAdapter.clear();
                String error = postsList.getErrorText() != null 
                        ? postsList.getErrorText() 
                        : SearchableActivity.this.getString(R.string.error_list_empty);
                this.showError(error);
            }
        }

        @Override
        public void showError(String error) {
            SearchableActivity.this.switchToErrorView(error);
        }

        @Override
        public void showLoadingScreen() {
            SearchableActivity.this.switchToLoadingView();
        }

        @Override
        public void hideLoadingScreen() {
            SearchableActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_OFF);
            SearchableActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);
            
            SearchableActivity.this.switchToListView();
            SearchableActivity.this.mCurrentTask = null;
        }
    }
}
