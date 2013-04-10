package com.vortexwolf.dvach.activities;

import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.ThreadsListAdapter;
import com.vortexwolf.dvach.asynctasks.DownloadFileTask;
import com.vortexwolf.dvach.asynctasks.DownloadThreadsTask;
import com.vortexwolf.dvach.asynctasks.SearchImageTask;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.CompatibilityUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.db.HiddenThreadsDataSource;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IListView;
import com.vortexwolf.dvach.interfaces.IPagesSerializationService;
import com.vortexwolf.dvach.models.domain.ThreadInfo;
import com.vortexwolf.dvach.models.domain.ThreadsList;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.models.presentation.OpenTabModel;
import com.vortexwolf.dvach.models.presentation.PostItemViewModel;
import com.vortexwolf.dvach.models.presentation.ThreadItemViewModel;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.Tracker;
import com.vortexwolf.dvach.services.presentation.ClickListenersFactory;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.services.presentation.ListViewScrollListener;
import com.vortexwolf.dvach.services.presentation.PostItemViewBuilder;
import com.vortexwolf.dvach.settings.ApplicationPreferencesActivity;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import com.vortexwolf.dvach.settings.SettingsEntity;

public class ThreadsListActivity extends BaseListActivity {
    private static final String TAG = "ThreadsListActivity";

    private MainApplication mApplication;
    private IJsonApiReader mJsonReader;
    private Tracker mTracker;
    private ApplicationSettings mSettings;
    private IPagesSerializationService mSerializationService;
    private PostItemViewBuilder mPostItemViewBuilder;
    private HiddenThreadsDataSource mHiddenThreadsDataSource;
    private DvachUriBuilder mDvachUriBuilder;

    private DownloadThreadsTask mCurrentDownloadTask = null;
    private ThreadsListAdapter mAdapter = null;
    private final ThreadsReaderListener mThreadsReaderListener = new ThreadsReaderListener();

    private SettingsEntity mCurrentSettings;

    private View mNavigationBar;

    private OpenTabModel mTabModel;
    private String mBoardName;
    private int mPageNumber = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mApplication = this.getMainApplication();

        // Парсим код доски и номер страницы
        Uri data = this.getIntent().getData();
        if (data != null) {
            this.mBoardName = UriUtils.getBoardName(data);
            this.mPageNumber = UriUtils.getBoardPageNumber(data);
        }

        if (StringUtils.isEmpty(this.mBoardName)) {
            this.mBoardName = this.mApplication.getSettings().getHomepage();
        }

        this.mJsonReader = this.mApplication.getJsonApiReader();
        this.mSettings = this.mApplication.getSettings();
        this.mCurrentSettings = this.mSettings.getCurrentSettings();
        this.mTracker = this.mApplication.getTracker();
        this.mSerializationService = this.mApplication.getSerializationService();
        this.mPostItemViewBuilder = new PostItemViewBuilder(this, this.mBoardName, null, this.mApplication.getBitmapManager(), this.mSettings);
        this.mHiddenThreadsDataSource = Factory.getContainer().resolve(HiddenThreadsDataSource.class);
        this.mDvachUriBuilder = Factory.getContainer().resolve(DvachUriBuilder.class);

        // Заголовок страницы
        String pageTitle = this.mPageNumber != 0
                ? String.format(this.getString(R.string.data_board_title_with_page), this.mBoardName, this.mPageNumber)
                : String.format(this.getString(R.string.data_board_title), this.mBoardName);
        this.setTitle(pageTitle);

        // Сохраняем во вкладках
        OpenTabModel tabModel = new OpenTabModel(this.mBoardName, this.mDvachUriBuilder.create2chBoardUri(this.mBoardName, this.mPageNumber));
        this.mTabModel = this.mApplication.getOpenTabsManager().add(tabModel);

        this.resetUI();

        this.setAdapter(savedInstanceState);

