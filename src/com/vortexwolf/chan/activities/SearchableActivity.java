package com.vortexwolf.chan.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.FoundPostsListAdapter;
import com.vortexwolf.chan.asynctasks.SearchPostsTask;
import com.vortexwolf.chan.boards.dvach.DvachApiReader;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.makaba.MakabaApiReader;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.interfaces.IBitmapManager;
import com.vortexwolf.chan.interfaces.ICloudflareListener;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IListView;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.services.CloudflareCheckService;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.presentation.ListViewScrollListener;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class SearchableActivity extends BaseListActivity {
    private static final String TAG = "SearchableActivity";

    private IJsonApiReader mJsonReader;
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

        if (ThreadPostUtils.isMakabaBoard(Constants.EXTRA_BOARD_NAME)) {
            this.mJsonReader = Factory.resolve(MakabaApiReader.class);
        } else {
            this.mJsonReader = Factory.resolve(DvachApiReader.class);
        }
        
        this.resetUI();

        this.handleIntent(this.getIntent());

        Factory.getContainer().resolve(MyTracker.class).setBoardVar(this.mBoardName);
        Factory.getContainer().resolve(MyTracker.class).trackActivityView(TAG);
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
                this.refresh();
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
                String text = info.getSpannedComment().toString();
                CompatibilityUtils.copyText(this, info.getNumber(), text);

                AppearanceUtils.showToastMessage(this, this.getString(R.string.notification_post_copied));
                break;
        }

        return true;
    }

    private void navigateToThreads(String boardName) {
        Intent i = new Intent(this.getApplicationContext(), ThreadsListActivity.class);
        i.setData(this.mDvachUriBuilder.createBoardUri(boardName, 0));
        this.startActivity(i);
    }

    private void navigateToThread(String threadNumber, String postNumber) {
        Intent i = new Intent(this.getApplicationContext(), PostsListActivity.class);
        i.setData(Uri.parse(this.mDvachUriBuilder.createPostUri(this.mBoardName, threadNumber, postNumber)));

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
            this.refresh();
        }
    }

    private void setAdapter(String boardName) {
        this.mAdapter = new FoundPostsListAdapter(this, boardName, this.mBitmapManager, this.mApplciationSettings, this.getTheme(), this.mDvachUriBuilder);
        this.setListAdapter(this.mAdapter);

        if (Integer.valueOf(Build.VERSION.SDK) > 7) {
            this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }
    }

    protected void refresh() {
        if (this.mCurrentTask != null) {
            this.mCurrentTask.cancel(true);
        }

        this.setTitle(this.getString(R.string.data_search_posts_title, this.mBoardName, this.mSearchQuery));

        this.mCurrentTask = new SearchPostsTask(this.mBoardName, this.mSearchQuery, this.mJsonReader, this.mFoundPostsListener);
        this.mCurrentTask.execute();
    }

    @Override
    protected int getLayoutId() {
        return com.vortexwolf.chan.R.layout.search_posts_list_view;
    }

    private class FoundPostsListener implements IListView<SearchPostListModel> {

        @Override
        public Context getApplicationContext() {
            return SearchableActivity.this.getApplicationContext();
        }

        @Override
        public void setWindowProgress(int value) {
            SearchableActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, value);
        }

        @Override
        public void setData(SearchPostListModel postsListModel) {
            if (postsListModel == null) {
                return;
            }
            
            PostModel[] posts = postsListModel.getPosts();
            if (posts != null) {
                SearchableActivity.this.mAdapter.setAdapterData(posts);
            } else {
                SearchableActivity.this.mAdapter.clear();
                String error = postsListModel.getError() != null
                        ? postsListModel.getError()
                        : SearchableActivity.this.getString(R.string.error_list_empty);
                this.showError(error);
            }
        }

        @Override
        public void showError(String error) {
        	SearchableActivity.this.switchToErrorView(error);
        	if (error != null && error.startsWith("503")) {
        		String url = mDvachUriBuilder.createUri("/makaba/posting.fcgi").toString();
        		new CloudflareCheckService(url, SearchableActivity.this, new ICloudflareListener(){
					public void success() {
						refresh();
					}
        		}, this).start();
        	}
        }
        
        @Override
        public void showCaptcha(CaptchaEntity captcha) {
        	// TODO: replace by captcha view
        	this.showError("Cloudflare captcha, open any board first.");
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
