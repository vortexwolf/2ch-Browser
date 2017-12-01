package ua.in.quireg.chan.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.ui.adapters.ThreadsListAdapter;
import ua.in.quireg.chan.asynctasks.DownloadThreadsTask;
import ua.in.quireg.chan.boards.makaba.MakabaApiReader;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.interfaces.ICloudflareCheckListener;
import ua.in.quireg.chan.interfaces.IJsonApiReader;
import ua.in.quireg.chan.interfaces.IListView;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.models.domain.ThreadModel;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.services.CloudflareCheckService;
import ua.in.quireg.chan.services.NavigationService;
import ua.in.quireg.chan.services.presentation.ClickListenersFactory;
import ua.in.quireg.chan.services.presentation.ListViewScrollListener;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;
import ua.in.quireg.chan.services.presentation.PostItemViewBuilder;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.activities.AddPostActivity;

public class ThreadsListFragment extends BaseListFragment{

    private IJsonApiReader mJsonReader = Factory.resolve(MakabaApiReader.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private final PagesSerializationService mSerializationService = Factory.resolve(PagesSerializationService.class);
    private final FavoritesDataSource mFavoritesDatasource = Factory.resolve(FavoritesDataSource.class);
    private final HiddenThreadsDataSource mHiddenThreadsDataSource = Factory.resolve(HiddenThreadsDataSource.class);
    private final OpenTabsManager mOpenTabsManager = Factory.resolve(OpenTabsManager.class);
    private PostItemViewBuilder mPostItemViewBuilder;
    private IUrlBuilder mUrlBuilder;

    private DownloadThreadsTask mCurrentDownloadTask = null;
    private ThreadsListAdapter mAdapter = null;
    private final ThreadsReaderListener mThreadsReaderListener = new ThreadsReaderListener();


    private View mNavigationBar;
    private View mCatalogBar;

    private OpenTabModel mTabModel;
    private IWebsite mWebsite;
    private String mBoardName;
    private int mPageNumber;
    private boolean mIsCatalog;
    private int mCatalogFilter = 0;

    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebsite = Websites.getDefault();
        mUrlBuilder = mWebsite.getUrlBuilder();
        mBoardName = getArguments().getString(Constants.EXTRA_BOARD_NAME);
        mPageNumber = getArguments().getInt(Constants.EXTRA_BOARD_PAGE, 0);
        mIsCatalog = getArguments().getBoolean(Constants.EXTRA_CATALOG);

//        if (StringUtils.areEqual(mBoardName, "g")) {
//            extras.putString(Constants.EXTRA_BOARD_NAME, "gg");
//            mNavigationService.restartActivity(this, extras);
//        }
        // Сохраняем во вкладках
        OpenTabModel tabModel = new OpenTabModel(mWebsite, mBoardName, mPageNumber, null, null);
        mTabModel = mOpenTabsManager.add(tabModel);
        setHasOptionsMenu(true);

    }

    @Override
    public void onPause() {
        mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(mListView));
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.threads_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPostItemViewBuilder = new PostItemViewBuilder(getContext(), mWebsite, mBoardName, null, mSettings);

        registerForContextMenu(mListView);
        setAdapter(savedInstanceState);
        mAdapter.notifyDataSetChanged();

        // Заголовок страницы
        String pageTitle = mIsCatalog
                ? String.format(getString(R.string.data_board_title_catalog), mBoardName)
                : mPageNumber > 0
                ? String.format(getString(R.string.data_board_title_with_page), mBoardName, String.valueOf(mPageNumber))
                : String.format(getString(R.string.data_board_title), mBoardName);

        setTitle(pageTitle);

        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            ThreadItemViewModel info = mAdapter.getItem(position);

            if(info == null){
                return;
            }

            if (info.isHidden()) {
                mHiddenThreadsDataSource.removeFromHiddenThreads(mWebsite.name(), mBoardName, info.getNumber());
                info.setHidden(false);
                mAdapter.notifyDataSetChanged();
            } else {
                String threadSubject = info.getSubjectOrText();
                navigateToThread(info.getNumber(), threadSubject);
            }
        });

        // Панель навигации по страницам
//        mNavigationBar = getView().findViewById(R.id.threads_navigation_bar);
//        mNavigationBar.setVisibility(mIsCatalog ? View.GONE : View.VISIBLE);

        mCatalogBar = getView().findViewById(R.id.threads_catalog_bar);
        mCatalogBar.setVisibility(mIsCatalog ? View.VISIBLE : View.GONE);

//        TextView pageNumberView = (TextView) getView().findViewById(R.id.threads_page_number);
//        pageNumberView.setText(String.valueOf(mPageNumber));

