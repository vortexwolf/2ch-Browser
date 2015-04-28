package com.vortexwolf.chan.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.PostsListAdapter;
import com.vortexwolf.chan.asynctasks.DownloadFileListTask;
import com.vortexwolf.chan.asynctasks.DownloadFileTask;
import com.vortexwolf.chan.asynctasks.DownloadPostsTask;
import com.vortexwolf.chan.boards.makaba.MakabaApiReader;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IPostsListView;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.models.presentation.IPostListEntity;
import com.vortexwolf.chan.models.presentation.OpenTabModel;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.models.presentation.PostsViewModel;
import com.vortexwolf.chan.models.presentation.StatusIndicatorEntity;
import com.vortexwolf.chan.models.presentation.ThreadImageModel;
import com.vortexwolf.chan.models.presentation.ThreadItemViewModel;
import com.vortexwolf.chan.services.BrowserLauncher;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.NavigationService;
import com.vortexwolf.chan.services.ThreadImagesService;
import com.vortexwolf.chan.services.TimerService;
import com.vortexwolf.chan.services.presentation.ListViewScrollListener;
import com.vortexwolf.chan.services.presentation.OpenTabsManager;
import com.vortexwolf.chan.services.presentation.PagesSerializationService;
import com.vortexwolf.chan.services.presentation.PostItemViewBuilder;
import com.vortexwolf.chan.settings.ApplicationPreferencesActivity;
import com.vortexwolf.chan.settings.ApplicationSettings;
import com.vortexwolf.chan.settings.SettingsEntity;

import java.util.ArrayList;
import java.util.List;

public class PostsListActivity extends BaseListActivity {
    private static final String TAG = "PostsListActivity";

    private IJsonApiReader mJsonReader;
    private final MyTracker mTracker = Factory.resolve(MyTracker.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final PagesSerializationService mSerializationService = Factory.resolve(PagesSerializationService.class);
    private final FavoritesDataSource mFavoritesDatasource = Factory.resolve(FavoritesDataSource.class);
    private final HistoryDataSource mHistoryDataSource = Factory.resolve(HistoryDataSource.class);
    private final OpenTabsManager mOpenTabsManager = Factory.resolve(OpenTabsManager.class);
    private final ThreadImagesService mThreadImagesService = Factory.resolve(ThreadImagesService.class);
    private final NavigationService mNavigationService = Factory.resolve(NavigationService.class);

    private IUrlBuilder mUrlBuilder;
    private PostsListAdapter mAdapter = null;
    private DownloadPostsTask mCurrentDownloadTask = null;
    private TimerService mAutoRefreshTimer = null;
    private final PostsReaderListener mPostsReaderListener = new PostsReaderListener();

    private SettingsEntity mCurrentSettings;

    private OpenTabModel mTabModel;
    private IWebsite mWebsite;
    private String mBoardName;
    private String mThreadNumber;
    private String mPostNumber;

    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mCurrentSettings = this.mSettings.getCurrentSettings();

        // Парсим код доски и номер страницы
        Bundle extras = this.getIntent().getExtras();
        this.mWebsite = Websites.fromName(extras.getString(Constants.EXTRA_WEBSITE));
        this.mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
        this.mThreadNumber = extras.getString(Constants.EXTRA_THREAD_NUMBER);
        this.mPostNumber = extras.getString(Constants.EXTRA_POST_NUMBER);

        this.mUrlBuilder = this.mWebsite.getUrlBuilder();
        this.mJsonReader = Factory.resolve(MakabaApiReader.class);

        // Page title and new tab
        String pageSubject = extras != null
                ? StringUtils.nullIfEmpty(extras.getString(Constants.EXTRA_THREAD_SUBJECT))
                : null;

        OpenTabModel tabModel = new OpenTabModel(this.mWebsite, this.mBoardName, 0, this.mThreadNumber, pageSubject);
        this.mTabModel = this.mOpenTabsManager.add(tabModel);

        this.updateTitle(pageSubject);

        this.resetUI();

        this.setAdapter(savedInstanceState);

        final Runnable refreshTask = new Runnable() {
            @Override
            public void run() {
                MyLog.v(TAG, "Attempted to refresh");
                if (PostsListActivity.this.mCurrentDownloadTask == null) {
                    PostsListActivity.this.refresh();
                }
            }
        };

        this.mAutoRefreshTimer = new TimerService(this.mSettings.isAutoRefresh(), this.mSettings.getAutoRefreshInterval(), refreshTask, this);
        this.mAutoRefreshTimer.start();

        this.mTracker.setBoardVar(this.mBoardName);
        this.mTracker.trackActivityView(TAG);
    }