        this.mTracker.setBoardVar(this.mBoardName);
        this.mTracker.setPageNumberVar(this.mPageNumber);
        this.mTracker.trackActivityView(TAG);
    }

    @Override
    protected void onStop() {
        this.mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(this.getListView()));

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SettingsEntity newSettings = this.mSettings.getCurrentSettings();

        if (this.mCurrentSettings.theme != newSettings.theme) {
            this.finish();
            Intent i = new Intent(this, this.getClass());
            i.putExtra(Constants.EXTRA_PREFER_DESERIALIZED, true);
            this.startActivity(i);
            return;
        }

        if (this.mCurrentSettings.isDisplayNavigationBar != newSettings.isDisplayNavigationBar) {
            this.mNavigationBar.setVisibility(newSettings.isDisplayNavigationBar ? View.VISIBLE : View.GONE);
        }

        if (this.mCurrentSettings.isLoadThumbnails != newSettings.isLoadThumbnails) {
            this.mAdapter.notifyDataSetChanged();
        }

        this.mCurrentSettings = this.mSettings.getCurrentSettings();
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
        this.mNavigationBar.setVisibility(this.mSettings.isDisplayNavigationBar() ? View.VISIBLE : View.GONE);

        TextView pageNumberView = (TextView) this.findViewById(R.id.threads_page_number);
        pageNumberView.setText(String.valueOf(this.mPageNumber));

        ImageButton nextButton = (ImageButton) this.findViewById(R.id.threads_next_page);
        ImageButton prevButton = (ImageButton) this.findViewById(R.id.threads_prev_page);
        if (this.mPageNumber == 0) {
            prevButton.setVisibility(View.GONE);
        } else if (this.mPageNumber == 19) {
            nextButton.setVisibility(View.GONE);
        }

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadsListActivity.this.navigateToBoardPageNumber(ThreadsListActivity.this.mBoardName, ThreadsListActivity.this.mPageNumber - 1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadsListActivity.this.navigateToBoardPageNumber(ThreadsListActivity.this.mBoardName, ThreadsListActivity.this.mPageNumber + 1);
            }
        });
    }

    private void setAdapter(Bundle savedInstanceState) {
        if (this.mAdapter != null) {
            return;
        }

        this.mAdapter = new ThreadsListAdapter(this, this.mBoardName, this.mApplication.getBitmapManager(), this.mApplication.getSettings(), this.getTheme(), this.mHiddenThreadsDataSource, this.mDvachUriBuilder);
        this.setListAdapter(this.mAdapter);

        // добавляем обработчик, чтобы не рисовать картинки во время прокрутки
        if (Integer.valueOf(Build.VERSION.SDK) > 7) {
            this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }

        ThreadInfo[] threads = null;
        if (this.getIntent().hasExtra(Constants.EXTRA_PREFER_DESERIALIZED)
            || savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED)) {
            threads = this.mSerializationService.deserializeThreads(this.mBoardName, this.mPageNumber);
        }

        if (threads == null) {
            this.refreshThreads(false);
        } else {
            this.mAdapter.setAdapterData(threads);
            // Устанавливаем позицию, если открываем как уже открытую вкладку
            AppearanceUtils.ListViewPosition savedPosition = this.mTabModel.getPosition();
            if (savedPosition != null) {
                this.getListView().setSelectionFromTop(savedPosition.position, savedPosition.top);
            }
        }
    }
    
    @Override
    public boolean onSearchRequested() {
        Bundle data = new Bundle();
        data.putString(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        
        this.startSearch(null, false, data, false);
        
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tabs_menu_id:
                Intent openTabsIntent = new Intent(this.getApplicationContext(), TabsHistoryBookmarksActivity.class);
                openTabsIntent.putExtra(Constants.EXTRA_CURRENT_URL, this.mTabModel.getUri().toString());
                this.startActivity(openTabsIntent);
                break;
            case R.id.pick_board_menu_id:
            case android.R.id.home:
                // Start new activity
                Intent pickBoardIntent = new Intent(this.getApplicationContext(), PickBoardActivity.class);
                this.startActivityForResult(pickBoardIntent, Constants.REQUEST_CODE_PICK_BOARD_ACTIVITY);
                break;
            case R.id.refresh_menu_id:
                this.refreshThreads();
                break;
            case R.id.open_browser_menu_id:
                BrowserLauncher.launchExternalBrowser(this, this.mDvachUriBuilder.create2chBoardUri(this.mBoardName, this.mPageNumber).toString(), true);
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
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_PICK_BOARD_ACTIVITY:
                    String boardCode = intent.getExtras().getString(Constants.EXTRA_SELECTED_BOARD);
                    // Открываем новую борду, если не совпадает с открытой
                    if (!StringUtils.isEmpty(boardCode) && !this.mBoardName.equals(boardCode)) {
                        this.navigateToBoardPageNumber(boardCode, 0);
                    }
                    break;
                case Constants.REQUEST_CODE_ADD_POST_ACTIVITY:
                    // Получаем номер созданного треда и переходим к нему. Иначе
                    // обновляем список тредов на всякий случай
                    String redirectedThreadNumber = intent.getExtras().getString(Constants.EXTRA_REDIRECTED_THREAD_NUMBER);
                    if (redirectedThreadNumber != null) {
                        this.navigateToThread(redirectedThreadNumber);
                    } else {
                        this.refreshThreads();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ThreadItemViewModel info = this.mAdapter.getItem(position);

        if (info.isHidden()) {
            this.mHiddenThreadsDataSource.removeFromHiddenThreads(this.mBoardName, info.getNumber());
            info.setHidden(false);
            this.mAdapter.notifyDataSetChanged();
        } else {
            String threadSubject = !StringUtils.isEmpty(info.getSubject())
                    ? info.getSubject()
                    : StringUtils.cutIfLonger(StringUtils.emptyIfNull(info.getSpannedComment().toString()), 50);
            this.navigateToThread(info.getNumber(), threadSubject);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ThreadItemViewModel item = this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_ANSWER, 0, this.getString(R.string.cmenu_answer_without_reading));

        if (item.hasAttachment() && item.getAttachment(this.mBoardName).isFile()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_DOWNLOAD_FILE, 1, this.getString(R.string.cmenu_download_file));
        }

        if (item.isEllipsized()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_VIEW_FULL_POST, 2, this.getString(R.string.cmenu_view_op_post));
        }

        if (item.hasAttachment() && item.getAttachment(this.mBoardName).isImage()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_SEARCH_IMAGE, 3, this.getString(R.string.cmenu_search_image));
        }

        if (!item.isHidden()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_HIDE_THREAD, 4, this.getString(R.string.cmenu_hide_thread));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ThreadItemViewModel info = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_ANSWER: {
                Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
                addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
                addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, info.getNumber());
                this.startActivity(addPostIntent);
                return true;
            }
            case Constants.CONTEXT_MENU_DOWNLOAD_FILE: {
                AttachmentInfo attachment = info.getAttachment(this.mBoardName);
                Uri fileUri = Uri.parse(attachment.getSourceUrl(this.mSettings));
                new DownloadFileTask(this, fileUri).execute();
                return true;
            }
            case Constants.CONTEXT_MENU_VIEW_FULL_POST: {
                PostItemViewModel postModel = new PostItemViewModel(Constants.OP_POST_POSITION, info.getOpPost(), this.getTheme(), this.mSettings, ClickListenersFactory.getDefaultSpanClickListener(this.mDvachUriBuilder), this.mDvachUriBuilder);
                this.mPostItemViewBuilder.displayPopupDialog(postModel, this, this.getTheme());
                return true;
            }
            case Constants.CONTEXT_MENU_SEARCH_IMAGE: {
                String imageUrl = info.getAttachment(this.mBoardName).getSourceUrl(this.mSettings).replace("2ch.so", "2-ch.so");
                new SearchImageTask(imageUrl, this.getApplicationContext(), MainApplication.getHttpClient()).execute();
                return true;
            }
            case Constants.CONTEXT_MENU_HIDE_THREAD: {
                this.mHiddenThreadsDataSource.addToHiddenThreads(this.mBoardName, info.getNumber());
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

    private void navigateToAddThreadView() {
        Intent addPostIntent = new Intent(this.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, Constants.ADD_THREAD_PARENT);

        this.startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    private void navigateToThread(String threadNumber) {
        this.navigateToThread(threadNumber, null);
    }

    private void navigateToThread(String threadNumber, String threadSubject) {
        Intent i = new Intent(this.getApplicationContext(), PostsListActivity.class);
        i.setData(Uri.parse(this.mDvachUriBuilder.create2chThreadUrl(this.mBoardName, threadNumber)));
        if (threadSubject != null) {
            i.putExtra(Constants.EXTRA_THREAD_SUBJECT, threadSubject);
        }

        this.startActivity(i);
    }

    private void navigateToBoardPageNumber(String boardCode, int pageNumber) {
        Intent i = new Intent(this.getApplicationContext(), ThreadsListActivity.class);
        i.setData(this.mDvachUriBuilder.create2chBoardUri(boardCode, pageNumber));

        this.startActivity(i);
    }

    private void refreshThreads() {
        this.refreshThreads(true);
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

    private class ThreadsReaderListener implements IListView<ThreadsList> {

        @Override
        public Context getApplicationContext() {
            return ThreadsListActivity.this.getApplicationContext();
        }

        @Override
        public void setWindowProgress(int value) {
            ThreadsListActivity.this.setProgress(value);
        }

        @Override
        public void setData(ThreadsList threadsList) {
            if (threadsList != null) {
                ThreadInfo[] threads = threadsList.getThreads();
                ThreadsListActivity.this.mSerializationService.serializeThreads(ThreadsListActivity.this.mBoardName, ThreadsListActivity.this.mPageNumber, threads);
                ThreadsListActivity.this.mAdapter.setAdapterData(threads);
            } else {
                ThreadsListActivity.this.mAdapter.clear();
                this.showError(ThreadsListActivity.this.getString(R.string.error_list_empty));
            }
        }

        @Override
        public void showError(String error) {
            ThreadsListActivity.this.switchToErrorView(error);
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