//        ImageButton nextButton = (ImageButton) getView().findViewById(R.id.threads_next_page);
//        ImageButton prevButton = (ImageButton) getView().findViewById(R.id.threads_prev_page);
//        if (mPageNumber == 0) {
//            prevButton.setVisibility(View.INVISIBLE);
//        } else if (mPageNumber == 19) {
//            nextButton.setVisibility(View.INVISIBLE);
//        }
//
//        prevButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigateToPageNumber(mPageNumber - 1);
//            }
//        });
//
//        nextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigateToPageNumber(mPageNumber + 1);
//            }
//        });

        Spinner filterSelect = (Spinner) getView().findViewById(R.id.threads_filter_select);
        filterSelect.setSelection(mCatalogFilter);
        filterSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mCatalogFilter != position) {
                    mCatalogFilter = position;
                    refreshThreads(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setAdapter(Bundle savedInstanceState) {
        if (mAdapter == null) {
            mAdapter = new ThreadsListAdapter(getContext(), mWebsite, mBoardName, getActivity().getTheme(), mListView);
        }
        mListView.setAdapter(mAdapter);

        // добавляем обработчик, чтобы не рисовать картинки во время прокрутки
        if (Constants.SDK_VERSION > 7) {
            mListView.setOnScrollListener(new ListViewScrollListener(mAdapter));
        }

        boolean preferDeserialized = getArguments().getBoolean(Constants.EXTRA_PREFER_DESERIALIZED, false) ||
                savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED);

        if (preferDeserialized) {
            new LoadThreadsTask().execute();
        } else {
            refreshThreads(false);
        }
    }

    private void setAdapterData(ThreadModel[] threads) {
        mAdapter.setAdapterData(threads);
    }
        //TODO implement search
//    @Override
//    public boolean onSearchRequested() {
//        Bundle data = new Bundle();
//        data.putString(Constants.EXTRA_WEBSITE, mWebsite.name());
//        data.putString(Constants.EXTRA_BOARD_NAME, mBoardName);
//
//        startSearch(null, false, data, false);
//
//        return true;
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.board, menu);
        mMenu = menu;
        updateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_menu_id:
                refreshThreads(false);
                break;
            case R.id.open_browser_menu_id:
                String browserUrl;
                if (mIsCatalog) {
                    browserUrl = mUrlBuilder.getCatalogUrlHtml(mBoardName, mCatalogFilter);
                } else {
                    browserUrl = mUrlBuilder.getPageUrlHtml(mBoardName, mPageNumber);
                }
                BrowserLauncher.launchExternalBrowser(getActivity(), browserUrl);
                break;
            case R.id.add_menu_id:
                navigateToAddThreadView();
                break;
