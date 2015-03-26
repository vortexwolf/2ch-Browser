package com.vortexwolf.chan.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.ThreadsListAdapter;
import com.vortexwolf.chan.asynctasks.DownloadThreadsTask;
import com.vortexwolf.chan.boards.makaba.MakabaApiReader;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HiddenThreadsDataSource;
import com.vortexwolf.chan.interfaces.ICloudflareCheckListener;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IListView;
import com.vortexwolf.chan.interfaces.IOpenTabsManager;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.models.domain.ThreadModel;
import com.vortexwolf.chan.models.presentation.OpenTabModel;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.models.presentation.ThreadItemViewModel;
import com.vortexwolf.chan.services.BrowserLauncher;
import com.vortexwolf.chan.services.CloudflareCheckService;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.NavigationService;
import com.vortexwolf.chan.services.presentation.ClickListenersFactory;
import com.vortexwolf.chan.services.presentation.ListViewScrollListener;
import com.vortexwolf.chan.services.presentation.PagesSerializationService;
import com.vortexwolf.chan.services.presentation.PostItemViewBuilder;
import com.vortexwolf.chan.settings.ApplicationPreferencesActivity;
import com.vortexwolf.chan.settings.ApplicationSettings;
import com.vortexwolf.chan.settings.SettingsEntity;

public class ThreadsListActivity extends BaseListActivity {
    private static final String TAG = "ThreadsListActivity";

    private IJsonApiReader mJsonReader;
    private final MyTracker mTracker = Factory.resolve(MyTracker.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final PagesSerializationService mSerializationService = Factory.resolve(PagesSerializationService.class);
    private final FavoritesDataSource mFavoritesDatasource = Factory.resolve(FavoritesDataSource.class);
    private final HiddenThreadsDataSource mHiddenThreadsDataSource = Factory.resolve(HiddenThreadsDataSource.class);
    private final IOpenTabsManager mOpenTabsManager = Factory.resolve(IOpenTabsManager.class);
    private final NavigationService mNavigationService = Factory.resolve(NavigationService.class);
    private PostItemViewBuilder mPostItemViewBuilder;
    private IUrlBuilder mUrlBuilder;

    private DownloadThreadsTask mCurrentDownloadTask = null;
    private ThreadsListAdapter mAdapter = null;
    private final ThreadsReaderListener mThreadsReaderListener = new ThreadsReaderListener();

    private SettingsEntity mCurrentSettings;

    private View mNavigationBar;
    private View mCatalogBar;

    private OpenTabModel mTabModel;
    private String mWebsite;
    private String mBoardName;
    private int mPageNumber = 0;

    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Парсим код доски и номер страницы
        Bundle extras = this.getIntent().getExtras();
        this.mWebsite = extras.getString(Constants.EXTRA_WEBSITE);
        this.mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
        this.mPageNumber = extras.getInt(Constants.EXTRA_BOARD_PAGE, 0);

        this.mUrlBuilder = Websites.getUrlBuilder(this.mWebsite);
        this.mJsonReader = Factory.resolve(MakabaApiReader.class);

        this.mCurrentSettings = this.mSettings.getCurrentSettings();
        this.mPostItemViewBuilder = new PostItemViewBuilder(this, this.mWebsite, this.mBoardName, null, this.mSettings);

        // Заголовок страницы
        String pageTitle = this.mPageNumber > 0
                ? String.format(this.getString(R.string.data_board_title_with_page), this.mBoardName, this.mPageNumber)
                : this.mPageNumber == 0
                ? String.format(this.getString(R.string.data_board_title), this.mBoardName)
                : String.format(this.getString(R.string.data_board_title_catalog), this.mBoardName);
        this.setTitle(pageTitle);

        // Сохраняем во вкладках
        OpenTabModel tabModel = new OpenTabModel(this.mWebsite, this.mBoardName, this.mPageNumber, null, null);
        this.mTabModel = this.mOpenTabsManager.add(tabModel);

        this.resetUI();

        this.setAdapter(savedInstanceState);

        this.mTracker.setBoardVar(this.mBoardName);
        this.mTracker.setPageNumberVar(this.mPageNumber);
        this.mTracker.trackActivityView(TAG);
    }

