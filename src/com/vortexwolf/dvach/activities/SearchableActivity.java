package com.vortexwolf.dvach.activities;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.FoundPostsListAdapter;
import com.vortexwolf.dvach.adapters.PostsListAdapter;
import com.vortexwolf.dvach.asynctasks.DownloadFileTask;
import com.vortexwolf.dvach.asynctasks.SearchPostsTask;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.CompatibilityUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IListView;
import com.vortexwolf.dvach.interfaces.IPostsListView;
import com.vortexwolf.dvach.models.domain.FoundPostsList;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.domain.PostsList;
import com.vortexwolf.dvach.models.domain.ThreadsList;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.models.presentation.PostItemViewModel;
import com.vortexwolf.dvach.models.presentation.ThreadItemViewModel;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.Tracker;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.services.presentation.ListViewScrollListener;
import com.vortexwolf.dvach.settings.ApplicationPreferencesActivity;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
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
    
    private String mSearchQuery = null;
    private String mBoardName = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.resetUI();
        
        this.handleIntent(this.getIntent());
        
        Factory.getContainer().resolve(Tracker.class).trackActivityView(TAG);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
        this.handleIntent(intent);
    }
    
    @Override
    protected void resetUI() {
        super.resetUI();

        CompatibilityUtils.setDisplayHomeAsUpEnabled(this);
        this.registerForContextMenu(this.getListView());
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        AppearanceUtils.showToastMessage(this, "Item clicked");
    }
    
    @Override
    public boolean onSearchRequested() {
        Bundle data = new Bundle();
        data.putString(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        
        this.startSearch(this.mSearchQuery, true, data, false);
        
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.foundposts, menu);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.navigateToThreads(this.mBoardName);
                break;
            case R.id.menu_search_id:
                this.onSearchRequested();
                break;
            case R.id.refresh_menu_id:
                this.searchAndLoadList();
                break;
        }

        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        PostItemViewModel item = this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_OPEN_THREAD, 0, this.getString(R.string.cmenu_open_thread));
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST, 0, this.getString(R.string.cmenu_reply_post));
        if (!StringUtils.isEmpty(item.getSpannedComment().toString())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST_QUOTE, 1, this.getString(R.string.cmenu_reply_post_quote));
        }
        if (!StringUtils.isEmpty(item.getSpannedComment())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_TEXT, 2, this.getString(R.string.cmenu_copy_post));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PostItemViewModel info = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_OPEN_THREAD:
                this.navigateToThread(info.getParentThreadNumber(), info.getNumber());
                break;
            case Constants.CONTEXT_MENU_REPLY_POST:
                this.navigateToAddPostView(info.getNumber(), info.getParentThreadNumber(), null);
                break;
            case Constants.CONTEXT_MENU_REPLY_POST_QUOTE:
                this.navigateToAddPostView(info.getNumber(), info.getParentThreadNumber(), info.getSpannedComment().toString());
                break;
            case Constants.CONTEXT_MENU_COPY_TEXT:
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(info.getSpannedComment().toString());

                AppearanceUtils.showToastMessage(this, this.getString(R.string.notification_post_copied));
                break;
        }

        return true;
    }
    
    private void navigateToThreads(String boardName) {
        Intent i = new Intent(this.getApplicationContext(), ThreadsListActivity.class);
        i.setData(this.mDvachUriBuilder.create2chBoardUri(boardName, 0));
        this.startActivity(i);
    }
    
    private void navigateToThread(String threadNumber, String postNumber) {
        Intent i = new Intent(this.getApplicationContext(), PostsListActivity.class);
        i.setData(Uri.parse(this.mDvachUriBuilder.create2chThreadUrl(this.mBoardName, threadNumber, postNumber)));

        this.startActivity(i);
    }
    
    private void navigateToAddPostView(String postNumber, String threadNumber, String postComment) {
        Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, threadNumber);

        if (postNumber != null) {
            addPostIntent.putExtra(Constants.EXTRA_POST_NUMBER, postNumber);
        }
        if (postComment != null) {
            addPostIntent.putExtra(Constants.EXTRA_POST_COMMENT, postComment);
        }

        this.startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            this.mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
            Bundle b = intent.getBundleExtra(SearchManager.APP_DATA);
            this.mBoardName = b.getString(Constants.EXTRA_BOARD_NAME);
            
            this.setAdapter(this.mBoardName);
            this.searchAndLoadList();
        }
    }
    
    private void setAdapter(String boardName) {
        this.mAdapter = new FoundPostsListAdapter(this, boardName, this.mBitmapManager, this.mApplciationSettings, this.getTheme(), this.mDvachUriBuilder);
        this.setListAdapter(this.mAdapter);

        if (Integer.valueOf(Build.VERSION.SDK) > 7) {
            this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }
    }
    
    private void searchAndLoadList(){
        if(this.mCurrentTask != null) {
            this.mCurrentTask.cancel(true);
        }
        
        this.setTitle(this.getString(R.string.data_search_posts_title, this.mBoardName, this.mSearchQuery));
        
        this.mCurrentTask = new SearchPostsTask(this.mBoardName, this.mSearchQuery, this.mJsonReader, this.mFoundPostsListener);
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