//            case R.id.menu_search_id:
//                onSearchRequested();
//                break;
            case R.id.menu_catalog_id:
                navigateToCatalog();
                break;
            case android.R.id.home:
                ;
                break;
            case R.id.add_remove_favorites_menu_id:
                if (mFavoritesDatasource.hasFavorites(mWebsite.name(), mBoardName, null)) {
                    mFavoritesDatasource.removeFromFavorites(mWebsite.name(), mBoardName, null);
                } else {
                    mFavoritesDatasource.addToFavorites(mWebsite.name(), mBoardName, null, mTabModel.getTitle());
                }

                updateOptionsMenu();

                break;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_ADD_POST_ACTIVITY:
                    // Получаем номер созданного треда и переходим к нему. Иначе
                    // обновляем список тредов на всякий случай
                    String redirectedThreadNumber = intent.getExtras().getString(Constants.EXTRA_REDIRECTED_THREAD_NUMBER);
                    if (redirectedThreadNumber != null) {
                        navigateToThread(redirectedThreadNumber);
                    } else {
                        refreshThreads(false);
                    }
                    break;
            }
        }
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ThreadItemViewModel item = mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_ANSWER, 0, getString(R.string.cmenu_answer_without_reading));
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_VIEW_FULL_POST, 2, getString(R.string.cmenu_view_op_post));

        if (!item.isHidden()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_HIDE_THREAD, 3, getString(R.string.cmenu_hide_thread));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ThreadItemViewModel info = mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_ANSWER: {
                navigateToAddPostView(info.getNumber());
                return true;
            }
            case Constants.CONTEXT_MENU_VIEW_FULL_POST: {
                PostItemViewModel postModel = new PostItemViewModel(mWebsite, mBoardName, info.getNumber(), Constants.OP_POST_POSITION, info.getOpPost(), getActivity().getTheme(), ClickListenersFactory.getDefaultSpanClickListener(mUrlBuilder));
                mPostItemViewBuilder.displayPopupDialog(postModel, getActivity(), getActivity().getTheme(), null);
                return true;
            }
            case Constants.CONTEXT_MENU_HIDE_THREAD: {
                mHiddenThreadsDataSource.addToHiddenThreads(mWebsite.name(), mBoardName, info.getNumber());
                info.setHidden(true);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        super.onSaveInstanceState(outState);
    }

    private void updateOptionsMenu() {
        if (mMenu == null) {
            return;
        }

        MenuItem favoritesItem = mMenu.findItem(R.id.add_remove_favorites_menu_id);
        if (mFavoritesDatasource.hasFavorites(mWebsite.name(), mBoardName, null)) {
            favoritesItem.setTitle(R.string.menu_remove_favorites);
        } else {
            favoritesItem.setTitle(R.string.menu_add_favorites);
        }
    }

    private void navigateToAddThreadView() {
        Intent addPostIntent = new Intent(getContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, mWebsite.name());
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, "");

        startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    private void navigateToAddPostView(String threadNumber) {
        Intent addPostIntent = new Intent(getContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, mWebsite.name());
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, threadNumber);

        startActivity(addPostIntent);
    }

    private void navigateToThread(String threadNumber) {
        navigateToThread(threadNumber, null);
    }

    private void navigateToThread(String threadNumber, String threadSubject) {

        NavigationService.getInstance().navigateThread(mWebsite.name(), mBoardName, threadNumber, threadSubject, null, false);

    }

    private void navigateToCatalog() {
        //TODO
        //mNavigationService.navigateCatalog(getFragmentManager(), mWebsite.name(), mBoardName);
    }

    private void navigateToPageNumber(int pageNumber) {
        //TODO
        //mNavigationService.onBoardClick(getFragmentManager(), null, mWebsite.name(), mBoardName, pageNumber, false);
    }

    private void refreshThreads(boolean checkModified) {
        if (mCurrentDownloadTask != null) {
            mCurrentDownloadTask.cancel(true);
        }

        int pageNumberOrFilter = mIsCatalog ? mCatalogFilter : mPageNumber;
        mCurrentDownloadTask = new DownloadThreadsTask(getActivity(), mThreadsReaderListener, mBoardName, mIsCatalog, pageNumberOrFilter, checkModified, mJsonReader);
        mCurrentDownloadTask.execute();
    }

    @Override
    public void onRefresh() {
        refreshThreads(false);
    }

    private class LoadThreadsTask extends AsyncTask<Void, Long, ThreadModel[]> {
        @Override
        protected ThreadModel[] doInBackground(Void... arg0) {
            if (!mIsCatalog) {
                return mSerializationService.deserializeThreads(mWebsite.name(), mBoardName, mPageNumber);
            }
            return null;
        }

        @Override
        public void onPreExecute() {
            mThreadsReaderListener.showLoadingScreen();
        }

        @Override
        public void onPostExecute(ThreadModel[] threads) {
            mThreadsReaderListener.hideLoadingScreen();

            if (threads == null) {
                refreshThreads(false);
            } else {
                setAdapterData(threads);
                // Устанавливаем позицию, если открываем как уже открытую вкладку
                if (mTabModel.getPosition() != null) {
                    AppearanceUtils.ListViewPosition p = mTabModel.getPosition();
                    mListView.setSelectionFromTop(p.position, p.top);
                }
            }
        }
    }

    private class ThreadsReaderListener implements IListView<ThreadModel[]> {

        @Override
        public void setWindowProgress(int value) {
            getActivity().setProgress(value);
        }

        @Override
        public void setData(ThreadModel[] threads) {
            if (threads != null && threads.length > 0) {
                if (!mIsCatalog) {
                    mSerializationService.serializeThreads(mWebsite.name(), mBoardName, mPageNumber, threads);
                }
                setAdapterData(threads);
            } else {
                String error = getString(R.string.error_list_empty);
                if (mAdapter.getCount() == 0) {
                    showError(error);
                } else {
                    showToastIfVisible(error);
                }
            }
        }

        @Override
        public void showError(String error) {
            switchToErrorView(error);
            if (error != null && error.startsWith("503")) {
                String url = mUrlBuilder.getPageUrlHtml(mBoardName, mPageNumber);
                new CloudflareCheckService(url, getActivity(), new ICloudflareCheckListener(){
                    public void onSuccess() {
                        refreshThreads(false);;
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
            switchToCaptchaView(mWebsite, captcha);
        }

        @Override
        public void showLoadingScreen() {
            switchToLoadingView();
        }

        @Override
        public void hideLoadingScreen() {
            switchToListView();
            mCurrentDownloadTask = null;
        }
    }
}