    @Override
    protected void onPause() {
        this.mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(this.getListView()));

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SettingsEntity newSettings = this.mSettings.getCurrentSettings();

        if (this.mCurrentSettings.theme != newSettings.theme) {
            this.finish();
            Intent i = new Intent(this.getIntent());
            i.putExtra(Constants.EXTRA_PREFER_DESERIALIZED, true);
            this.startActivity(i);
            return;
        }

        if (this.mCurrentSettings.isLoadThumbnails != newSettings.isLoadThumbnails) {
            this.mAdapter.notifyDataSetChanged();
        }

        this.mCurrentSettings = newSettings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.threads_list_view;
    }

    @Override
    protected void resetUI() {
        // вызываем метод базового класса
        super.resetUI();

        CompatibilityUtils.setDisplayHomeAsUpEnabled(this);
        this.registerForContextMenu(this.getListView());

        // Панель навигации по страницам
        this.mNavigationBar = this.findViewById(R.id.threads_navigation_bar);
        this.mNavigationBar.setVisibility(this.mPageNumber < 0 ? View.GONE : View.VISIBLE);

        this.mCatalogBar = this.findViewById(R.id.threads_catalog_bar);
        this.mCatalogBar.setVisibility(this.mPageNumber < 0 ? View.VISIBLE : View.GONE);

        TextView pageNumberView = (TextView) this.findViewById(R.id.threads_page_number);
        pageNumberView.setText(String.valueOf(this.mPageNumber));

        ImageButton nextButton = (ImageButton) this.findViewById(R.id.threads_next_page);
        ImageButton prevButton = (ImageButton) this.findViewById(R.id.threads_prev_page);
        if (this.mPageNumber == 0) {
            prevButton.setVisibility(View.INVISIBLE);
        } else if (this.mPageNumber == 19) {
            nextButton.setVisibility(View.INVISIBLE);
        }

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadsListActivity.this.navigateToPageNumber(ThreadsListActivity.this.mPageNumber - 1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadsListActivity.this.navigateToPageNumber(ThreadsListActivity.this.mPageNumber + 1);
            }
        });

        Spinner filterSelect = (Spinner) this.findViewById(R.id.threads_filter_select);
        filterSelect.setSelection(-1-mPageNumber);
        filterSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ThreadsListActivity.this.mPageNumber != -1-position) {
                    ThreadsListActivity.this.mPageNumber = -1-position;
                    ThreadsListActivity.this.refreshThreads(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setAdapter(Bundle savedInstanceState) {
        if (this.mAdapter != null) {
            return;
        }

        this.mAdapter = new ThreadsListAdapter(this, this.mWebsite, this.mBoardName, this.getTheme());
        this.setListAdapter(this.mAdapter);

        // добавляем обработчик, чтобы не рисовать картинки во время прокрутки
        if (Constants.SDK_VERSION > 7) {
            this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }

        boolean preferDeserialized = this.getIntent().hasExtra(Constants.EXTRA_PREFER_DESERIALIZED) || savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED);

        if (preferDeserialized) {
            new LoadThreadsTask().execute();
        } else {
            this.refreshThreads(false);
        }
    }

    private void setAdapterData(ThreadModel[] threads) {
        this.mAdapter.setAdapterData(threads);
        // Устанавливаем позицию, если открываем как уже открытую вкладку
        AppearanceUtils.ListViewPosition savedPosition = this.mTabModel.getPosition();
        if (savedPosition != null) {
            this.getListView().setSelectionFromTop(savedPosition.position, savedPosition.top);
        }
    }

    @Override
    public boolean onSearchRequested() {
        Bundle data = new Bundle();
        data.putString(Constants.EXTRA_WEBSITE, this.mWebsite);
        data.putString(Constants.EXTRA_BOARD_NAME, this.mBoardName);

        this.startSearch(null, false, data, false);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.board, menu);

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

                String currentUrl = this.mUrlBuilder.getPageUrlHtml(this.mBoardName, this.mPageNumber);
                openTabsIntent.putExtra(Constants.EXTRA_CURRENT_URL, currentUrl);
                this.startActivity(openTabsIntent);
                break;
            case R.id.pick_board_menu_id:
            case android.R.id.home:
                this.mNavigationService.navigateBoardList(this, this.mWebsite, true);
                break;
            case R.id.refresh_menu_id:
                this.refresh();
                break;
            case R.id.open_browser_menu_id:
                String browserUrl = null;
                if (this.mPageNumber < 0) {
                    browserUrl = this.mUrlBuilder.getCatalogUrlHtml(this.mBoardName, -1 - this.mPageNumber);
                } else {
                    browserUrl = this.mUrlBuilder.getPageUrlHtml(this.mBoardName, this.mPageNumber);
                }
                BrowserLauncher.launchExternalBrowser(this, browserUrl);
                break;
            case R.id.preferences_menu_id:
                // Start new activity
                Intent preferencesIntent = new Intent(this.getApplicationContext(), ApplicationPreferencesActivity.class);
                this.startActivity(preferencesIntent);
                break;
            case R.id.add_menu_id:
                this.navigateToAddThreadView();
                break;
            case R.id.menu_search_id:
                this.onSearchRequested();
                break;
            case R.id.menu_catalog_id:
                this.navigateToCatalog(this.mBoardName, -1);
                break;
            case R.id.add_remove_favorites_menu_id:
                if (this.mFavoritesDatasource.hasFavorites(this.mWebsite, this.mBoardName, null)) {
                    this.mFavoritesDatasource.removeFromFavorites(this.mWebsite, this.mBoardName, null);
                } else {
                    this.mFavoritesDatasource.addToFavorites(this.mWebsite, this.mBoardName, null, this.mTabModel.getTitle());
                }

                this.updateOptionsMenu();

                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_ADD_POST_ACTIVITY:
                    // Получаем номер созданного треда и переходим к нему. Иначе
                    // обновляем список тредов на всякий случай
                    String redirectedThreadNumber = intent.getExtras().getString(Constants.EXTRA_REDIRECTED_THREAD_NUMBER);
                    if (redirectedThreadNumber != null) {
                        this.navigateToThread(redirectedThreadNumber);
                    } else {
                        this.refresh();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ThreadItemViewModel info = this.mAdapter.getItem(position);

        if (info.isHidden()) {
            this.mHiddenThreadsDataSource.removeFromHiddenThreads(this.mWebsite, this.mBoardName, info.getNumber());
            info.setHidden(false);
            this.mAdapter.notifyDataSetChanged();
        } else {
            String threadSubject = info.getSubjectOrText();
            this.navigateToThread(info.getNumber(), threadSubject);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ThreadItemViewModel item = this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_ANSWER, 0, this.getString(R.string.cmenu_answer_without_reading));
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_VIEW_FULL_POST, 2, this.getString(R.string.cmenu_view_op_post));

        if (!item.isHidden()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_HIDE_THREAD, 3, this.getString(R.string.cmenu_hide_thread));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ThreadItemViewModel info = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_ANSWER: {
                this.navigateToAddPostView(info.getNumber());
                return true;
            }
            case Constants.CONTEXT_MENU_VIEW_FULL_POST: {
                PostItemViewModel postModel = new PostItemViewModel(this.mWebsite, this.mBoardName, info.getNumber(), Constants.OP_POST_POSITION, info.getOpPost(), this.getTheme(), ClickListenersFactory.getDefaultSpanClickListener(this.mUrlBuilder));
                this.mPostItemViewBuilder.displayPopupDialog(postModel, this, this.getTheme(), null);
                return true;
            }
            case Constants.CONTEXT_MENU_HIDE_THREAD: {
                this.mHiddenThreadsDataSource.addToHiddenThreads(this.mWebsite, this.mBoardName, info.getNumber());
                info.setHidden(true);
                this.mAdapter.notifyDataSetChanged();
                return true;
            }
        }

        return false;
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
        if (this.mFavoritesDatasource.hasFavorites(this.mWebsite, this.mBoardName, null)) {
            favoritesItem.setTitle(R.string.menu_remove_favorites);
        } else {
            favoritesItem.setTitle(R.string.menu_add_favorites);
        }
    }

    private void navigateToAddThreadView() {
        Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, this.mWebsite);
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, Constants.ADD_THREAD_PARENT);

        this.startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    private void navigateToAddPostView(String threadNumber) {
        Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, this.mWebsite);
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, threadNumber);

        this.startActivity(addPostIntent);
    }

    private void navigateToThread(String threadNumber) {
        this.navigateToThread(threadNumber, null);
    }

    private void navigateToThread(String threadNumber, String threadSubject) {
        this.mNavigationService.navigateThread(this, null, this.mWebsite, this.mBoardName, threadNumber, threadSubject, null, false);
    }

    // TODO: rewrite without using getPageUrl
    private void navigateToCatalog(String boardCode, int filter) {
        AppearanceUtils.showToastMessage(this, "Catalog is not implemented.");
        /*Intent i = new Intent(this.getApplicationContext(), ThreadsListActivity.class);
        i.setData(Uri.parse(this.mMakabaUriBuilder.getPageUrlHtml(boardCode, filter)));
        this.startActivity(i);*/
    }

    private void navigateToPageNumber(int pageNumber) {
        this.mNavigationService.navigateBoardPage(this, null, this.mWebsite, this.mBoardName, pageNumber, false);
    }

    protected void refresh() {
        this.refreshThreads(false);
    }

    private void refreshThreads(boolean checkModified) {
        if (this.mCurrentDownloadTask != null) {
            this.mCurrentDownloadTask.cancel(true);
        }

        if (this.mBoardName != null) {
            this.mCurrentDownloadTask = new DownloadThreadsTask(this, this.mThreadsReaderListener, this.mBoardName, this.mPageNumber, checkModified, this.mJsonReader);
            this.mCurrentDownloadTask.execute();
        }
    }

    private class LoadThreadsTask extends AsyncTask<Void, Long, ThreadModel[]> {
        @Override
        protected ThreadModel[] doInBackground(Void... arg0) {
            ThreadModel[] threads = mSerializationService.deserializeThreads(mBoardName, mPageNumber);
            return threads;
        }

        @Override
        public void onPreExecute() {
            mThreadsReaderListener.showLoadingScreen();
        }

        @Override
        public void onPostExecute(ThreadModel[] threads) {
            mThreadsReaderListener.hideLoadingScreen();

            if (threads == null) {
                ThreadsListActivity.this.refreshThreads(false);
            } else {
                ThreadsListActivity.this.setAdapterData(threads);
            }
        }
    }

    private class ThreadsReaderListener implements IListView<ThreadModel[]> {

        @Override
        public Context getApplicationContext() {
            return ThreadsListActivity.this.getApplicationContext();
        }

        @Override
        public void setWindowProgress(int value) {
            ThreadsListActivity.this.setProgress(value);
        }

        @Override
        public void setData(ThreadModel[] threads) {
            if (threads != null && threads.length > 0) {
                ThreadsListActivity.this.mSerializationService.serializeThreads(ThreadsListActivity.this.mBoardName, ThreadsListActivity.this.mPageNumber, threads);
                ThreadsListActivity.this.setAdapterData(threads);
            } else {
                String error = ThreadsListActivity.this.getString(R.string.error_list_empty);
                if (ThreadsListActivity.this.mAdapter.getCount() == 0) {
                    this.showError(error);
                } else {
                    showToastIfVisible(error);
                }
            }
        }

        @Override
        public void showError(String error) {
            ThreadsListActivity.this.switchToErrorView(error);
            if (error != null && error.startsWith("503")) {
                String url = mUrlBuilder.getPageUrlHtml(mBoardName, mPageNumber);
                new CloudflareCheckService(url, ThreadsListActivity.this, new ICloudflareCheckListener(){
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
            ThreadsListActivity.this.switchToCaptchaView(captcha);
        }

        @Override
        public void showLoadingScreen() {
            ThreadsListActivity.this.switchToLoadingView();
        }

        @Override
        public void hideLoadingScreen() {
            ThreadsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_OFF);
            ThreadsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);

            ThreadsListActivity.this.switchToListView();
            ThreadsListActivity.this.mCurrentDownloadTask = null;
        }
    }
}