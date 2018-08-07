package ua.in.quireg.chan.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;

import java.util.List;

import javax.inject.Inject;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.DownloadThreadsTask;
import ua.in.quireg.chan.boards.makaba.MakabaApiReader;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.MainApplication;
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
import ua.in.quireg.chan.mvp.presenters.ThreadsListPresenter;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.routing.commands.NavigateThread;
import ua.in.quireg.chan.mvp.views.ThreadsListView;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.services.CloudflareCheckService;
import ua.in.quireg.chan.services.presentation.ClickListenersFactory;
import ua.in.quireg.chan.services.presentation.ListViewScrollListener;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;
import ua.in.quireg.chan.services.presentation.PostItemViewBuilder;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.activities.AddPostActivity;
import ua.in.quireg.chan.ui.adapters.ThreadsListAdapter;

public class ThreadsListFragment extends MvpAppCompatFragment implements ThreadsListView, OnRefreshListener {

    @InjectPresenter(type = PresenterType.WEAK)
    ThreadsListPresenter mThreadsListPresenter;

    @Inject ApplicationSettings mSettings;
    @Inject FavoritesDataSource mFavoritesDatasource;
    @Inject OpenTabsManager mOpenTabsManager;
    @Inject MainRouter mRouter;

    private PostItemViewBuilder mPostItemViewBuilder;
    private IUrlBuilder mUrlBuilder;

    private ThreadsListAdapter mAdapter = null;

    protected ListView mListView;
    protected View mLoadingView;
    protected SwipeToLoadLayout mSwipeToLoadLayout;

    private OpenTabModel mTabModel;
    private IWebsite mWebsite = Websites.getDefault();
    private String mBoardName;
    private int mPageNumber;
    private boolean mIsCatalog;
    private int mCatalogFilter = 0;

    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        mUrlBuilder = mWebsite.getUrlBuilder();
        if (getArguments() == null) {
            throw new RuntimeException("No arguments supplied");
        }
        mBoardName = getArguments().getString(Constants.EXTRA_BOARD_NAME);
        mPageNumber = getArguments().getInt(Constants.EXTRA_BOARD_PAGE, 0);
        mIsCatalog = getArguments().getBoolean(Constants.EXTRA_CATALOG);

//        if (StringUtils.areEqual(mBoardName, "g")) {
//            extras.putString(Constants.EXTRA_BOARD_NAME, "gg");
//            mNavigationService.restartActivity(this, extras);
//        }
        // Сохраняем во вкладках
        OpenTabModel tabModel = new OpenTabModel(mWebsite, mBoardName, mPageNumber, null, null, false);
        mTabModel = mOpenTabsManager.add(tabModel);
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Заголовок страницы
        String pageTitle = mIsCatalog
                ? String.format(getString(R.string.data_board_title_catalog), mBoardName)
                : mPageNumber > 0
                ? String.format(getString(R.string.data_board_title_with_page), mBoardName, String.valueOf(mPageNumber))
                : String.format(getString(R.string.data_board_title), mBoardName);

        if ((getActivity()) != null) {
            ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (mActionBar != null) {
                mActionBar.setTitle(pageTitle);
            }
        }
    }

    @Override
    public void onPause() {
        mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(mListView));
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.threads_list_view2, container, false);
        mSwipeToLoadLayout = rootView.findViewById(R.id.swipeToLoadLayout);
        mListView = rootView.findViewById(R.id.swipe_target);
        mLoadingView = rootView.findViewById(R.id.empty_list_item);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeToLoadLayout.setOnRefreshListener(this);
        mPostItemViewBuilder = new PostItemViewBuilder(view.getContext(), mWebsite, mBoardName, null, mSettings);

        if (mAdapter == null) {
            mAdapter = new ThreadsListAdapter(view.getContext());
        }

        mListView.setAdapter(mAdapter);

        registerForContextMenu(mListView);

//        boolean preferDeserialized = getArguments().getBoolean(Constants.EXTRA_PREFER_DESERIALIZED, false) ||
//                savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED);
//
//        if (preferDeserialized) {
//        } else {
//            refreshThreads(false);
//        }

        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            mThreadsListPresenter.onListItemClick(mAdapter.getItem(position));
        });
        mThreadsListPresenter.requestThreadsList();

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
                onRefresh();
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
                getActivity().onBackPressed();
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
//                mHiddenThreadsDataSource.addToHiddenThreads(mWebsite.name(), mBoardName, info.getNumber());
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
        mRouter.navigateThread(mWebsite.name(), mBoardName, threadNumber, threadSubject, null, false);

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
        onRefresh();
    }

    @Override
    public void onRefresh() {
        mThreadsListPresenter.requestThreadsList();
    }


    @Override
    public void showThreads(List<ThreadItemViewModel> threads) {
        mAdapter.clear();
        mAdapter.addAll(threads);
        mAdapter.notifyDataSetChanged();
        mLoadingView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mSwipeToLoadLayout.setRefreshing(false);
    }

//    private class ThreadsReaderListener implements IListView<ThreadModel[]> {
//
//        @Override
//        public void setWindowProgress(int value) {
//            getActivity().setProgress(value);
//        }
//
//        @Override
//        public void setData(ThreadModel[] threads) {
//            if (threads != null && threads.length > 0) {
//                if (!mIsCatalog) {
//                    mSerializationService.serializeThreads(mWebsite.name(), mBoardName, mPageNumber, threads);
//                }
//                setAdapterData(threads);
//            } else {
//                String error = getString(R.string.error_list_empty);
//                if (mAdapter.getCount() == 0) {
//                    showError(error);
//                } else {
////                    showToastIfVisible(error);
//                }
//            }
//        }
//
//        @Override
//        public void showError(String error) {
////            switchToErrorView(error);
//            if (error != null && error.startsWith("503")) {
//                String url = mUrlBuilder.getPageUrlHtml(mBoardName, mPageNumber);
//                new CloudflareCheckService(url, getActivity(), new ICloudflareCheckListener() {
//                    public void onSuccess() {
//                        refreshThreads(false);
//
//                    }
//
//                    public void onStart() {
//                        showError(getString(R.string.notification_cloudflare_check_started));
//                    }
//
//                    public void onTimeout() {
//                        showError(getString(R.string.error_cloudflare_check_timeout));
//                    }
//                }).start();
//            }
//        }
//
//        @Override
//        public void showCaptcha(CaptchaEntity captcha) {
////            switchToCaptchaView(mWebsite, captcha);
//        }
//
//        @Override
//        public void showLoadingScreen() {
////            switchToLoadingView();
//        }
//
//        @Override
//        public void hideLoadingScreen() {
////            switchToListView();
//            mSwipeToLoadLayout.setRefreshing(false);
//            mCurrentDownloadTask = null;
//
//        }
//    }
}
