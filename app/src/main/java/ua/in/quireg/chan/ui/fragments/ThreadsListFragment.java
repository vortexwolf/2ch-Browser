package ua.in.quireg.chan.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EdgeEffect;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.presentation.IThreadListEntity;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.models.presentation.PageDividerViewModel;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.mvp.presenters.ThreadsListPresenter;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.views.ThreadsListView;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.services.presentation.ClickListenersFactory;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.services.presentation.PostItemViewBuilder;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.activities.AddPostActivity;
import ua.in.quireg.chan.ui.adapters.ThreadsListRecyclerViewAdapter;
import ua.in.quireg.chan.ui.views.RecyclerViewWithCM;

public class ThreadsListFragment extends MvpAppCompatFragment
        implements ThreadsListView, OnRefreshListener {

    @InjectPresenter(type = PresenterType.WEAK)
    ThreadsListPresenter mThreadsListPresenter;

    @Inject ApplicationSettings mSettings;
    @Inject FavoritesDataSource mFavoritesDatasource;
    @Inject OpenTabsManager mOpenTabsManager;
    @Inject MainRouter mRouter;

    private PostItemViewBuilder mPostItemViewBuilder;
    private IUrlBuilder mUrlBuilder;

    private ThreadsListRecyclerViewAdapter mAdapter;

    protected RecyclerViewWithCM mRecyclerView;
    protected View mLoadingView;
    protected View mRootView;
    protected SwipeToLoadLayout mSwipeToLoadLayout;

    private OpenTabModel mTabModel;
    private IWebsite mWebsite = Websites.getDefault();
    private String mBoardName;
    private int mPageNumber;
    private boolean mIsCatalog;
    private int mCatalogFilter = 0;

    private Menu mMenu;

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        mUrlBuilder = mWebsite.getUrlBuilder();

        mBoardName = getArguments().getString(Constants.EXTRA_BOARD_NAME);
        mPageNumber = getArguments().getInt(Constants.EXTRA_BOARD_PAGE, 0);
        mIsCatalog = getArguments().getBoolean(Constants.EXTRA_CATALOG);

//        if (StringUtils.areEqual(mBoardName, "g")) {
//            extras.putString(Constants.EXTRA_BOARD_NAME, "gg");
//            mNavigationService.restartActivity(this, extras);
//        }
        // Сохраняем во вкладках
        OpenTabModel tabModel =
                new OpenTabModel(mWebsite, mBoardName, mPageNumber, null, null, false);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.threads_list_view2, container, false);
