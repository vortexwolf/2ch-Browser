package ua.in.quireg.chan.ui.activities;

import android.app.SearchManager;
import android.content.Intent;
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

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.adapters.FoundPostsListAdapter;
import ua.in.quireg.chan.asynctasks.SearchPostsTask;
import ua.in.quireg.chan.boards.makaba.MakabaApiReader;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.interfaces.ICloudflareCheckListener;
import ua.in.quireg.chan.interfaces.IJsonApiReader;
import ua.in.quireg.chan.interfaces.IListView;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.SearchPostListModel;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.services.CloudflareCheckService;
import ua.in.quireg.chan.services.NavigationService;
import ua.in.quireg.chan.services.presentation.ListViewScrollListener;

public class SearchableActivity extends BaseListActivity {
    private static final String TAG = "SearchableActivity";

    private IJsonApiReader mJsonReader;
    private final FoundPostsListener mFoundPostsListener = new FoundPostsListener();
    private final NavigationService mNavigationService = Factory.resolve(NavigationService.class);
    private IUrlBuilder mUrlBuilder;

    private FoundPostsListAdapter mAdapter = null;
    private SearchPostsTask mCurrentTask = null;

    private String mSearchQuery = null;
    private String mBoardName = null;
    private IWebsite mWebsite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mJsonReader = Factory.resolve(MakabaApiReader.class);

        this.resetUI();

        this.handleIntent(this.getIntent());
        this.mUrlBuilder = this.mWebsite.getUrlBuilder();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
        this.handleIntent(intent);
    }

    @Override
    protected void resetUI() {
        super.resetUI();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.registerForContextMenu(this.getListView());
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
                this.navigateToThreads();
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

    private void navigateToThreads() {
        //this.mNavigationService.navigateBoard(this, this.mWebsite.name(), this.mBoardName, 0, false);
    }

    private void navigateToThread(String threadNumber, String postNumber) {
        //this.mNavigationService.navigateThread(this, null, this.mWebsite.name(), this.mBoardName, threadNumber, null, postNumber, false);
    }

    private void navigateToAddPostView(String postNumber, String threadNumber, String postComment) {
        Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, this.mWebsite.name());
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
            this.mWebsite = Websites.fromName(b.getString(Constants.EXTRA_WEBSITE));
            if (this.mWebsite == null) {
                this.mWebsite = Websites.getDefault();
            }
            this.mBoardName = b.getString(Constants.EXTRA_BOARD_NAME);

            this.setAdapter(this.mBoardName);
            this.refresh();
        }
    }

    private void setAdapter(String boardName) {
        this.mAdapter = new FoundPostsListAdapter(this, this.mWebsite, boardName, this.getTheme());
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
        return ua.in.quireg.chan.R.layout.search_posts_list_view;
    }

    private class FoundPostsListener implements IListView<SearchPostListModel> {

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
                String url = mUrlBuilder.getPostingUrlHtml();
                new CloudflareCheckService(url, SearchableActivity.this, new ICloudflareCheckListener(){
                    public void onSuccess() {
                        refresh();
                    }
                    public void onStart() {
                        showError(getString(R.string.notification_cloudflare_check_started));
                    }
                    public void onTimeout() {
                        showError(getString(R.string.error_cloudflare_check_timeout));
                    }
                }).start();
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