    @Override
    protected void onDestroy() {
        this.mAutoRefreshTimer.stop();

        String uri = this.mUrlBuilder.getThreadUrlHtml(this.mBoardName, this.mThreadNumber);
        this.mThreadImagesService.clearThreadImages(uri);

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        this.mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(this.getListView()));

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Проверяем изменение настроек
        SettingsEntity newSettings = this.mSettings.getCurrentSettings();
        if (this.mCurrentSettings.theme != newSettings.theme) {
            this.finish();
            Intent i = new Intent(this.getIntent());
            i.putExtra(Constants.EXTRA_PREFER_DESERIALIZED, true);
            this.startActivity(i);
            return;
        }

        if (this.mCurrentSettings.isDisplayDate != newSettings.isDisplayDate || this.mCurrentSettings.isLoadThumbnails != newSettings.isLoadThumbnails || this.mCurrentSettings.isLocalDate != newSettings.isLocalDate) {
            this.mAdapter.notifyDataSetChanged();
        }

        this.mAutoRefreshTimer.update(this.mSettings.isAutoRefresh(), this.mSettings.getAutoRefreshInterval());

        this.mCurrentSettings = newSettings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.posts_list_view;
    }

    @Override
    protected void resetUI() {
        super.resetUI();

        CompatibilityUtils.setDisplayHomeAsUpEnabled(this);
        this.registerForContextMenu(this.getListView());
    }

    private void setAdapter(Bundle savedInstanceState) {
        this.mAdapter = new PostsListAdapter(this, this.mWebsite, this.mBoardName, this.mThreadNumber, this.getTheme(), this.getListView());
        this.setListAdapter(this.mAdapter);

        // добавляем обработчик, чтобы не рисовать картинки во время прокрутки
        if (Constants.SDK_VERSION > 7) {
            this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }

        boolean preferDeserialized = this.getIntent().hasExtra(Constants.EXTRA_PREFER_DESERIALIZED) || savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED);

        LoadPostsTask task = new LoadPostsTask(preferDeserialized);
        task.execute();
    }

    private void setAdapterData(PostModel[] posts) {
        boolean isFirstTime = this.mAdapter.isEmpty();

        this.mAdapter.setAdapterData(posts);
        if (posts.length > 0) {
            PostItemViewModel firstModel = (PostItemViewModel)this.mAdapter.getItem(0);
            this.updateTitle(firstModel.getSubjectOrText());
        }

        if (isFirstTime) {
            if (this.mPostNumber != null) {
                this.mAdapter.scrollToPost(this.mPostNumber);
            } else {
                // Устанавливаем позицию, если открываем как уже открытую вкладку
                AppearanceUtils.ListViewPosition savedPosition = this.mTabModel.getPosition();
                if (savedPosition != null) {
                    this.getListView().setSelectionFromTop(savedPosition.position, savedPosition.top);
                }
            }
        }
    }

    private void updateTitle(String title) {
        String pageTitle = title != null
                ? String.format(this.getString(R.string.data_thread_withsubject_title), this.mBoardName, title)
                : String.format(this.getString(R.string.data_thread_title), this.mBoardName, this.mThreadNumber);

        this.setTitle(pageTitle);

        String tabTitle = title != null ? title : pageTitle;
        this.mTabModel.setTitle(tabTitle);
        this.mHistoryDataSource.updateHistoryItem(this.mWebsite.name(), this.mBoardName, this.mThreadNumber, tabTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.thread, menu);

        this.mMenu = menu;
        this.updateOptionsMenu();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tabs_menu_id:
                Class<? extends Activity> tabsActivity = Constants.SDK_VERSION >= 4 ? TabsHistoryBookmarksActivity.class : TabsHistoryBookmarksCompActivity.class;
                Intent openTabsIntent = new Intent(this.getApplicationContext(), tabsActivity);

                String currentUrl = this.mUrlBuilder.getThreadUrlHtml(this.mBoardName, this.mThreadNumber);
                openTabsIntent.putExtra(Constants.EXTRA_CURRENT_URL, currentUrl);
                this.startActivity(openTabsIntent);
                break;
            case R.id.refresh_menu_id:
                this.refresh();
                break;
            case R.id.pick_board_menu_id:
                this.mNavigationService.navigateBoardList(this, this.mWebsite.name(), true);
                break;
            case R.id.open_browser_menu_id:
                BrowserLauncher.launchExternalBrowser(this, this.mUrlBuilder.getThreadUrlHtml(this.mBoardName, this.mThreadNumber));
                break;
            case R.id.preferences_menu_id:
                // Start new activity
                Intent preferencesIntent = new Intent(this.getApplicationContext(), ApplicationPreferencesActivity.class);
                this.startActivity(preferencesIntent);
                break;
            case R.id.add_menu_id:
                this.navigateToAddNewPost();
                break;
            case R.id.download_all_files_menu_id:
                List<String> filePaths = this.mAdapter.getAllPostFiles();
                if (filePaths.size() > 0) {
                    DownloadFileListTask downloadAllTask = new DownloadFileListTask(this, filePaths);
                    downloadAllTask.execute();
                }
                break;
            case R.id.share_menu_id:
                String shareUrl = this.mUrlBuilder.getThreadUrlHtml(this.mBoardName, this.mThreadNumber);

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, this.mTabModel.getTitle());
                i.putExtra(Intent.EXTRA_TEXT, shareUrl);
                this.startActivity(Intent.createChooser(i, this.getString(R.string.share_via)));
                break;
            case R.id.add_remove_favorites_menu_id:
                if (this.mFavoritesDatasource.hasFavorites(this.mWebsite.name(), this.mBoardName, this.mThreadNumber)) {
                    this.mFavoritesDatasource.removeFromFavorites(this.mWebsite.name(), this.mBoardName, this.mThreadNumber);
                } else {
                    this.mFavoritesDatasource.addToFavorites(this.mWebsite.name(), this.mBoardName, this.mThreadNumber, this.mTabModel.getTitle());
                }

                this.updateOptionsMenu();

                break;
            case android.R.id.home:
                this.navigateToThreads();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IPostListEntity item = this.mAdapter.getItem(info.position);
        if (item instanceof StatusIndicatorEntity) {
            return;
        }

        PostItemViewModel model = (PostItemViewModel)item;
        populateContextMenu(menu, model, this.getResources());
    }

    public static void populateContextMenu(Menu menu, PostItemViewModel model, Resources res) {
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST, 0, res.getString(R.string.cmenu_reply_post));
        if (!StringUtils.isEmpty(model.getSpannedComment().toString())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST_QUOTE, 1, res.getString(R.string.cmenu_reply_post_quote));
        }
        if (!StringUtils.isEmpty(model.getSpannedComment())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_TEXT, 2, res.getString(R.string.cmenu_copy_post));
        }
        if (model.hasAttachment() && model.getAttachment(0).isFile()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_DOWNLOAD_FILE, 3, res.getString(
                    model.getAttachmentsNumber() == 1 ? R.string.cmenu_download_file
                            : R.string.cmenu_download_files));
        }
        if (!StringUtils.isEmpty(model.getSpannedComment())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_SHARE, 4, res.getString(R.string.cmenu_share));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        IPostListEntity adapterItem = this.mAdapter.getItem(menuInfo.position);
        if (adapterItem instanceof StatusIndicatorEntity) {
            return true;
        }

        PostItemViewModel model = (PostItemViewModel) adapterItem;
        View view = AppearanceUtils.getListItemAtPosition(this.getListView(), menuInfo.position);

        return handleContextMenuItemClick(item, model, this, view);
    }

    public static boolean handleContextMenuItemClick(MenuItem item, PostItemViewModel model, Activity activity, View view) {
        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_REPLY_POST:
                navigateToAddReply(activity, model, null);
                break;
            case Constants.CONTEXT_MENU_REPLY_POST_QUOTE:
                navigateToAddReply(activity, model, model.getSpannedComment().toString());
                break;
            case Constants.CONTEXT_MENU_COPY_TEXT:
                PostItemViewBuilder.ViewBag vb = (PostItemViewBuilder.ViewBag) view.getTag();

                if (CompatibilityUtils.isTextSelectable(vb.commentView)) {
                    vb.commentView.startSelection();
                } else {
                    CompatibilityUtils.copyText(activity, "#" + model.getNumber(), model.getSpannedComment().toString());

                    AppearanceUtils.showToastMessage(activity, activity.getString(R.string.notification_post_copied));
                }
                break;
            case Constants.CONTEXT_MENU_DOWNLOAD_FILE:
                for (int i = 0; i < model.getAttachmentsNumber(); ++i) {
                    AttachmentInfo attachment = model.getAttachment(i);
                    Uri fileUri = Uri.parse(attachment.getSourceUrl());
                    new DownloadFileTask(activity, fileUri).execute();
                }
                break;
            case Constants.CONTEXT_MENU_SHARE:
                Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                shareLinkIntent.setType("text/plain");
                shareLinkIntent.putExtra(Intent.EXTRA_SUBJECT, model.getBoardName() + ", post #" + model.getNumber());
                shareLinkIntent.putExtra(Intent.EXTRA_TEXT, model.getSpannedComment().toString());
                activity.startActivity(Intent.createChooser(shareLinkIntent, activity.getString(R.string.share_via)));
                break;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        super.onSaveInstanceState(outState);
    }

    private void updateOptionsMenu() {
        if (this.mMenu == null) {
            return;
        }

        MenuItem favoritesItem = this.mMenu.findItem(R.id.add_remove_favorites_menu_id);
        if (this.mFavoritesDatasource.hasFavorites(this.mWebsite.name(), this.mBoardName, this.mThreadNumber)) {
            favoritesItem.setTitle(R.string.menu_remove_favorites);
        } else {
            favoritesItem.setTitle(R.string.menu_add_favorites);
        }
    }

    private void navigateToAddNewPost() {
        Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, this.mWebsite.name());
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, this.mThreadNumber);

        this.startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    private static void navigateToAddReply(Activity activity, PostItemViewModel model, String postComment) {
        Intent addPostIntent = new Intent(activity.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, model.getWebsite().name());
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, model.getBoardName());
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, model.getThreadNumber());
        addPostIntent.putExtra(Constants.EXTRA_POST_NUMBER, model.getNumber());
        if (postComment != null) {
            addPostIntent.putExtra(Constants.EXTRA_POST_COMMENT, postComment);
        }

        activity.startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    private void navigateToThreads() {
        this.mNavigationService.navigateBoardPage(this, null, this.mWebsite.name(), this.mBoardName, 0, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_ADD_POST_ACTIVITY:
                    this.refresh();
                    break;
            }
        }
    }

    public void refresh() {
        this.refreshPosts(true);
    }

    private void refreshPosts(boolean checkModified) {
        if (this.mCurrentDownloadTask != null) {
            this.mCurrentDownloadTask.cancel(true);
        }

        if (!this.mAdapter.isEmpty()) {
            // load new posts
            this.mCurrentDownloadTask = new DownloadPostsTask(this.mPostsReaderListener, this.mBoardName, this.mThreadNumber, true, this.mJsonReader, true);
            this.mCurrentDownloadTask.execute(this.mAdapter.getLastPostNumber());
        } else {
            this.mCurrentDownloadTask = new DownloadPostsTask(this.mPostsReaderListener, this.mBoardName, this.mThreadNumber, checkModified, this.mJsonReader, false);
            this.mCurrentDownloadTask.execute();
        }
    }

    private class LoadPostsTask extends AsyncTask<Void, Long, PostModel[]> {
        private boolean mPreferDeserialized;

        public LoadPostsTask(boolean preferDeserialized) {
            this.mPreferDeserialized = preferDeserialized;
        }

        @Override
        protected PostModel[] doInBackground(Void... arg0) {
            // Пробуем десериализовать в любом случае
            PostModel[] posts = mSerializationService.deserializePosts(mWebsite.name(), mBoardName, mThreadNumber);
            return posts;
        }

        @Override
        public void onPreExecute() {
            PostsListActivity.this.mPostsReaderListener.showLoadingScreen();
        }

        @Override
        public void onPostExecute(PostModel[] posts) {
            PostsListActivity.this.mPostsReaderListener.hideLoadingScreen();

            if (posts != null) {
                PostsListActivity.this.setAdapterData(posts);

                // Обновляем посты, если не был установлен ограничивающий extra
                if (this.mPreferDeserialized) {
                    // nothing
                } else {
                    PostsListActivity.this.refresh();
                }
            } else {
                PostsListActivity.this.refreshPosts(false);
            }
        }
    }

    private class PostsReaderListener implements IPostsListView {
        @Override
        public Context getApplicationContext() {
            return PostsListActivity.this.getApplicationContext();
        }

        @Override
        public void setWindowProgress(int value) {
            PostsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, value);
        }

        @Override
        public void setData(PostModel[] posts) {
            if (posts != null && posts.length > 0) {
                PostsListActivity.this.setAdapterData(posts);
                mSerializationService.serializePosts(mWebsite.name(), mBoardName, mThreadNumber, mAdapter.getOriginalPosts());
            } else {
                PostsListActivity.this.mAdapter.clear();
                this.showError(PostsListActivity.this.getString(R.string.error_list_empty));
            }
        }

        @Override
        public void showError(String error) {
            if (error != null && error.startsWith("503")) {
                error = "Error 503: it seems like Cloudflare check, open any board first.";
            }
            PostsListActivity.this.switchToErrorView(error);
        }

        @Override
        public void showCaptcha(CaptchaEntity captcha) {
            // TODO: replace by captcha view
            this.showError("Cloudflare captcha, open any board first.");
        }

        @Override
        public void showLoadingScreen() {
            PostsListActivity.this.switchToLoadingView();
        }

        @Override
        public void hideLoadingScreen() {
            PostsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_OFF);
            PostsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);

            PostsListActivity.this.switchToListView();
            PostsListActivity.this.mCurrentDownloadTask = null;
        }

        @Override
        public void updateData(int from, PostModel[] posts) {
            if (posts == null) {
                showToastIfVisible(PostsListActivity.this.getResources().getString(R.string.notification_no_new_posts));
                return;
            }

            int addedCount = PostsListActivity.this.mAdapter.updateAdapterData(from, posts);
            if (addedCount != 0) {
                mSerializationService.serializePosts(mWebsite.name(), mBoardName, mThreadNumber, mAdapter.getOriginalPosts());
                showToastIfVisible(PostsListActivity.this.getResources().getQuantityString(R.plurals.data_new_posts_quantity, addedCount, addedCount));
            } else {
                showToastIfVisible(PostsListActivity.this.getResources().getString(R.string.notification_no_new_posts));
            }
        }

        @Override
        public void showUpdateError(String error) {
            showToastIfVisible(error);
        }

        @Override
        public void showUpdateLoading() {
            PostsListActivity.this.mAdapter.setUpdating(true);
        }

        @Override
        public void hideUpdateLoading() {
            PostsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_OFF);
            PostsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);

            PostsListActivity.this.mAdapter.setUpdating(false);
            PostsListActivity.this.mCurrentDownloadTask = null;
        }
    }
}