//        mSwipeToLoadLayout = rootView.findViewById(R.id.swipeToLoadLayout);
        mRecyclerView = rootView.findViewById(R.id.list);
        mLoadingView = rootView.findViewById(R.id.empty_list_item);
        mRootView = rootView.findViewById(R.id.threads_list_root);

        return rootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mSwipeToLoadLayout.setOnRefreshListener(this);
        mPostItemViewBuilder = new PostItemViewBuilder(
                view.getContext(), mWebsite, mBoardName, null, mSettings);

        if (mAdapter == null) mAdapter = new ThreadsListRecyclerViewAdapter(mThreadsListPresenter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(
                mSettings.isMultiThumbnailsInThreads()
                ? new DividerItemDecoration(view.getContext(), layoutManager.getOrientation())
                : new LeftPaddedDivider(view.getContext())
        );
        mRecyclerView.setAdapter(mAdapter);

//        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
//        RecyclerView.EdgeEffectFactory f = new RecyclerView.EdgeEffectFactory() {
//            @SuppressLint("ClickableViewAccessibility")
//            @NonNull
//            @Override
//            protected EdgeEffect createEdgeEffect(RecyclerView view, int direction) {
//                if (DIRECTION_TOP == direction) {
//
//                    EdgeEffect effect = new EdgeEffect(view.getContext());
//
//                    return effect;
//                }
//                return super.createEdgeEffect(view, direction);
//            }
//        };
//
//        mRecyclerView.setEdgeEffectFactory(f);

//        mRootView.setOnTouchListener((v, event) -> {
//            Timber.d("onTouch: mRootView");
//
//            if (mRecyclerView.computeVerticalScrollOffset() == 0) {
//                return true;
//            }
//            return false;
//        });

    //        mRecyclerView.setOnTouchListener((v1, event) -> {
    //            Timber.d("onTouch: mRecyclerView");
    //
    //            float initialY = 0;
    //            int pointerId = event.getPointerId(0);
    //
    //            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
    //                float moveY = event.getY(pointerId);
    //                if (mRecyclerView.computeVerticalScrollOffset() == 0) {
    ////                                Timber.d("TranslationY: " + (moveY - initialY));
    //                    Timber.d("Y: " + moveY);
    //                    mRecyclerView.setTranslationY(moveY - initialY);
    //                    return true;
    //                } else {
    //                    initialY = event.getY();
    //                }
    //            } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL ||
    //                    event.getAction() == MotionEvent.ACTION_UP) {
    //                mRecyclerView.setTranslationY(0);
    //            } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
    //                initialY = event.getY();
    //            }
    //
    //            return false;
    //        });
        registerForContextMenu(mRecyclerView);

//        boolean preferDeserialized = getArguments().getBoolean(Constants.EXTRA_PREFER_DESERIALIZED, false) ||
//                savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED);
//

//        mRecyclerView.setOnItemClickListener((parent, v, position, id) -> {
//            mThreadsListPresenter.onListItemClick((ThreadItemViewModel) parent.getItemAtPosition(position));
//        });

//                // Устанавливаем позицию, если открываем как уже открытую вкладку
//                if (mTabModel.getPosition() != null) {
//                    AppearanceUtils.ListViewPosition p = mTabModel.getPosition();
//                    mListView.setSelectionFromTop(p.position, p.top);
//                }
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
                    mFavoritesDatasource.removeFromFavorites(
                            mWebsite.name(), mBoardName, null);
                } else {
                    mFavoritesDatasource.addToFavorites(
                            mWebsite.name(), mBoardName, null, mTabModel.getTitle());
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
                    String redirectedThreadNumber =
                            intent.getExtras().getString(Constants.EXTRA_REDIRECTED_THREAD_NUMBER);
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

        RecyclerViewWithCM.ContextMenuInfo info = (RecyclerViewWithCM.ContextMenuInfo) menuInfo;

        ThreadItemViewModel item = (ThreadItemViewModel) mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_ANSWER, 0, getString(R.string.cmenu_answer_without_reading));
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_VIEW_FULL_POST, 2, getString(R.string.cmenu_view_op_post));

        if (!item.isHidden()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_HIDE_THREAD, 3, getString(R.string.cmenu_hide_thread));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        RecyclerViewWithCM.ContextMenuInfo menuInfo = (RecyclerViewWithCM.ContextMenuInfo) item.getMenuInfo();

        ThreadItemViewModel info = (ThreadItemViewModel) mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_ANSWER: {
                navigateToAddPostView(info.getNumber());
                return true;
            }
            case Constants.CONTEXT_MENU_VIEW_FULL_POST: {
                PostItemViewModel postModel = new PostItemViewModel(
                        mWebsite, mBoardName, info.getNumber(),
                        Constants.OP_POST_POSITION, info.getOpPost(), getActivity().getTheme(),
                        ClickListenersFactory.getDefaultSpanClickListener(mUrlBuilder));

                mPostItemViewBuilder.displayPopupDialog(postModel, getActivity(), getActivity().getTheme(), null);
                return true;
            }
            case Constants.CONTEXT_MENU_HIDE_THREAD: {
                mThreadsListPresenter.hideThread(info);
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
//        mRouter.navigateThread(mWebsite.name(), mBoardName, threadNumber, threadSubject, null, false);

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
        mThreadsListPresenter.refreshList();
    }

    @Override
    public void showThreads(List<IThreadListEntity> threads) {
        Timber.i("showThreads() %s", threads.toString());
        if (!threads.isEmpty()) {
            for (IThreadListEntity model : threads) {
                mAdapter.addToList(model);
            }
        }
    }

    @Override
    public void setList(List<IThreadListEntity> threads) {
        Timber.i("showThreads() %s", threads.toString());
        mAdapter.setList(threads);
    }

    @Override
    public void clearList() {
        mAdapter.clearList();
    }

    @Override
    public void setListPosition(int position) {
        Timber.i("smoothScrollToPosition(): %d", position);
        mRecyclerView.smoothScrollToPosition(position);
    }
    AnimatorSet mAnimatorSet;
    @Override
    public void startLoadingFirstTime() {
        Timber.d("startLoadingFirstTime()");
        ObjectAnimator fadeOut =
                ObjectAnimator.ofFloat(mRecyclerView, View.ALPHA, mRecyclerView.getAlpha(), 0f);
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRecyclerView.setVisibility(View.GONE);
                mLoadingView.setVisibility(View.VISIBLE);
//                mSwipeToLoadLayout.setRefreshing(true);
            }
        });
        mAnimatorSet.play(fadeOut);
        mAnimatorSet.setDuration(200);
        mAnimatorSet.start();
    }

    @Override
    public void stopLoadingFirstTime() {
        Timber.d("stopLoadingFirstTime()");
        ObjectAnimator fadeIn =
                ObjectAnimator.ofFloat(mRecyclerView, View.ALPHA, mRecyclerView.getAlpha(), 1f);
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRecyclerView.setVisibility(View.VISIBLE);
                mLoadingView.setVisibility(View.GONE);
//                mSwipeToLoadLayout.setRefreshing(false);
            }
        });
        mAnimatorSet.play(fadeIn);
        mAnimatorSet.setDuration(200);
        mAnimatorSet.start();
    }

    @Override
    public void startLoadingNewPage() {

    }

    @Override
    public void stopLoadingNewPage() {
//        mSwipeToLoadLayout.setRefreshing(false);
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

    private class LeftPaddedDivider extends RecyclerView.ItemDecoration {
        private static final int THUMBNAIL_PADDING_DP = 8;

        private final Drawable mDivider;
        private final int mThumbnailSize;
        private final int mThumbnailPadding;

        LeftPaddedDivider(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.shadowline);
            mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
            mThumbnailPadding =
                    THUMBNAIL_PADDING_DP * Math.round(
                            (float) (context.getResources().getDisplayMetrics().densityDpi /
                                    DisplayMetrics.DENSITY_DEFAULT));
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                if (parent.getChildAt(i + 1) == null) {
                    //do not draw if it is a last item in list
                    continue;
                }
                if (parent.getChildAt(i).getTag() instanceof PageDividerViewModel) {
                    //do not draw if it is a page divider
                    continue;
                }
                if (parent.getChildAt(i + 1).getTag() instanceof PageDividerViewModel) {
                    //do not draw if next item is a page divider
                    continue;
                }

                RecyclerView.LayoutParams params =
                        (RecyclerView.LayoutParams) view.getLayoutParams();
                int left = mThumbnailSize + mThumbnailPadding;
                int top = view.getBottom() + params.bottomMargin;
                int right = parent.getWidth() - parent.getPaddingRight();
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
